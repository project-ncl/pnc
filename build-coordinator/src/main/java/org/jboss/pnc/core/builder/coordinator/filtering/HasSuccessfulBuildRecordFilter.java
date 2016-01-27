/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.core.builder.coordinator.filtering;

import org.jboss.pnc.core.builder.coordinator.BuildTask;
import org.jboss.pnc.core.builder.datastore.DatastoreAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;

/**
 * Checks whether given {@link org.jboss.pnc.model.BuildConfiguration} has a corresponding successful {@link org.jboss.pnc.model.BuildRecord}.
 *
 * @author Sebastian Laskawiec
 */
@ApplicationScoped
public class HasSuccessfulBuildRecordFilter implements BuildTaskFilter {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    DatastoreAdapter adapter;

    @Override
    public Predicate<BuildTask> filter() {
        return task -> {
            boolean hasASuccessfulBuildRecord = adapter.hasSuccessfulBuildRecord(task.getBuildConfiguration());
            logger.debug("[{}] has a successful BuildRecord: {}", task.getBuildConfiguration().getId(), hasASuccessfulBuildRecord);
            return hasASuccessfulBuildRecord;
        };
    }
}
