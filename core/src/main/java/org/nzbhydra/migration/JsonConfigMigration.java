package org.nzbhydra.migration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import joptsimple.internal.Strings;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.nzbhydra.config.AuthConfig;
import org.nzbhydra.config.AuthType;
import org.nzbhydra.config.BaseConfig;
import org.nzbhydra.config.CategoriesConfig;
import org.nzbhydra.config.Category;
import org.nzbhydra.config.ConfigProvider;
import org.nzbhydra.config.DownloadType;
import org.nzbhydra.config.DownloaderConfig;
import org.nzbhydra.config.DownloaderType;
import org.nzbhydra.config.IndexerCategoryConfig;
import org.nzbhydra.config.IndexerConfig;
import org.nzbhydra.config.LoggingConfig;
import org.nzbhydra.config.MainConfig;
import org.nzbhydra.config.NzbAccessType;
import org.nzbhydra.config.NzbAddingType;
import org.nzbhydra.config.ProxyType;
import org.nzbhydra.config.SearchModuleType;
import org.nzbhydra.config.SearchSourceRestriction;
import org.nzbhydra.config.SearchingConfig;
import org.nzbhydra.config.UserAuthConfig;
import org.nzbhydra.indexers.CheckCapsRespone;
import org.nzbhydra.indexers.Indexer.BackendType;
import org.nzbhydra.indexers.NewznabChecker;
import org.nzbhydra.mapping.newznab.ActionAttribute;
import org.nzbhydra.mediainfo.InfoProvider.IdType;
import org.nzbhydra.migration.FromPythonMigration.MigrationMessageEvent;
import org.nzbhydra.migration.configmapping.Auth;
import org.nzbhydra.migration.configmapping.Categories;
import org.nzbhydra.migration.configmapping.Downloader;
import org.nzbhydra.migration.configmapping.Indexer;
import org.nzbhydra.migration.configmapping.Logging;
import org.nzbhydra.migration.configmapping.Main;
import org.nzbhydra.migration.configmapping.OldConfig;
import org.nzbhydra.migration.configmapping.Searching;
import org.nzbhydra.migration.configmapping.User;
import org.nzbhydra.searching.CategoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Component
public class JsonConfigMigration {

    private static final Logger logger = LoggerFactory.getLogger(JsonConfigMigration.class);
    private static final int NZBHYDRA1_SUPPORTED_CONFIG_VERSION = 40;

    private static Map<String, SearchSourceRestriction> searchSourceRestrictionMap = new HashMap<>();

    static {
        searchSourceRestrictionMap.put("internal", SearchSourceRestriction.INTERNAL);
        searchSourceRestrictionMap.put("external", SearchSourceRestriction.API);
        searchSourceRestrictionMap.put("both", SearchSourceRestriction.BOTH);
        searchSourceRestrictionMap.put("always", SearchSourceRestriction.BOTH);
    }

    @Autowired
    private CategoryProvider categoryProvider;
    @Autowired
    private ConfigProvider configProvider;
    @Autowired
    private NewznabChecker newznabChecker;
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    public ConfigMigrationResult migrate(String oldConfigJson) throws IOException {
        logger.info("Migrating config from NZBHydra 1");
        eventPublisher.publishEvent(new MigrationMessageEvent("Migrating config and checking indexers"));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        OldConfig oldConfig = mapper.readValue(oldConfigJson, OldConfig.class);

        BaseConfig newConfig = configProvider.getBaseConfig();
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new Jdk8Module());
        newConfig = mapper.readValue(mapper.writeValueAsString(newConfig), BaseConfig.class); //Easy way of cloning the base config

        List<String> messages = new ArrayList<>();

        if (oldConfig.getMain().getConfigVersion() != NZBHYDRA1_SUPPORTED_CONFIG_VERSION) {
            logger.warn("Unable to migrate config from config version {}. Aborting", oldConfig.getMain().getConfigVersion());
            throw new IllegalStateException("Unable to migrate config from config version " + oldConfig.getMain().getConfigVersion());
        }

