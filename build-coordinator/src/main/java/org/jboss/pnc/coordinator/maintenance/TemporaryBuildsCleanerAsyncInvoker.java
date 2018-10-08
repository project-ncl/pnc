/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.coordinator.maintenance;

import org.jboss.pnc.common.concurrent.NamedThreadFactory;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class TemporaryBuildsCleanerAsyncInvoker {

    private Logger logger = LoggerFactory.getLogger(TemporaryBuildsCleanerAsyncInvoker.class);

    private TemporaryBuildsCleaner temporaryBuildsCleaner;
    private BuildRecordRepository buildRecordRepository;

    private ExecutorService executorService;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Deprecated //CDI workaround
    public TemporaryBuildsCleanerAsyncInvoker() {
    }

    @Inject
    public TemporaryBuildsCleanerAsyncInvoker(
            TemporaryBuildsCleaner temporaryBuildsCleaner,
            BuildRecordRepository buildRecordRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository) {
        this.temporaryBuildsCleaner = temporaryBuildsCleaner;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;

        executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("build-coordinator.TemporaryBuildsCleanerAsyncInvoker"));
    }

    public void deleteTemporaryBuild(Integer buildRecordId, String authToken, Consumer<Result> onComplete) throws ValidationException {
        BuildRecord buildRecord = buildRecordRepository.findByIdFetchAllProperties(buildRecordId);
        if (!buildRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }

        executorService.submit(() -> {
            try {
                Result result = temporaryBuildsCleaner.deleteTemporaryBuild(buildRecordId, authToken);
                onComplete.accept(result);
            } catch (ValidationException e) {
                logger.error("Failed to delete temporary buildRecord.id: " + buildRecordId + ".", e);
                onComplete.accept(new Result(buildRecordId.toString(), Result.Status.FAILED, "Failed to delete temporary buildRecord."));
            }
        });
    }

    public void deleteTemporaryBuildConfigSetRecord(Integer buildConfigSetRecordId, String authToken, Consumer<Result> onComplete) throws ValidationException {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);
        if (!buildConfigSetRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }
        executorService.submit(() -> {
            try {
                Result result = temporaryBuildsCleaner.deleteTemporaryBuildConfigSetRecord(buildConfigSetRecordId, authToken);
                onComplete.accept(result);
            } catch (ValidationException e) {
                logger.error("Failed to delete temporary buildConfigSetRecord.id: " + buildConfigSetRecordId + ".", e);
                onComplete.accept(new Result(buildConfigSetRecordId.toString(), Result.Status.FAILED, "Failed to delete temporary buildConfigSetRecord."));
            }
        });
    }



}
