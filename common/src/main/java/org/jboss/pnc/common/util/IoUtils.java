package org.jboss.pnc.common.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    /**
     *
     * @param systemPropertyName Name of system property to override default file name
     * @param defaultFileName
     * @param classLoader
     * @return
     * @throws IOException
     */
    public static String readFileOrResource(String systemPropertyName, String defaultFileName, ClassLoader classLoader) throws IOException {

        String templateFileName = System.getProperty(systemPropertyName);

        if (templateFileName == null) {
            templateFileName = defaultFileName;
        }

        File file = new File(templateFileName); //try full path

        String configString;
        if (file.exists()) {
            try {
                byte[] encoded;
                encoded = Files.readAllBytes(Paths.get(file.getPath()));
                configString = new String(encoded, Charset.defaultCharset());
            } catch (IOException e) {
                throw new IOException("Cannot load " + templateFileName + ".", e);
            }
        } else {
            try {
                configString = IoUtils.readResource(templateFileName, classLoader);
            } catch (IOException e) {
                throw new IOException("Cannot load " + templateFileName + ".", e);
            }
        }

        if (configString == null) {
            throw new IOException("Cannot load " + templateFileName + ".");
        }

        return configString;
    }
}
