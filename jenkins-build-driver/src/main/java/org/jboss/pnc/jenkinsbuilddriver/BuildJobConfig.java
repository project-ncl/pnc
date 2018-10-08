/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.jenkinsbuilddriver;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.util.StringPropertyReplacer;

import java.io.IOException;
import java.util.Properties;

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
                //return IoUtils.readFileOrResource("jenkins-job-template", "jenkins-job-template.xml", getClass().getClassLoader());
                return IoUtils.readFileOrResource("jenkins-job-template", "freeform-job-template.xml", getClass().getClassLoader());
            } catch (IOException e) {
                throw new BuildDriverException("Cannot load config template.", e);
            }
    }

}
