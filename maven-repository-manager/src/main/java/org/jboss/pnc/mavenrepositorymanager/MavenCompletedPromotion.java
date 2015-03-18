package org.jboss.pnc.mavenrepositorymanager;

import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryPromotion;

public class MavenCompletedPromotion implements CompletedRepositoryPromotion {

    private boolean success;

    public MavenCompletedPromotion(boolean success) {
        this.success = success;
    }

    @Override
    public boolean isSuccessful() {
        return success;
    }

}
