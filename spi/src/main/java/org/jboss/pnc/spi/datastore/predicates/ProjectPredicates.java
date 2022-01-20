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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.Project} entity.
 */
public class ProjectPredicates {

    public static Predicate<Project> withProjectId(Integer projectId) {
        return (root, query, cb) -> cb.equal(root.get(Project_.id), projectId);
    }

    public static Predicate<Project> withProjectName(String name) {
        return (root, query, cb) -> cb.equal(root.get(Project_.name), name);
    }

    public static Predicate<Project> searchByProjectName(String name) {
        return (root, query, cb) -> cb.like(root.get(Project_.name), name);
    }

}
