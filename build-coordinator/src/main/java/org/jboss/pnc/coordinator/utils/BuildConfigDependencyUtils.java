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

package org.jboss.pnc.coordinator.utils;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;

import java.util.Collection;

import static org.jboss.pnc.common.util.CollectionUtils.ofNullableCollection;
import static org.jboss.pnc.common.util.StreamCollectors.toFlatList;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/19/16
 * Time: 12:59 PM
 */
public class BuildConfigDependencyUtils {

    public static boolean hasARebuiltDependency(BuildConfiguration config) {
        BuildRecord record = config.getLatestSuccesfulBuildRecord();
        if (record == null) {
            return false;
        }

        Collection<BuildRecord> lastBuiltFrom = getRecordsUsedFor(record);

        return lastBuiltFrom.stream()
                .anyMatch(BuildConfigDependencyUtils::hasNewerVersion);
    }

    private static boolean hasNewerVersion(BuildRecord record) {
        BuildConfiguration buildConfig = record.getLatestBuildConfiguration();
        BuildRecord newestRecord = buildConfig.getLatestSuccesfulBuildRecord();
        return !record.getId().equals(newestRecord.getId());
    }

    private static Collection<BuildRecord> getRecordsUsedFor(BuildRecord record) {
        return ofNullableCollection(record.getDependencies())
                .stream()
                .map(Artifact::getBuildRecords)
                .collect(toFlatList());
    }
}