        try {
            messages.addAll(migrateMain(oldConfig.getMain(), newConfig.getMain()));
        } catch (Exception e) {
            logger.error("Error while migrating main settings", e);
            messages.add("Error while migrating main settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateIndexers(oldConfig, newConfig));
        } catch (Exception e) {
            logger.error("Error while migrating indexer settings", e);
            messages.add("Error while migrating indexer settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateSearching(oldConfig, newConfig.getSearching()));
        } catch (Exception e) {
            logger.error("Error while migrating searching settings", e);
            messages.add("Error while migrating searching settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateAuth(oldConfig.getAuth(), newConfig.getAuth()));
        } catch (Exception e) {
            logger.error("Error while migrating auth settings", e);
            messages.add("Error while migrating auth settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateLogging(oldConfig.getMain().getLogging(), newConfig.getMain().getLogging()));
        } catch (Exception e) {
            logger.error("Error while migrating logging settings", e);
            messages.add("Error while migrating logging settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateCategories(oldConfig.getCategories(), newConfig.getCategoriesConfig()));
        } catch (Exception e) {
            logger.error("Error while migrating category settings", e);
            messages.add("Error while migrating category settings. Please check and set the values manually.");
        }
        try {
            messages.addAll(migrateDownloaders(oldConfig, newConfig));
        } catch (Exception e) {
            logger.error("Error while migrating downloader settings", e);
            messages.add("Error while migrating downloader settings. Please check and set the values manually.");
        }

        configProvider.getBaseConfig().replace(newConfig);
        configProvider.getBaseConfig().save();
        eventPublisher.publishEvent(new MigrationMessageEvent("Completed migrating config with " + messages.size() + " messages"));
        return new ConfigMigrationResult(newConfig, messages);
    }

    private List<String> migrateDownloaders(OldConfig oldConfig, BaseConfig newConfig) {
        List<String> messages = new ArrayList<>();
        List<DownloaderConfig> downloaders = new ArrayList<>();
        for (Downloader oldDownloader : oldConfig.getDownloaders()) {
            DownloaderConfig newDownloader = new DownloaderConfig();

            if (oldDownloader.getType().equals("nzbget")) {
                newDownloader.setDownloaderType(DownloaderType.NZBGET);
                String url = (oldDownloader.isSsl() ? "https://" : "http://");
                newDownloader.setUsername(oldDownloader.getUsername());
                newDownloader.setPassword(oldDownloader.getPassword());
                url += oldDownloader.getHost() + ":" + oldDownloader.getPort();
                newDownloader.setUrl(url);
            } else {
                newDownloader.setDownloaderType(DownloaderType.SABNZBD);
                newDownloader.setUrl(oldDownloader.getUrl());
            }
            newDownloader.setName(oldDownloader.getName());
            try {
                newDownloader.setNzbAddingType(NzbAddingType.valueOf((oldDownloader.getNzbAddingType().toUpperCase().replace("LINK", "SEND_LINK").replace("NZB", "UPLOAD"))));
            } catch (IllegalArgumentException e) {
                logAsWarningAndAdd(messages, "Unable to migrate NZB adding type for downloader '" + oldDownloader.getName() + "'. Setting it to 'Send link'.");
                newDownloader.setNzbAddingType(NzbAddingType.SEND_LINK);
            }
            if (oldDownloader.getNzbaccesstype().equals("serve")) {
                newDownloader.setNzbAccessType(NzbAccessType.PROXY);
            } else {
                newDownloader.setNzbAccessType(NzbAccessType.REDIRECT);
            }
            newDownloader.setIconCssClass(oldDownloader.getIconCssClass());
            newDownloader.setDefaultCategory(oldDownloader.getDefaultCategory());
            newDownloader.setDownloadType(DownloadType.NZB);
            newDownloader.setEnabled(oldDownloader.isEnabled());
            downloaders.add(newDownloader);
        }
        newConfig.getDownloading().setDownloaders(downloaders);
        return messages;
    }

    protected List<String> migrateCategories(Categories oldCategories, CategoriesConfig newCategories) {
        //Will only migrate categories that exist under that name in the new config
        newCategories.setEnableCategorySizes(oldCategories.isEnableCategorySizes());
        for (Category newCategory : newCategories.getCategories()) {
            org.nzbhydra.migration.configmapping.Category oldCat = oldCategories.getCategories().get(newCategory.getName().replace(" ", "").toLowerCase());
            if (oldCat != null) {
                newCategory.setApplyRestrictionsType(searchSourceRestrictionMap.getOrDefault(oldCat.getApplyRestrictions(), SearchSourceRestriction.NONE));
                newCategory.setForbiddenRegex(oldCat.getForbiddenRegex());
                newCategory.setForbiddenWords(oldCat.getForbiddenWords());
                newCategory.setMinSizePreset(oldCat.getMin());
                newCategory.setMaxSizePreset(oldCat.getMax());
                newCategory.setNewznabCategories(oldCat.getNewznabCategories());
                newCategory.setRequiredRegex(oldCat.getRequiredRegex());
                newCategory.setRequiredWords(oldCat.getRequiredWords());
                newCategory.setIgnoreResultsFrom(searchSourceRestrictionMap.getOrDefault(oldCat.getIgnoreResults(), SearchSourceRestriction.NONE));
            }
        }
        return Collections.emptyList();
    }

    private List<String> migrateLogging(Logging oldLogging, LoggingConfig newLogging) {
        newLogging.setConsolelevel(oldLogging.getConsolelevel());
        newLogging.setLogfilelevel(oldLogging.getLogfilelevel());
        newLogging.setLogMaxSize(oldLogging.getKeepLogFiles());
        return Collections.emptyList();
    }

    private List<String> migrateAuth(Auth oldAuth, AuthConfig newAuth) {
        logger.info("Migrating auth settings");
        List<String> messages = new ArrayList<>();
        newAuth.setRestrictAdmin(oldAuth.isRestrictAdmin());
        newAuth.setRestrictSearch(oldAuth.isRestrictSearch());
        newAuth.setRestrictStats(oldAuth.isRestrictStats());
        newAuth.setRestrictDetailsDl(oldAuth.isRestrictDetailsDl());
        newAuth.setRestrictIndexerSelection(oldAuth.isRestrictIndexerSelection());
        try {
            newAuth.setAuthType(AuthType.valueOf((oldAuth.getAuthType().toUpperCase())));
        } catch (IllegalArgumentException e) {
            logAsWarningAndAdd(messages, "Unable to migrate auth type. Setting it to 'None'");
            newAuth.setAuthType(AuthType.NONE);
        }
        newAuth.setRememberUsers(oldAuth.isRestrictAdmin());
        for (User user : oldAuth.getUsers()) {
            UserAuthConfig newUserConfig = new UserAuthConfig();
            newUserConfig.setMaySeeAdmin(user.isMaySeeAdmin());
            newUserConfig.setMaySeeStats(user.isMaySeeStats());
            newUserConfig.setMaySeeDetailsDl(user.isMaySeeDetailsDl());
            newUserConfig.setShowIndexerSelection(user.isShowIndexerSelection());
            newUserConfig.setUsername(user.getUsername());
            newUserConfig.setPassword(user.getPassword());
            newAuth.getUsers().add(newUserConfig);
        }
        return messages;
    }

    private void logAsWarningAndAdd(List<String> messages, String message) {
        logger.warn(message);
        messages.add(message);
    }

    private List<String> migrateSearching(OldConfig oldConfig, SearchingConfig newSearching) {
        logger.info("Migrating search settings");
        List<String> messages = new ArrayList<>();
        Searching oldSearching = oldConfig.getSearching();
        newSearching.setAlwaysShowDuplicates(oldSearching.isAlwaysShowDuplicates());
        try {
            newSearching.setApplyRestrictions(SearchSourceRestriction.valueOf(oldSearching.getApplyRestrictions().toUpperCase()));
        } catch (IllegalArgumentException e) {
            newSearching.setApplyRestrictions(SearchSourceRestriction.BOTH);
            logAsWarningAndAdd(messages, "Unable to migrate 'Enable for' in searching config. Setting it to 'Both'.");
        }
        newSearching.setDuplicateAgeThreshold(oldSearching.getDuplicateAgeThreshold());
        newSearching.setDuplicateSizeThresholdInPercent(oldSearching.getDuplicateSizeThresholdInPercent());
        if (oldSearching.getIdFallbackToTitle().contains("internal") && oldSearching.getIdFallbackToTitle().contains("external")) {
            newSearching.setIdFallbackToQueryGeneration(SearchSourceRestriction.BOTH);
        } else if (oldSearching.getIdFallbackToTitle().contains("external")) {
            newSearching.setIdFallbackToQueryGeneration(SearchSourceRestriction.API);
        } else if (oldSearching.getIdFallbackToTitle().contains("internal")) {
            newSearching.setIdFallbackToQueryGeneration(SearchSourceRestriction.INTERNAL);
        } else {
            newSearching.setIdFallbackToQueryGeneration(SearchSourceRestriction.NONE);
        }
        if (oldSearching.getGenerateQueries().size() == 2) {
            newSearching.setGenerateQueries(SearchSourceRestriction.BOTH);
        } else if (oldSearching.getGenerateQueries().contains("internal")) {
            newSearching.setGenerateQueries(SearchSourceRestriction.INTERNAL);
        } else if (oldSearching.getGenerateQueries().contains("external")) {
            newSearching.setGenerateQueries(SearchSourceRestriction.API);
        } else {
            newSearching.setGenerateQueries(SearchSourceRestriction.NONE);
        }
        newSearching.setIgnorePassworded(oldSearching.isIgnorePassworded());
        newSearching.setIgnoreTemporarilyDisabled(oldSearching.isIgnoreTemporarilyDisabled());
        newSearching.setForbiddenWords(oldSearching.getForbiddenWords());
        newSearching.setMaxAge(oldSearching.getMaxAge());
        if (oldSearching.getNzbAccessType().equals("serve")) {
            newSearching.setNzbAccessType(NzbAccessType.PROXY);
        } else {
            newSearching.setNzbAccessType(NzbAccessType.REDIRECT);
        }
        newSearching.setRemoveTrailing(oldSearching.getRemoveTrailing());
        newSearching.setRequiredWords(oldSearching.getRequiredWords());
        newSearching.setTimeout(oldSearching.getTimeout());
        newSearching.setUserAgent(oldSearching.getUserAgent());
        newSearching.setRequiredRegex(oldSearching.getRequiredRegex());
        newSearching.setForbiddenRegex(oldSearching.getForbiddenRegex());
        newSearching.setForbiddenGroups(oldSearching.getForbiddenGroups());
        newSearching.setForbiddenPosters(oldSearching.getForbiddenPosters());
        newSearching.setKeepSearchResultsForDays(oldConfig.getMain().getKeepSearchResultsForDays());
        if (newSearching.getKeepSearchResultsForDays() == 7) {
            logger.info("Increasing age of results to keep to 14 days");
            newSearching.setKeepSearchResultsForDays(14);
        }
        return messages;
    }

    private List<String> migrateMain(Main oldMain, MainConfig newMain) {
        logger.info("Migrating main settings");
        List<String> messages = new ArrayList<>();
        newMain.setApiKey(oldMain.getApikey());
        newMain.setDereferer(Strings.isNullOrEmpty((oldMain.getDereferer())) ? null : (oldMain.getDereferer()));
        newMain.setExternalUrl(Strings.isNullOrEmpty(oldMain.getExternalUrl()) ? null : oldMain.getExternalUrl());
        newMain.setHost(oldMain.getHost());
        newMain.setShutdownForRestart(oldMain.isShutdownForRestart());
        migrateProxies(messages, newMain, oldMain);
        newMain.setSsl(oldMain.isSsl());
        newMain.setSslcert(Strings.isNullOrEmpty((oldMain.getSslcert())) ? null : (oldMain.getSslcert()));
        newMain.setSslkey(Strings.isNullOrEmpty((oldMain.getSslkey())) ? null : (oldMain.getSslkey()));
        newMain.setStartupBrowser(oldMain.isStartupBrowser());
        newMain.setTheme(oldMain.getTheme());
        if (!Strings.isNullOrEmpty(oldMain.getUrlBase()) || !Strings.isNullOrEmpty(oldMain.getExternalUrl())) {
            logAsWarningAndAdd(messages, "URL base and/or external URL cannot be migrated. You'll have to set them manually");
            newMain.setUrlBase(null);
            newMain.setExternalUrl(null);
        }
        newMain.setUseLocalUrlForApiAccess(oldMain.isUseLocalUrlForApiAccess());
        return messages;
    }

    protected void migrateProxies(List<String> messages, MainConfig newMain, Main oldMain) {
        if (!Strings.isNullOrEmpty(oldMain.getSocksProxy()) || !Strings.isNullOrEmpty(oldMain.getHttpProxy()) || !Strings.isNullOrEmpty(oldMain.getHttpsProxy())) {
            if (!Strings.isNullOrEmpty(oldMain.getSocksProxy()) && (!Strings.isNullOrEmpty(oldMain.getHttpProxy()) || !Strings.isNullOrEmpty(oldMain.getHttpsProxy()))) {
                logAsWarningAndAdd(messages, "Both SOCKS and HTTP(s) proxy are set. Using SOCKS proxy only.");
            } else if (!Strings.isNullOrEmpty(oldMain.getHttpProxy()) && !Strings.isNullOrEmpty(oldMain.getHttpsProxy())) {
                logAsWarningAndAdd(messages, "Both HTTP and HTTPS proxy are set. Using HTTPS proxy for both HTTP and HTTPS");
            }
            String urlString;
            ProxyType proxyType;
            if (!Strings.isNullOrEmpty(oldMain.getSocksProxy())) {
                urlString = oldMain.getSocksProxy();
                proxyType = ProxyType.SOCKS;
            } else if (Strings.isNullOrEmpty(oldMain.getHttpsProxy())) {
                urlString = oldMain.getHttpsProxy();
                proxyType = ProxyType.HTTP;
            } else {
                urlString = oldMain.getHttpProxy();
                proxyType = ProxyType.HTTP;
            }
            try {
                URL url = new URL(urlString);
                newMain.setProxyHost(url.getHost());
                newMain.setProxyPort(url.getPort());
                newMain.setProxyType(proxyType);
                String userInfo = url.getUserInfo();
                if (userInfo != null) {
                    String[] userAndPass = userInfo.split(":");
                    newMain.setProxyUsername(userAndPass[0]);
                    newMain.setProxyPassword(userAndPass[1]);
                }
            } catch (MalformedURLException e) {
                logger.error("Unable to parse old proxy URL " + urlString, e);
                messages.add("Unable to parse old proxy URL " + urlString + ". Error message: " + e.getMessage());
            }

        }
    }

    private List<String> migrateIndexers(OldConfig oldConfig, BaseConfig newConfig) {
        logger.info("Migrating indexers");
        List<String> messages = new ArrayList<>();
        Map<String, Boolean> originalEnabledState = new HashMap<>();
        List<IndexerConfig> indexerConfigs = new ArrayList<>();

        for (Indexer oldIndexer : oldConfig.getIndexers()) {
            logger.info("Migrating indexer {} from config", oldIndexer.getName());
            try {
                if (oldIndexer.getType().toUpperCase().equals("NZBCLUB")) {
                    logAsWarningAndAdd(messages, "NZBClub doesn't exist anymore and will not be migrated");
                    continue;
                }
                IndexerConfig newIndexer = new IndexerConfig();
                newIndexer.setEnabled(oldIndexer.isEnabled());
                originalEnabledState.put(oldIndexer.getName(), oldIndexer.isEnabled());
                newIndexer.setHost(oldIndexer.getHost());
                newIndexer.setTimeout(oldIndexer.getTimeout());
                newIndexer.setDownloadLimit(oldIndexer.getDownloadLimit());
                newIndexer.setHitLimit(oldIndexer.getHitLimit());
                newIndexer.setHitLimitResetTime(oldIndexer.getHitLimitResetTime());
                newIndexer.setName(oldIndexer.getName());
                newIndexer.setApiKey(oldIndexer.getApikey());
                newIndexer.setLoadLimitOnRandom(oldIndexer.getLoadLimitOnRandom());
                newIndexer.setPassword(oldIndexer.getPassword());
                newIndexer.setUsername(oldIndexer.getUsername());
                newIndexer.setUserAgent(oldIndexer.getUserAgent());
                newIndexer.setPreselect(oldIndexer.isPreselect());
                newIndexer.setScore(oldIndexer.getScore());
                if (!Strings.isNullOrEmpty(oldIndexer.getType())) {
                    try {
                        newIndexer.setSearchModuleType(SearchModuleType.valueOf(oldIndexer.getType().toUpperCase().replace("JACKETT", "TORZNAB")));
                    } catch (IllegalArgumentException e) {
                        logger.error("Error migrating indexer", e);
                        logAsWarningAndAdd(messages, "Unable to migrate indexer '" + oldIndexer.getName() + "'. You will need to add it manually.");
                        continue;
                    }
                } else {
                    logger.error("Error migrating indexer: Type is empty");
                    logAsWarningAndAdd(messages, "Unable to migrate indexer '" + oldIndexer.getName() + "'. You will need to add it manually.");
                    continue;
                }
                if (newIndexer.getSearchModuleType() == SearchModuleType.NEWZNAB) {
                    if (!Strings.isNullOrEmpty(oldIndexer.getBackend())) {
                        try {
                            newIndexer.setBackend(BackendType.valueOf(oldIndexer.getBackend().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            logger.error("Error migrating indexer", e);
                            logAsWarningAndAdd(messages, "Unable to migrate indexer '" + oldIndexer.getName() + "'. You will need to add it manually.");
                            continue;
                        }
                    }
                }

                newIndexer.setShowOnSearch(true); //Used to be for womble, we currently don't have indexers which cannot be searched
                newIndexer.setPreselect(oldIndexer.isPreselect());
                List<String> enabledForCategories = new ArrayList<>();
                for (String oldCat : oldIndexer.getCategories()) {

                    Optional<Category> first = categoryProvider.getCategories().stream().filter(x -> x.getName().toLowerCase().replace(" ", "").equals(oldCat.toLowerCase())).findFirst();
                    if (first.isPresent()) {
                        enabledForCategories.add(first.get().getName());
                    } else {
                        logAsWarningAndAdd(messages, "Unable to find category '" + oldCat + "'. Indexer '" + oldIndexer.getName() + "' will not be enabled for it.");
                    }
                }
                newIndexer.setEnabledCategories(enabledForCategories);


                List<IdType> supportedIdTypes = new ArrayList<>();
                for (String s : oldIndexer.getSearchIds()) {
                    try {
                        String correctedSearchId = s.toUpperCase()
                                .replace("TVMAZEID", "TVMAZE")
                                .replace("TVDBID", "TVDB")
                                .replace("TMDBID", "TMDB")
                                .replace("IMDBID", "IMDB")
                                .replace("TRAKTID", "TRAKT")
                                .replace("RID", "TVRAGE");
                        supportedIdTypes.add(IdType.valueOf(correctedSearchId));
                    } catch (IllegalArgumentException e) {
                        logger.error("Error migrating supported search ID", e);
                        logAsWarningAndAdd(messages, "Unable to migrate supported search IDs for indexer '" + oldIndexer.getName() + "'. You should repeat the caps check for it.");
                    }
                }
                newIndexer.setSupportedSearchIds(supportedIdTypes);


                if (oldIndexer.getSearchTypes() != null && !oldIndexer.getSearchTypes().isEmpty()) {
                    newIndexer.setSupportedSearchTypes(new ArrayList<>());
                    for (String s : oldIndexer.getSearchTypes()) {
                        try {
                            newIndexer.getSupportedSearchTypes().add(ActionAttribute.valueOf(s.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            logger.error("Error migrating supported search type", e);
                            logAsWarningAndAdd(messages, "Unable to migrate supported search types for indexer '" + oldIndexer.getName() + "'. You should repeat the caps check for it.");
                        }
                    }

                }
                newIndexer.setCategoryMapping(new IndexerCategoryConfig());
                newIndexer.setGeneralMinSize(oldIndexer.getGeneralMinSize());
                if (!Strings.isNullOrEmpty(oldIndexer.getAccessType())) {
                    try {
                        newIndexer.setEnabledForSearchSource(SearchSourceRestriction.valueOf(oldIndexer.getAccessType().toUpperCase().replace("EXTERNAL", "API")));
                    } catch (IllegalArgumentException e) {
                        logger.error("Error migrating search source restriction", e);
                        logAsWarningAndAdd(messages, "Unable to set 'Enabled for' for '" + oldIndexer.getName() + "'. Setting it to 'Both'.");
                        newIndexer.setEnabledForSearchSource(SearchSourceRestriction.BOTH);
                    }
                }
                if (newIndexer.getSearchModuleType() == SearchModuleType.NEWZNAB || newIndexer.getSearchModuleType() == SearchModuleType.TORZNAB) {
                    newIndexer.setEnabled(false);
                    newIndexer.setConfigComplete(false);
                    logger.info("Adding {} disabled for now because the config is incomplete", newIndexer.getName());
                } else {
                    newIndexer.setConfigComplete(true);
                }

                indexerConfigs.add(newIndexer);
            } catch (Exception e) {
                logger.error("Error migrating indexer", e);
                logAsWarningAndAdd(messages, "Unable to migrate indexer '" + oldIndexer.getName() + "'. You will need to add it manually.");
            }
        }
        checkCapsForEnabledNewznabIndexers(messages, originalEnabledState, indexerConfigs);

        newConfig.setIndexers(indexerConfigs);
        return messages;
    }

    private void checkCapsForEnabledNewznabIndexers(List<String> messages, Map<String, Boolean> originalEnabledState, List<IndexerConfig> indexerConfigs) {
        List<IndexerConfig> enabledNewznabIndexers = indexerConfigs.stream().filter(x -> (x.getSearchModuleType() == SearchModuleType.NEWZNAB || x.getSearchModuleType() == SearchModuleType.TORZNAB) && originalEnabledState.get(x.getName())).collect(Collectors.toList());
        if (!enabledNewznabIndexers.isEmpty()) {
            logger.info("Checking caps and getting category mapping infos for all previously enabled newznab/torznab indexers");
            ExecutorService executor = Executors.newFixedThreadPool(enabledNewznabIndexers.size());
            List<Callable<CheckCapsRespone>> callables = enabledNewznabIndexers.stream().<Callable<CheckCapsRespone>>map(indexerConfig -> () -> newznabChecker.checkCaps(indexerConfig)).collect(Collectors.toList());
            try {
                List<Future<CheckCapsRespone>> futures = executor.invokeAll(callables);
                for (Future<CheckCapsRespone> future : futures) {
                    try {
                        CheckCapsRespone checkCapsRespone = future.get();
                        IndexerConfig indexerConfig = checkCapsRespone.getIndexerConfig();
                        if (checkCapsRespone.isAllChecked()) {
                            logger.info("Successfully checked caps of {}. Setting it enabled now", indexerConfig.getName());
                            indexerConfig.setEnabled(true);
                            indexerConfig.setConfigComplete(true);
                            indexerConfig = checkCapsRespone.getIndexerConfig();
                            enabledNewznabIndexers.set(enabledNewznabIndexers.indexOf(indexerConfig), indexerConfig);
                        } else {
                            logAsWarningAndAdd(messages, "Caps check for " + indexerConfig.getName() + " failed. You'll need to repeat it manually from the config section before you can use the indexer");
                        }
                    } catch (ExecutionException e) {
                        logAsWarningAndAdd(messages, "Caps check for an indexer failed. You'll need to repeat it manually from the config section before you can use the indexer");
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Data
    @AllArgsConstructor
    public static class ConfigMigrationResult {
        private BaseConfig migratedConfig;
        private List<String> messages;
    }

}
