/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.integration.template;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.util.StringPropertyReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

public class JsonTemplateBuilder {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String template;
    private Properties templateValues = new Properties();

    public JsonTemplateBuilder() {

    }

    public static JsonTemplateBuilder fromResource(String resourceName) throws IOException {
        JsonTemplateBuilder jsonTemplateBuilder = new JsonTemplateBuilder();
        jsonTemplateBuilder.template = IoUtils.readFileOrResource(
                resourceName,
                resourceName + ".json",
                MethodHandles.lookup().lookupClass().getClassLoader());
        return jsonTemplateBuilder;
    }

    public JsonTemplateBuilder addValue(String key, String value) {
        templateValues.put(key, value);
        return this;
    }

    public String fillTemplate() {
        String filledTemplate = StringPropertyReplacer.replaceProperties(template, templateValues);
        logger.debug("Filled template: {}", filledTemplate);
        return filledTemplate;
    }
}
