package org.jboss.pnc.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-06.
 */
public class IoUtils {

    public static String readResource(String name, ClassLoader classLoader) throws IOException {
        String configString;InputStream is = classLoader.getResourceAsStream(name);
        try {
            configString = new Scanner(is, Charset.defaultCharset().name()).useDelimiter("\\A").next();
        } finally {
            is.close();
        }
        return configString;
    }

}
