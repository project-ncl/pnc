package org.jboss.pnc.rest.provider.api;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.model.BuildConfigurationRest;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface BuildConfigurationProvider extends Provider<BuildConfiguration, BuildConfigurationRest> {

    void addDependency(Integer configId, Integer dependencyId) throws RestValidationException;

    CollectionInfo<BuildConfigurationRest> getAllForBuildConfigurationSet(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildConfigurationSetId);

    CollectionInfo<BuildConfigurationRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query, Integer productId);

    CollectionInfo<BuildConfigurationRest> getAllForProductAndProductVersion(int pageIndex, int pageSize, String sortingRsql, String query, Integer productId, Integer versionId);

    CollectionInfo<BuildConfigurationRest> getAllForProject(Integer pageIndex, Integer pageSize, String sortingRsql, String query, Integer projectId);

    CollectionInfo<BuildConfigurationRest> getAllNonArchived(Integer pageIndex, Integer pageSize, String sortingRsql, String query);

    CollectionInfo<BuildConfigurationRest> getDependencies(int pageIndex, int pageSize, String sortingRsql, String query, Integer configId);

    void archive(Integer buildConfigurationId) throws RestValidationException;

    Integer clone(Integer buildConfigurationId) throws RestValidationException;

    void removeDependency(Integer configId, Integer dependencyId) throws RestValidationException;

}
