package org.jboss.pnc.common.util;

import org.jboss.util.StringPropertyReplacer;

import java.util.Map;
import java.util.Properties;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-01-01.
 */
public class StringUtils {
    public static String replaceEnv(String configString) {
        Properties properties = new Properties();
        for (Map.Entry entry : System.getenv().entrySet()) {
            properties.put("env." + entry.getKey(), entry.getValue());
        }
        return StringPropertyReplacer.replaceProperties(configString, properties);
    }
}
