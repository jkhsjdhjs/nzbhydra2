/*
 *  (C) Copyright 2023 TheOtherP (theotherp@posteo.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.nzbhydra;

import org.nzbhydra.config.migration.ConfigMigrationStep003to004;
import org.nzbhydra.downloading.downloaders.sabnzbd.mapping.QueueResponse;
import org.nzbhydra.mapping.newznab.NewznabParameters;
import org.nzbhydra.news.NewsWeb;
import org.nzbhydra.springnative.ReflectionMarker;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.lang.reflect.Method;
import java.util.Set;

public class NativeHints implements RuntimeHintsRegistrar {

    private static final Logger logger = LoggerFactory.getLogger(NativeHints.class);


    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        logger.info("Registering native hints");
        hints.reflection().registerType(NewsWeb.NewsEntryForWeb.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(NewznabParameters.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(QueueResponse.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        hints.reflection().registerType(ConfigMigrationStep003to004.class, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
        hints.resources().registerResourceBundle("joptsimple.ExceptionMessages");
        final Set<Class<?>> classes = new Reflections("org.nzbhydra", Scanners.TypesAnnotated).getTypesAnnotatedWith(ReflectionMarker.class);
        for (Class<?> clazz : classes) {
            hints.reflection().registerType(clazz, MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS);
            for (Method method : clazz.getDeclaredMethods()) {
                logger.info("Registering " + method + " for reflection");
                hints.reflection().registerMethod(method, ExecutableMode.INVOKE);
            }
        }
    }

}
