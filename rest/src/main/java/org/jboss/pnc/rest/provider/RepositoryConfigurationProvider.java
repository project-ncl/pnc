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
package org.jboss.pnc.rest.provider;

import org.apache.commons.lang3.StringUtils;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>.
 */
@Stateless
public class RepositoryConfigurationProvider extends AbstractProvider<RepositoryConfiguration, RepositoryConfigurationRest> {

    private static final Pattern REPOSITORY_NAME_PATTERN = Pattern.compile("(\\/[\\w\\.:\\~_-]+)+(\\.git)(?:\\/?|\\#[\\d\\w\\.\\-_]+?)$");

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private ProductVersionRepository productVersionRepository;

    private ScmModuleConfig moduleConfig;

    @Inject
    public RepositoryConfigurationProvider(
            RepositoryConfigurationRepository repositoryConfigurationRepository,
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer,
            Configuration configuration) throws ConfigurationParseException {
        super(repositoryConfigurationRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class));
    }

    // needed for EJB/CDI
    @Deprecated
    public RepositoryConfigurationProvider() {
    }

    public RepositoryConfigurationRest getSpecificByInternalScm(String internalScmUrl) {
        RepositoryConfiguration repositoryConfiguration = repository.queryByPredicates(RepositoryConfigurationPredicates.withInternalScmRepoUrl(internalScmUrl));
        if (repositoryConfiguration != null) {
            return toRESTModel().apply(repositoryConfiguration);
        }
        return null;
    }

    @Override
    protected void validateBeforeSaving(RepositoryConfigurationRest repositoryConfigurationRest) throws ValidationException {
        super.validateBeforeSaving(repositoryConfigurationRest);
        validateInternalRepository(repositoryConfigurationRest.getInternalUrl());
    }

    @Override
    protected void validateBeforeUpdating(Integer id, RepositoryConfigurationRest repositoryConfigurationRest) throws ValidationException {
        super.validateBeforeUpdating(id, repositoryConfigurationRest);
        validateInternalRepository(repositoryConfigurationRest.getInternalUrl());
    }

    public void validateInternalRepository(String internalRepoUrl) throws InvalidEntityException {
        String internalScmAuthority = moduleConfig.getInternalScmAuthority();

        if (StringUtils.isNotBlank(internalRepoUrl) && internalScmAuthority != null) {
            String expectedPrefix = "git+ssh://" + internalScmAuthority;
            if (!internalRepoUrl.startsWith(expectedPrefix)
                    || !REPOSITORY_NAME_PATTERN.matcher(internalRepoUrl.replace(expectedPrefix, "")).matches()) {
                throw new InvalidEntityException("Internal repository url has to start with: " + expectedPrefix + " followed by a repository name.");
            }
        }
    }

    @Override
    protected Function<? super RepositoryConfiguration, ? extends RepositoryConfigurationRest> toRESTModel() {
        return repositoryConfiguration -> new RepositoryConfigurationRest(repositoryConfiguration);
    }

    @Override
    protected Function<? super RepositoryConfigurationRest, ? extends RepositoryConfiguration> toDBModel() {
        return repositoryConfigurationRest -> repositoryConfigurationRest.toDBEntityBuilder().build();
    }

}
