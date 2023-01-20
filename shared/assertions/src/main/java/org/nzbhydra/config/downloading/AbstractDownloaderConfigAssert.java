package org.nzbhydra.config.downloading;

import org.assertj.core.api.AbstractObjectAssert;
import org.assertj.core.util.Objects;

/**
 * Abstract base class for {@link DownloaderConfig} specific assertions - Generated by CustomAssertionGenerator.
 */
@jakarta.annotation.Generated(value = "assertj-assertions-generator")
public abstract class AbstractDownloaderConfigAssert<S extends AbstractDownloaderConfigAssert<S, A>, A extends DownloaderConfig> extends AbstractObjectAssert<S, A> {

    /**
     * Creates a new <code>{@link AbstractDownloaderConfigAssert}</code> to make assertions on actual DownloaderConfig.
     *
     * @param actual the DownloaderConfig we want to make assertions on.
     */
    protected AbstractDownloaderConfigAssert(A actual, Class<S> selfType) {
        super(actual, selfType);
    }

    /**
     * Verifies that the actual DownloaderConfig is add paused.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig is not add paused.
     */
    public S isAddPaused() {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // check that property call/field access is true
        if (!actual.isAddPaused()) {
            failWithMessage("\nExpecting that actual DownloaderConfig is add paused but is not.");
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig is not add paused.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig is add paused.
     */
    public S isNotAddPaused() {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // check that property call/field access is false
        if (actual.isAddPaused()) {
            failWithMessage("\nExpecting that actual DownloaderConfig is not add paused but is.");
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's apiKey is equal to the given one.
     *
     * @param apiKey the given apiKey to compare the actual DownloaderConfig's apiKey to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's apiKey is not equal to the given one.
     */
    public S hasApiKey(String apiKey) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting apiKey of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualApiKey = actual.getApiKey();
        if (!Objects.areEqual(actualApiKey, apiKey)) {
            failWithMessage(assertjErrorMessage, actual, apiKey, actualApiKey);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's defaultCategory is equal to the given one.
     *
     * @param defaultCategory the given defaultCategory to compare the actual DownloaderConfig's defaultCategory to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's defaultCategory is not equal to the given one.
     */
    public S hasDefaultCategory(String defaultCategory) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting defaultCategory of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualDefaultCategory = actual.getDefaultCategory();
        if (!Objects.areEqual(actualDefaultCategory, defaultCategory)) {
            failWithMessage(assertjErrorMessage, actual, defaultCategory, actualDefaultCategory);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's downloadType is equal to the given one.
     *
     * @param downloadType the given downloadType to compare the actual DownloaderConfig's downloadType to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's downloadType is not equal to the given one.
     */
    public S hasDownloadType(DownloadType downloadType) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting downloadType of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        DownloadType actualDownloadType = actual.getDownloadType();
        if (!Objects.areEqual(actualDownloadType, downloadType)) {
            failWithMessage(assertjErrorMessage, actual, downloadType, actualDownloadType);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's downloaderType is equal to the given one.
     *
     * @param downloaderType the given downloaderType to compare the actual DownloaderConfig's downloaderType to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's downloaderType is not equal to the given one.
     */
    public S hasDownloaderType(org.nzbhydra.downloading.DownloaderType downloaderType) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting downloaderType of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        org.nzbhydra.downloading.DownloaderType actualDownloaderType = actual.getDownloaderType();
        if (!Objects.areEqual(actualDownloaderType, downloaderType)) {
            failWithMessage(assertjErrorMessage, actual, downloaderType, actualDownloaderType);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig is enabled.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig is not enabled.
     */
    public S isEnabled() {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // check that property call/field access is true
        if (!actual.isEnabled()) {
            failWithMessage("\nExpecting that actual DownloaderConfig is enabled but is not.");
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig is not enabled.
     *
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig is enabled.
     */
    public S isNotEnabled() {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // check that property call/field access is false
        if (actual.isEnabled()) {
            failWithMessage("\nExpecting that actual DownloaderConfig is not enabled but is.");
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's iconCssClass is equal to the given one.
     *
     * @param iconCssClass the given iconCssClass to compare the actual DownloaderConfig's iconCssClass to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's iconCssClass is not equal to the given one.
     */
    public S hasIconCssClass(String iconCssClass) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting iconCssClass of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualIconCssClass = actual.getIconCssClass();
        if (!Objects.areEqual(actualIconCssClass, iconCssClass)) {
            failWithMessage(assertjErrorMessage, actual, iconCssClass, actualIconCssClass);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's name is equal to the given one.
     *
     * @param name the given name to compare the actual DownloaderConfig's name to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's name is not equal to the given one.
     */
    public S hasName(String name) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting name of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualName = actual.getName();
        if (!Objects.areEqual(actualName, name)) {
            failWithMessage(assertjErrorMessage, actual, name, actualName);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's nzbAddingType is equal to the given one.
     *
     * @param nzbAddingType the given nzbAddingType to compare the actual DownloaderConfig's nzbAddingType to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's nzbAddingType is not equal to the given one.
     */
    public S hasNzbAddingType(NzbAddingType nzbAddingType) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting nzbAddingType of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        NzbAddingType actualNzbAddingType = actual.getNzbAddingType();
        if (!Objects.areEqual(actualNzbAddingType, nzbAddingType)) {
            failWithMessage(assertjErrorMessage, actual, nzbAddingType, actualNzbAddingType);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's password is equal to the given one.
     *
     * @param password the given password to compare the actual DownloaderConfig's password to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's password is not equal to the given one.
     */
    public S hasPassword(java.util.Optional password) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting password of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.Optional actualPassword = actual.getPassword();
        if (!Objects.areEqual(actualPassword, password)) {
            failWithMessage(assertjErrorMessage, actual, password, actualPassword);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's url is equal to the given one.
     *
     * @param url the given url to compare the actual DownloaderConfig's url to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's url is not equal to the given one.
     */
    public S hasUrl(String url) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting url of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        String actualUrl = actual.getUrl();
        if (!Objects.areEqual(actualUrl, url)) {
            failWithMessage(assertjErrorMessage, actual, url, actualUrl);
        }

        // return the current assertion for method chaining
        return myself;
    }

    /**
     * Verifies that the actual DownloaderConfig's username is equal to the given one.
     *
     * @param username the given username to compare the actual DownloaderConfig's username to.
     * @return this assertion object.
     * @throws AssertionError - if the actual DownloaderConfig's username is not equal to the given one.
     */
    public S hasUsername(java.util.Optional username) {
        // check that actual DownloaderConfig we want to make assertions on is not null.
        isNotNull();

        // overrides the default error message with a more explicit one
        String assertjErrorMessage = "\nExpecting username of:\n  <%s>\nto be:\n  <%s>\nbut was:\n  <%s>";

        // null safe check
        java.util.Optional actualUsername = actual.getUsername();
        if (!Objects.areEqual(actualUsername, username)) {
            failWithMessage(assertjErrorMessage, actual, username, actualUsername);
        }

        // return the current assertion for method chaining
        return myself;
    }

}