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
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotAuthorizedException;
import java.lang.invoke.MethodHandles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:dbrazdil@redhat.com">Dominik Brazdil</a>
 *
 * @see org.jboss.pnc.demo.data.DatabaseDataInitializer
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ExceptionMapperTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    @InSequence(1)
    public void shouldFailWith() throws InterruptedException {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());
        // BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asUser());
        assertThatThrownBy(() -> {
            try {
                client.createNew(BuildConfiguration.builder().build());
            } catch (Throwable th) {
                logger.debug("Received exception: {}", th);
                throw th;
            }
            ;
        }).hasCauseInstanceOf(NotAuthorizedException.class); // 401

    }
}
