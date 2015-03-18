package org.jboss.pnc.spi.repositorymanager.model;

import java.util.function.Consumer;

public interface RunningRepositoryPromotion {

    void monitor(Consumer<CompletedRepositoryPromotion> onComplete, Consumer<Exception> onError);

}
