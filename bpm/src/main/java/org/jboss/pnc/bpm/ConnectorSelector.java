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
package org.jboss.pnc.bpm;

import org.jboss.pnc.bpm.task.BpmBuildTask;

import java.util.Map;

/**
 * @author Matej Lazar
 */
public class ConnectorSelector {

    public static final String GENERIC_PARAMETER_KEY = "BPM_SERVER";
    public static final String RHPAM = "RH-PAM";

    public static boolean useNewProcess(BpmTask task, boolean forceNew) {
        if (task instanceof BpmBuildTask) {
            BpmBuildTask buildTask = (BpmBuildTask) task;
            Map<String, String> genericParameters = buildTask.getBuildTask()
                    .getBuildConfigurationAudited()
                    .getGenericParameters();
            return useNewProcessForBuild(genericParameters, forceNew);
        }
        return false;
    }

    public static boolean useNewProcessForBuild(Map<String, String> genericParameters, boolean forceNew) {
        return forceNew || genericParameters.getOrDefault(GENERIC_PARAMETER_KEY, "").equals(RHPAM);
    }

}
