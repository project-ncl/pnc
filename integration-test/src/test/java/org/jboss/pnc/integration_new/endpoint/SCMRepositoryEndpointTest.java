package org.jboss.pnc.integration_new.endpoint;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.integration_new.setup.Deployments;
import org.jboss.pnc.integration_new.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration_new.setup.RestClientConfiguration.AuthenticateAs.USER;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class SCMRepositoryEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(SCMRepositoryEndpointTest.class);

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void testGetBuildConfigurationForARepository() throws ClientException {
        SCMRepositoryClient repositoryClient = new SCMRepositoryClient(RestClientConfiguration.getConfiguration(USER));
        BuildConfigurationClient buildConfigurationClient = new BuildConfigurationClient(RestClientConfiguration.getConfiguration(USER));
        SCMRepository scmRepository = repositoryClient.getAll(null,null).iterator().next();

        Iterator<BuildConfiguration> allConfigsIterator = buildConfigurationClient.getAll().iterator();

        BuildConfiguration buildConfiguration1 = allConfigsIterator.next().toBuilder().scmRepository(scmRepository).build();
        BuildConfiguration buildConfiguration2 = allConfigsIterator.next().toBuilder().scmRepository(scmRepository).build();

        buildConfigurationClient.update(buildConfiguration1.getId(), buildConfiguration1);
        buildConfigurationClient.update(buildConfiguration2.getId(), buildConfiguration2);

        RemoteCollection<BuildConfiguration> buildConfigs = repositoryClient.getBuildsConfigs(scmRepository.getId());

        assertThat(buildConfigs).usingElementComparatorIgnoringFields("modificationTime")
                .contains(buildConfiguration1,buildConfiguration2)
                .allSatisfy((bc -> scmRepository.equals(bc.getScmRepository())));
    }
}