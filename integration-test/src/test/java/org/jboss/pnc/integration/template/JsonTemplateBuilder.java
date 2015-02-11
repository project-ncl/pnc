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
        jsonTemplateBuilder.template = IoUtils.readFileOrResource(resourceName, resourceName + ".json",
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
