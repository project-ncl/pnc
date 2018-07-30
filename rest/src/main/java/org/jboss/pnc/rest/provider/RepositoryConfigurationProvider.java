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
import org.jboss.pnc.common.util.UrlUtils;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

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

    private static Logger logger = LoggerFactory.getLogger(RepositoryConfigurationProvider.class);

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
        RepositoryConfiguration repositoryConfiguration = repository.queryByPredicates(RepositoryConfigurationPredicates.withExactInternalScmRepoUrl(internalScmUrl));
        if (repositoryConfiguration != null) {
            return toRESTModel().apply(repositoryConfiguration);
        }
        return null;
    }

    @Override
    protected void validateBeforeSaving(RepositoryConfigurationRest repositoryConfigurationRest) throws RestValidationException {
        super.validateBeforeSaving(repositoryConfigurationRest);
        validateInternalRepository(repositoryConfigurationRest.getInternalUrl());
        validateIfItsNotConflicting(repositoryConfigurationRest);
    }

    @Override
    protected void validateBeforeUpdating(Integer id, RepositoryConfigurationRest repositoryConfigurationRest) throws
            RestValidationException {
        super.validateBeforeUpdating(id, repositoryConfigurationRest);
        validateInternalRepository(repositoryConfigurationRest.getInternalUrl());
    }

    public void validateInternalRepository(String internalRepoUrl) throws InvalidEntityException {
        String internalScmAuthority = moduleConfig.getInternalScmAuthority();
        if (!isInternalRepository(internalScmAuthority, internalRepoUrl)) {
            logger.warn("Invalid internal repo url: " + internalRepoUrl);
            throw new InvalidEntityException("Internal repository url has to start with: <protocol>://" + internalScmAuthority + " followed by a repository name or match the pattern: " + REPOSITORY_NAME_PATTERN);
        }

    }

    public static Boolean isInternalRepository(String internalScmAuthority, String internalRepoUrl) {
        if (StringUtils.isBlank(internalRepoUrl) || internalScmAuthority == null) {
            throw new RuntimeException("InternalScmAuthority and internalRepoUrl parameters must be set.");
        }
        String internalRepoUrlNoProto = UrlUtils.stripProtocol(internalRepoUrl);
        return internalRepoUrlNoProto.startsWith(internalScmAuthority)
                && REPOSITORY_NAME_PATTERN.matcher(internalRepoUrlNoProto.replace(internalScmAuthority, "")).matches();
    }

    private void validateIfItsNotConflicting(RepositoryConfigurationRest repositoryConfigurationRest) throws ConflictedEntryException {
        RepositoryConfiguration existingRepositoryConfiguration =
                repository.queryByPredicates(RepositoryConfigurationPredicates.withExactInternalScmRepoUrl(repositoryConfigurationRest.getInternalUrl()));
        if(existingRepositoryConfiguration != null)
            throw new ConflictedEntryException("RepositoryConfiguration with specified internalURL already exists",
                    RepositoryConfiguration.class, existingRepositoryConfiguration.getId());

    }

    public CollectionInfo<RepositoryConfigurationRest> searchByScmUrl(int pageIndex, int pageSize, String sortingRsql, String scmUrl) {
        Predicate<RepositoryConfiguration> predicate = RepositoryConfigurationPredicates.searchByScmUrl(scmUrl);

        return getRepositoryConfigurationRestCollectionInfo(pageIndex, pageSize, sortingRsql, predicate);
    }

    public CollectionInfo<RepositoryConfigurationRest> matchByScmUrl(int pageIndex, int pageSize, String sortingRsql, String scmUrl) {
        Predicate<RepositoryConfiguration> predicate = RepositoryConfigurationPredicates.matchByScmUrl(scmUrl);

        return getRepositoryConfigurationRestCollectionInfo(pageIndex, pageSize, sortingRsql, predicate);
    }

    private CollectionInfo<RepositoryConfigurationRest> getRepositoryConfigurationRestCollectionInfo(int pageIndex, int pageSize, String sortingRsql, Predicate<RepositoryConfiguration> predicate) {
        List<RepositoryConfiguration> collection = repository.queryWithPredicates(
                pageInfoProducer.getPageInfo(pageIndex, pageSize),
                sortInfoProducer.getSortInfo(sortingRsql),
                predicate);

        int totalPages = (repository.count(predicate) + pageSize - 1) / pageSize;

        return nullableStreamOf(collection)
                .map(toRESTModel())
                .collect(new CollectionInfoCollector<>(pageIndex, pageSize, totalPages));
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
