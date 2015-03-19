package org.jboss.pnc.mavenrepositorymanager;

import org.commonjava.aprox.client.core.Aprox;
import org.commonjava.aprox.client.core.AproxClientException;
import org.commonjava.aprox.model.core.Group;
import org.commonjava.aprox.model.core.StoreKey;
import org.commonjava.aprox.model.core.StoreType;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryPromotion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

import java.util.function.Consumer;

public class MavenRunningPromotion implements RunningRepositoryPromotion {

    private String buildRepoId;
    private String recordSetRepoId;
    private Aprox aprox;

    public MavenRunningPromotion(String buildRepoId, String recordSetRepoId, Aprox aprox) {
        this.buildRepoId = buildRepoId;
        this.recordSetRepoId = recordSetRepoId;
        this.aprox = aprox;
    }

    @Override
    public void monitor(Consumer<CompletedRepositoryPromotion> onComplete, Consumer<Exception> onError) {
        try {
            if (!aprox.stores().exists(StoreType.hosted, buildRepoId)) {
                throw new RepositoryManagerException("No such build repository: %s", buildRepoId);
            }

            Group recordSetGroup = aprox.stores().load(StoreType.group, recordSetRepoId, Group.class);
            if (recordSetGroup == null) {
                throw new RepositoryManagerException("No such build-record group: %s", recordSetRepoId);
            }

            recordSetGroup.addConstituent(new StoreKey(StoreType.hosted, buildRepoId));

            boolean result = aprox.stores().update(recordSetGroup,
                    "Promoting build repo: " + buildRepoId + " to group: " + recordSetRepoId);
            onComplete.accept(new MavenCompletedPromotion(result));

        } catch (AproxClientException | RepositoryManagerException e) {
            onError.accept(e);
        }
    }

}
