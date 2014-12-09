package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Implementation of {@link RepositoryManager} that manages an <a href="https://github.com/jdcasey/aprox">AProx</a> instance to
 * support repositories for Maven-ish builds.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
@ApplicationScoped
public class RepositoryManagerDriver implements RepositoryManager {

    private static final String MAVEN_REPOSITORY_CONFIG_SECTION = "maven-repository";

    private static final String BASE_URL_PROPERTY = "base.url";

    private static final String REPO_ID_FORMAT = "build+%s+%s+%s+%s";

    @Inject
    Configuration configuration;

    ExecutorService executor;

    public RepositoryManagerDriver() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, workQueue);//TODO configurable
    }

    public RepositoryManagerDriver(Configuration configuration) {
        this();
        this.configuration = configuration;
    }

    /**
     * Only supports {@link RepositoryType#MAVEN}.
     */
    @Override
    public boolean canManage(RepositoryType managerType) {
        return (managerType == RepositoryType.MAVEN);
    }

    /**
     * Currently, we're using the AutoProx add-on of AProx. This add-on supports a series of rules (implemented as groovy
     * scripts), which match repository/group naming patterns and create remote repositories, hosted repositories, and groups in
     * flexible configurations on demand. Because these rules will create the necessary repositories the first time they are
     * accessed, this driver only has to formulate a repository URL that will trigger the appropriate AutoProx rule, then pass
     * this back via a {@link MavenRepositoryConfiguration} instance.
     */
    @Override
    public void createRepository(ProjectBuildConfiguration projectBuildConfiguration,
            BuildCollection buildCollection,
            Consumer<RepositoryConfiguration> onComplete, Consumer<Exception> onError) {
        // TODO Better way to generate id.

        try {
            Runnable command = () -> {
                ProductVersion pv = buildCollection.getProductVersion();
        
                String id = String.format(REPO_ID_FORMAT, pv.getProduct().getName(), pv.getVersion(),
                        safeUrlPart(projectBuildConfiguration.getProject().getName()), System.currentTimeMillis());
        
                Properties properties = configuration.getModuleConfig(MAVEN_REPOSITORY_CONFIG_SECTION);
                String baseUrl = properties.getProperty(BASE_URL_PROPERTY);
        
                String url = buildUrl(baseUrl, "api", "group", id);

                onComplete.accept(new MavenRepositoryConfiguration(id, new MavenRepositoryConnectionInfo(url)));
            };
            executor.execute(command);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private String buildUrl(String baseUrl, String api, String group, String id) {
        return String.format("%s%s/%s/%s", baseUrl, api, group, id);
    }

    @Override
    public void persistArtifacts(RepositoryConfiguration repository, ProjectBuildResult buildResult) {
        // TODO Listing/sifting of imports, promotion of output artifacts to build result
    }

    /**
     * Sift out spaces, pipe characters and colons (things that don't play well in URLs) from the project name, and convert them
     * to dashes. This is only for naming repositories, so an approximate match to the project in question is fine.
     */
    private String safeUrlPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }


}
