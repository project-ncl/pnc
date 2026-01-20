/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-01.
 */
public class StringUtils {

    private static final String DELIMITER = ";";

    /**
     * Replace environment variables in string. Environment variables are expected in format ${env.ENV_PROPERTY}, where
     * "env" is static prefix and ENV_PROPERTY is name of environment property.
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
        // remove env placeholders that were not replaced with values
        return replaced.replaceAll("\\$\\{env\\..+\\}", "");
    }

    /**
     * Check if the given string is null or contains only whitespace characters.
     *
     * @param string String to check for non-whitespace characters
     * @return boolean True if the string is null, empty, or contains only whitespace (empty when trimmed). Otherwise
     *         return false.
     */
    public static boolean isEmpty(String string) {
        if (string == null) {
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
            string = string.substring(1);
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
        if (string == null) {
            return null;
        }
        if (suffix == null) {
            return string;
        }

        if (string.endsWith(suffix)) {
            return string.substring(0, string.length() - suffix.length());
        } else {
            return string;
        }
    }

    public static String stripProtocol(String url) {
        if (url == null) {
            return null;
        }

        String protocolDivider = "://";
        int protocolDividerIndex = url.indexOf(protocolDivider);

        if (protocolDividerIndex > -1) {
            return url.substring(protocolDividerIndex + protocolDivider.length());
        } else {
            return url;
        }
    }

    public static void readStream(
            InputStream inputStream,
            Charset charset,
            ArrayDeque<String> lines,
            int maxMessageSize,
            Consumer<String> droppedLinesConsumer) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, charset);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        int messageSize = 0;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (maxMessageSize > -1) {
                messageSize += line.length();
                while (messageSize > maxMessageSize) {
                    String removedLine = lines.removeFirst();
                    messageSize -= removedLine.length();
                    droppedLinesConsumer.accept(removedLine);
                }
            }
            lines.add(line);
        }
    }

    /**
     * Parse comma separated string to Integer array.
     * 
     * @return An empty array when the string parameter is empty or null.
     */
    public static Integer[] deserializeInt(String string) {
        if (string == null) {
            return new Integer[0];
        }
        return Arrays.stream(string.split(","))
                .filter(s -> !s.equals(""))
                .map(Integer::parseInt)
                .toArray(Integer[]::new);
    }

    /**
     * Parse comma separated string to Long array.
     *
     * @return An empty array when the string parameter is empty or null.
     */
    public static Long[] deserializeLong(String string) {
        if (string == null) {
            return new Long[0];
        }
        return Arrays.stream(string.split(",")).filter(s -> !s.equals("")).map(Long::parseLong).toArray(Long[]::new);
    }

    /**
     * Serialize Integer array to comma separated string.
     * 
     * @return An empty string when the Integer array parameter is empty or null.
     */
    public static String serializeInt(Integer[] integers) {
        if (integers == null) {
            return "";
        }
        return Arrays.stream(integers).map(i -> Integer.toString(i)).collect(Collectors.joining(","));
    }

    public static String serializeLong(Long[] longs) {
        if (longs == null) {
            return "";
        }
        return Arrays.stream(longs).map(i -> Long.toString(i)).collect(Collectors.joining(","));
    }

    public static Integer parseInt(String s, int defaultValue) {
        if (isEmpty(s)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String firstCharToLowerCase(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toLowerCase(c[0]);
        return new String(c);
    }

    public static String firstCharToUpperCase(String string) {
        char[] c = string.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }

    public static String nullIfBlank(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        return string;
    }

    public static String joinArray(Collection<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(DELIMITER);
        strings.forEach(joiner::add);

        return joiner.toString();
    }

    public static List<String> splitString(String myString) {
        if (myString == null || myString.isEmpty()) {
            return new ArrayList<String>();
        }

        String[] elements = myString.split(DELIMITER);
        return new ArrayList<String>(Arrays.asList(elements));
    }

    public static URI stripToken(URI uri) {
        if (uri.toString().contains("token=")) {
            if (uri.toString().matches(".*builds\\/[^\\/]+\\/scm-archive\\?token=[^&\\/]+")) {
                try {
                    return new URI(uri.toString().split("\\?token")[0]);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return uri;
    }

}
