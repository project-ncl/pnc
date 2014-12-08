package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;
import org.jboss.util.StringPropertyReplacer;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
public class BuildJobConfig {
    private final RepositoryConnectionInfo connectionInfo;
    private String name;
    private String scmUrl;
    private String buildScript;

    public BuildJobConfig(String name, String scmUrl, String buildScript, RepositoryConnectionInfo connectionInfo) {
        this.name = name;
        this.scmUrl = scmUrl;
        this.buildScript = buildScript;
        this.connectionInfo = connectionInfo;
    }

    public String getName() {
        return name;
    }

    public String getXml() throws BuildDriverException {
        String xmlString = readConfigTemplate();

        Properties properties = new Properties();
        properties.setProperty("scm_url", scmUrl);
        
        //properties.setProperty("maven_settings", getMavenConfig(connectionInfo.getDependencyUrl()));
        //properties.setProperty("hudson.tasks.Shell.command", buildScript + " -s settings.xml");
        properties.setProperty("hudson.tasks.Shell.command", buildScript + "");

        return StringPropertyReplacer.replaceProperties(xmlString, properties);
    }

    private String getMavenConfig(String dependencyUrl) {
        String config = "printf \"<settings><mirrors><mirror><id>pnc-aprox</id><mirrorOf>*</mirrorOf><url>" + dependencyUrl +"</url></mirror></mirrors></settings>\" > settings.xml";
        return config.replace("<", "&lt;")
              .replace(">", "&gt;");

    }

    private String readConfigTemplate() throws BuildDriverException {
            try {
                return IoUtils.readFileOrResource("jenkins-job-template", "jenkins-job-template.xml", getClass().getClassLoader());
            } catch (IOException e) {
                throw new BuildDriverException("Cannot load config template.", e);
            }
    }

}
