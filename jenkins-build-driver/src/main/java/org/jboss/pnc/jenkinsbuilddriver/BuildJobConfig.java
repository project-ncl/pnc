package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.util.StringPropertyReplacer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
public class BuildJobConfig {
    private String name;
    private String scmUrl;
    private String buildScript = "mvn clean install"; //TODO

    public BuildJobConfig(String name, String scmUrl, String buildScript) {
        this.name = name;
        this.scmUrl = scmUrl;
        this.buildScript = buildScript;
    }

    public String getName() {
        return name;
    }

    public String getXml() throws BuildDriverException {
        String xmlString = readConfigTemplate();

        Properties properties = new Properties();
        properties.setProperty("scm_url", scmUrl);
        if (buildScript != null) {
            properties.setProperty("hudson.tasks.Shell.buildScript", buildScript);
        }

        return StringPropertyReplacer.replaceProperties(xmlString, properties);
    }

    private String readConfigTemplate() throws BuildDriverException {

        String templateFileName = System.getProperty("jenkins-job-template");

        File file = null;
        if (templateFileName == null) {
            templateFileName = "jenkins-job-template.xml";
        }

        file = new File(templateFileName); //try full path

        if (!file.exists()) {
            URL url = getClass().getClassLoader().getResource(templateFileName);
            if (url != null) {
                file = new File(url.getFile());
            }
        }

        if (!file.exists()) {
            throw new BuildDriverException("Cannot load jenkins-job-template.");
        }

        byte[] encoded;
        try {
            encoded = Files.readAllBytes(Paths.get(file.toURI()));
        } catch (IOException e) {
            throw new BuildDriverException("Cannot load jenkins-job-template.", e);
        }
        return new String(encoded, Charset.defaultCharset());

    }

}
