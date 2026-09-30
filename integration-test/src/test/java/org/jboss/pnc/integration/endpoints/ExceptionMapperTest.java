/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.endpoints;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.ws.rs.NotAuthorizedException;

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

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ExceptionMapperTest {

    public static final Logger logger = LoggerFactory.getLogger(ExceptionMapperTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    @InSequence(1)
    public void shouldFailWithNotAuthorizedException() {
        BuildConfigurationClient client = new BuildConfigurationClient(RestClientConfiguration.asAnonymous());
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
