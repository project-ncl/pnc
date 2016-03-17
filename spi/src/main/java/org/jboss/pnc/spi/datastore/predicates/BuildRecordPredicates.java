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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.*;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.SetJoin;

import java.util.Collection;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public class BuildRecordPredicates {

    public static Predicate<BuildRecord> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> cb.equal(root.get(org.jboss.pnc.model.BuildRecord_.id), buildRecordId);
    }

    public static Predicate<BuildRecord> withBuildConfigurationId(Integer configurationId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildConfigurationAudited> buildConfigurationAudited = root.join(BuildRecord_.buildConfigurationAudited);
            return cb.equal(buildConfigurationAudited.get(org.jboss.pnc.model.BuildConfigurationAudited_.id), configurationId);
        };
    }

    public static Predicate<BuildRecord> withSuccess() {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.status), BuildStatus.SUCCESS);
    }

    public static Predicate<BuildRecord> withBuildConfigSetId(Integer buildConfigSetId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildConfigSetRecord> joinedConfiguSet = root.join(BuildRecord_.buildConfigSetRecord);
            return cb.equal(joinedConfiguSet.get(org.jboss.pnc.model.BuildConfigSetRecord_.id), buildConfigSetId);
        };
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdInSet(Collection<Integer> buildConfigurationIds) {
        if (buildConfigurationIds.isEmpty()) {
            // return an always false predicate if there are no build config ids
            return (root, query, cb) -> cb.disjunction();
        } else {
            return (root, query, cb) -> {
                Join<BuildRecord, BuildConfigurationAudited> buildConfigurationAudited = root
                        .join(BuildRecord_.buildConfigurationAudited);
                return buildConfigurationAudited.get(org.jboss.pnc.model.BuildConfigurationAudited_.id)
                        .in(buildConfigurationIds);
            };
        }
    }

    public static Predicate<BuildRecord> withProjectId(Integer projectId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildConfigurationAudited> buildConfigurationAudited = root.join(BuildRecord_.buildConfigurationAudited);
            Join<BuildConfigurationAudited, Project> project = buildConfigurationAudited.join(
                    org.jboss.pnc.model.BuildConfigurationAudited_.project);
            return cb.equal(project.get(org.jboss.pnc.model.Project_.id), projectId);
        };
    }

    public static Predicate<BuildRecord> withArtifactDistributedInMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            ListJoin<BuildRecord, Artifact> builtArtifacts = root.join(BuildRecord_.builtArtifacts);
            SetJoin<Artifact, ProductMilestone> productMilestones = builtArtifacts.join(Artifact_.distributedInProductMilestones);
            return cb.equal(productMilestones.get(ProductMilestone_.id), productMilestoneId);
        };
    }

}
