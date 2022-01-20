/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.indyrepositorymanager;

import org.commonjava.indy.client.core.Indy;
import org.commonjava.indy.client.core.IndyClientException;
import org.commonjava.indy.model.core.StoreKey;
import org.commonjava.indy.model.core.StoreType;
import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;

import java.util.function.Consumer;

public class IndyRunningDeletion implements RunningRepositoryDeletion {

    private String pakageType;
    private StoreType fromType;
    private String fromId;
    private Indy indy;

    public IndyRunningDeletion(String pakageType, StoreType fromType, String fromId, Indy indy) {
        this.pakageType = pakageType;
        this.fromType = fromType;
        this.fromId = fromId;
        this.indy = indy;
    }

    /**
     * Trigger the repository deletion configured for this instance, and send the result to the appropriate consumer.
     *
     * @param onComplete The consumer which will handle non-error results
     * @param onError Handles errors
     */
    @Override
    public void monitor(Consumer<CompletedRepositoryDeletion> onComplete, Consumer<Exception> onError) {
        try {
            StoreKey fromKey = new StoreKey(pakageType, fromType, fromId);
            if (indy.stores().exists(fromKey)) {
                indy.stores().delete(fromKey, "Deleting artifacts for PNC build");
            }

            onComplete.accept(new IndyCompletedDeletion(true));

        } catch (IndyClientException e) {
            onError.accept(e);
        }
    }

}
