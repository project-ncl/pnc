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
package org.jboss.pnc.client;

import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.junit.Test;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class QueryAndSortTest {

    @Test
    public void testShouldCompile() throws Exception {
        if (true)
            return; // test never run, only to make sure the client classes are properly generated

        Configuration configuration = Configuration.builder().build();
        ProjectClient projectClient = new ProjectClient(configuration);
        projectClient.getAll();
        projectClient.getAll(Optional.of("asc=id"), Optional.empty());

        BuildClient buildClient = new BuildClient(configuration);
        BuildPushParameters pushRequest = BuildPushParameters.builder().build();
        buildClient.push("", pushRequest);
    }
}
