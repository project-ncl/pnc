package org.jboss.pnc.rest.provider;

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * @author Jakub Bartecek
 */
public class RepositoryConfigurationProviderTest {
    private static final int EXISTING_ID = 1243;
    private static final String URL_WITHOUT_SUFFIX = "git+ssh://git@github.com/project-ncl/pnc";
    private static final String VALID_URL = URL_WITHOUT_SUFFIX + ".git";
    private static final String INVALID_URL = "invalid url";

    @Mock
    private RepositoryConfigurationRepository repository;

    @Mock
    private ScmModuleConfig scmModuleConfig;

    @InjectMocks
    private RepositoryConfigurationProvider repositoryConfigurationProvider =
            new RepositoryConfigurationProvider();

    @Before
    public void setUp() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);
        when(repository.queryById(EXISTING_ID)).thenReturn(RepositoryConfiguration.Builder.newBuilder().build());
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git@github.com");
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldFailOnInvalidGitUrl() throws ValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl("git+ssh://git@github.com/");
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldFailOnValidGitUrlWithoutDotGit() throws ValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl(URL_WITHOUT_SUFFIX);
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldSucceedOnUpdateWithLackOfMirrorWithSlash() throws ValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl(INVALID_URL);
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    private RepositoryConfigurationRest createValidConfiguration() {
        RepositoryConfigurationRest repositoryConfigurationRest = new RepositoryConfigurationRest();
        repositoryConfigurationRest.setId(1);
        repositoryConfigurationRest.setInternalUrl(VALID_URL);
        return repositoryConfigurationRest;
    }
}
