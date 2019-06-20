/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration_new;

import org.assertj.core.api.Assertions;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.patch.BuildConfigurationPatchBuilder;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildConfigurationEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldPatchBuildConfiguration() throws ClientException, PatchBuilderException {
        BuildConfigurationClient client = new BuildConfigurationClient(
                RestClientConfiguration.getConfiguration(RestClientConfiguration.AuthenticateAs.USER));

        BuildConfiguration buildConfiguration = client.getAll().iterator().next();
        String newDescription = "Testing patch support.";

        Integer id = buildConfiguration.getId();

        Map<String, String> addElements = Collections.singletonMap("newKey", "newValue");
        BuildConfigurationPatchBuilder builder = new BuildConfigurationPatchBuilder()
                .replaceDescription(newDescription)
                .addGenericParameters(addElements);
        BuildConfiguration updated = client.patch(id, builder);

        Assert.assertEquals(newDescription, updated.getDescription());
        Assertions.assertThat(updated.getGenericParameters()).contains(addElements.entrySet().toArray(new Map.Entry[1]));

        String newDescription2 = "Testing patch support 2.";
        BuildConfigurationPatchBuilder builder2 = new BuildConfigurationPatchBuilder()
                .replaceDescription(newDescription2);
        BuildConfiguration updated2 = client.patch(id, builder2.getJsonPatch(), BuildConfiguration.class);
        Assert.assertEquals(newDescription2, updated2.getDescription());
    }

}
