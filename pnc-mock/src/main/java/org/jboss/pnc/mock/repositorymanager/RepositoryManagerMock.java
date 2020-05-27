/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mock.repositorymanager;

import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;
import org.jboss.pnc.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.CompletedRepositoryPromotion;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryDeletion;
import org.jboss.pnc.spi.repositorymanager.model.RunningRepositoryPromotion;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 */
@ApplicationScoped
public class RepositoryManagerMock implements RepositoryManager {

    private Boolean promotionSuccess;

    private Exception promotionError;

    private Boolean deletionSuccess;

    private Exception deletionError;

    public RepositoryManagerMock expectPromotionSuccess(boolean promotionSuccess) {
        this.promotionSuccess = promotionSuccess;
        return this;
    }

    public RepositoryManagerMock expectPromotionError(Exception promotionError) {
        this.promotionError = promotionError;
        return this;
    }

    public RepositoryManagerMock expectDeletionSuccess(boolean deletionSuccess) {
        this.deletionSuccess = deletionSuccess;
        return this;
    }

    public RepositoryManagerMock expectDeletionError(Exception deletionError) {
        this.deletionError = deletionError;
        return this;
    }

    @Override
    public RepositorySession createBuildRepository(
            BuildExecution buildExecution,
            String accessToken,
            String serviceAccountToken,
            RepositoryType repositoryType,
            Map<String, String> genericParameters) throws RepositoryManagerException {

        RepositorySession repositoryConfiguration = new RepositorySessionMock();
        return repositoryConfiguration;
    }

    @Override
    public RepositoryManagerResult collectRepoManagerResult(Long id) throws RepositoryManagerException {
        return RepositoryManagerResultMock.mockResult(false);
    }

    @Override
    public boolean canManage(RepositoryType managerType) {
        return true;
    }

    @Override
    public RunningRepositoryPromotion promoteBuild(
            BuildRecord buildRecord,
            String pkgType,
            String toGroup,
            String accessToken) throws RepositoryManagerException {
        return new RunningRepositoryPromotionMock(promotionSuccess, promotionError);
    }

    @Override
    public RunningRepositoryDeletion deleteBuild(BuildRecord buildRecord, String pkgType, String accessToken)
            throws RepositoryManagerException {
        return new RunningRepositoryDeletionMock(deletionSuccess, deletionError);
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
                onComplete.accept(() -> status);
            } else {
                onError.accept(error);
            }
        }

    }

    public static final class RunningRepositoryDeletionMock implements RunningRepositoryDeletion {

        private Boolean status;
        private Exception error;

        public RunningRepositoryDeletionMock(Boolean status, Exception error) {
            this.status = status;
            this.error = error;
        }

        @Override
        public void monitor(Consumer<CompletedRepositoryDeletion> onComplete, Consumer<Exception> onError) {
            if (status != null) {
                onComplete.accept(() -> status);
            } else {
                onError.accept(error);
            }
        }

    }

}
