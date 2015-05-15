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
package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryPromotion;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
public class RepositoryManagerMock implements RepositoryManager {

    private Boolean promotionSuccess;

    private Exception promotionError;

    public RepositoryManagerMock expectPromotionSuccess(boolean promotionSuccess) {
        this.promotionSuccess = promotionSuccess;
        return this;
    }

    public RepositoryManagerMock expectPromotionError(Exception promotionError) {
        this.promotionError = promotionError;
        return this;
    }

    @Override
    public RepositorySession createBuildRepository(BuildExecution buildExecution) throws RepositoryManagerException {

        RepositorySession repositoryConfiguration = new RepositorySessionMock();
        return repositoryConfiguration;
    }

    @Override
    public boolean canManage(RepositoryType managerType) {
        return true;
    }

    @Override
    public RunningRepositoryPromotion promoteBuild(BuildRecord buildRecord, String toGroup)
            throws RepositoryManagerException {
        return new RunningRepositoryPromotionMock(promotionSuccess, promotionError);
    }

    @Override
    public RunningRepositoryPromotion promoteBuildSet(BuildRecordSet buildRecordSet, String toGroup)
            throws RepositoryManagerException {
        return new RunningRepositoryPromotionMock(promotionSuccess, promotionError);
    }

    public static final class RunningRepositoryPromotionMock implements RunningRepositoryPromotion {

        private Boolean status;
        private Exception error;

        public RunningRepositoryPromotionMock(Boolean status, Exception error) {
            this.status = status;
            this.error = error;
        }

        @Override
        public void monitor(Consumer<CompletedRepositoryPromotion> onComplete, Consumer<Exception> onError) {
            if (status != null) {
                onComplete.accept(new CompletedRepositoryPromotion() {
                    @Override
                    public boolean isSuccessful() {
                        return status;
                    }
                });
            } else {
                onError.accept(error);
            }
        }

    }

}
