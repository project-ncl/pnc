/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common.util;

import org.jboss.util.StringPropertyReplacer;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-01.
 */
public class StringUtils {
    
    /**
     * Replace environment variables in string.
     * Environment variables are expected in format ${env.ENV_PROPERTY}, 
     * where "env" is static prefix and ENV_PROPERTY is name of environment property.
     * 
     * @param configString String with environment variables
     * @return String with replaced environment variables
     */
    public static String replaceEnv(String configString) {
        Properties properties = new Properties();
        
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            properties.put("env." + entry.getKey(), entry.getValue());
        }
        String replaced = StringPropertyReplacer.replaceProperties(configString, properties);
        //remove env placeholders that were not replaced with values
        return replaced.replaceAll("\\$\\{env\\..+\\}", "");
    }

    /**
     * Check if the given string is null or contains only whitespace characters.
     * 
     * @param string String to check for non-whitespace characters
     * @return boolean True if the string is null, empty, or contains only whitespace (empty when trimmed).  
     * Otherwise return false.
     */
    public static boolean isEmpty(String string) {
        if (string == null ) {
            return true;
        }
        return string.trim().isEmpty();
    }

    public static String toCVS(Set<Integer> buildRecordSetIds) {
        StringJoiner joiner = new StringJoiner(",");
        buildRecordSetIds.forEach(el -> joiner.add(el.toString()));
        return joiner.toString();
    }

    /**
     * Remove ending slash if present and return the string without ending slash
     *
     * @param string
     * @return
     */
    public static String stripEndingSlash(String string) {
        if (string == null) {
            return null;
        }
        if (string.endsWith("/")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     * Remove ending slash if present and return the string without ending slash
     *
     * @param string
     * @return
     */
    public static String stripTrailingSlash(String string) {
        if (string == null) {
            return null;
        }
        if (string.startsWith("/")) {
            string = string.substring(1, string.length());
        }
        return string;
    }

    /**
     * Adds ending slash if it is not present.
     *
     * @param string
     * @return
     */
    public static String addEndingSlash(String string) {
        if (string == null) {
            return null;
        }
        if (!string.endsWith("/")) {
            string += "/";
        }
        return string;
    }

    public static String trim(String string, int maxLength) {
        if (string == null) {
            return null;
        }

        if (string.length() > maxLength) {
            return string.substring(0, maxLength - 1) + "...";
        } else {
            return string;
        }
    }

    public static String stripSuffix(String string, String suffix) {
        if (string.endsWith(suffix)) {
            return string.substring(string.length() - suffix.length());
        } else {
            return string;
        }
    }

    public static String stripProtocol(String url) {
        String protocolDivider = "://";
        int protocolDividerIndex = url.indexOf("://");

        if (protocolDividerIndex > -1) {
            return url.substring(protocolDividerIndex + protocolDivider.length());
        } else {
            return url;
        }
    }
}
