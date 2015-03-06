package org.jboss.pnc.jenkinsbuilddriver;

import java.io.IOException;
import java.util.Properties;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.util.StringPropertyReplacer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-29.
 */
class BuildJobConfig {
    private String name;
    private String scmUrl;
    private String scmBranch;
    private String buildScript;

    public BuildJobConfig(String name, String scmUrl, String scmBranch, String buildScript) {
        this.name = name;
        this.scmUrl = scmUrl;
        this.buildScript = buildScript;
        if (scmBranch != null && !scmBranch.equals("")) {
            this.scmBranch = scmBranch;
        } else{
            this.scmBranch = "*/master";
        }
    }

    public String getName() {
        return name;
    }

    public String getXml() throws BuildDriverException {
        String xmlString = readConfigTemplate();

        Properties properties = new Properties();
        properties.setProperty("scm_url", scmUrl);
        properties.setProperty("scm_branch", scmBranch);

        properties.setProperty("hudson.tasks.Shell.command", buildScript);

        return StringPropertyReplacer.replaceProperties(xmlString, properties);
    }

    private String readConfigTemplate() throws BuildDriverException {
            try {
                return IoUtils.readFileOrResource("jenkins-job-template", "jenkins-job-template.xml", getClass().getClassLoader());
            } catch (IOException e) {
                throw new BuildDriverException("Cannot load config template.", e);
            }
    }

}
