package org.jboss.pnc.integration.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AuthResource {
    
    public static boolean authEnabled() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream("auth.properties");
        Properties properties = new Properties();
        properties.load(stream);
        String value = properties.getProperty("authentication.test.enabled");
        return value.equals("true") ? true : false;
       }

}
