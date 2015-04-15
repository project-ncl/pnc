package org.jboss.pnc.common.util;

import org.jboss.util.StringPropertyReplacer;

import java.util.Map;
import java.util.Properties;

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
        return StringPropertyReplacer.replaceProperties(configString, properties);
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
}
