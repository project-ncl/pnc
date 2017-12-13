/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.coordinator;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;

import javax.enterprise.context.ApplicationScoped;

/**
 * Bean providing an interface to delete temporary builds
 *
 * @author Jakub Bartecek
 */
@ApplicationScoped
public class TemporaryBuildsCleaner {

    public void deleteTemporaryBuilds(BuildRecord buildRecord) {
        // TODO

    }

    public void deleteTemporaryBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        // TODO
    }
}
