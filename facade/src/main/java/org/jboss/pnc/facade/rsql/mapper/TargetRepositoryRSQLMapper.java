/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.facade.rsql.RSQLSelectorPath;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;

/**
 * @author Jan Michalov <jmichalo@redhat.com>
 */
@ApplicationScoped
public class TargetRepositoryRSQLMapper implements RSQLMapper<TargetRepository> {

    @Override public Path<?> toPath(From<?, TargetRepository> from, RSQLSelectorPath selector) {
        switch (selector.getElement()) {
            case "id": from.get(TargetRepository_.id);
            case "repositoryPath": from.get(TargetRepository_.repositoryPath);
            case "repositoryType": from.get(TargetRepository_.repositoryType);
            case "identifier": from.get(TargetRepository_.identifier);
            case "temporaryRepo": from.get(TargetRepository_.temporaryRepo);
            default:
                throw new IllegalArgumentException("Unknown RSQL selector " + selector.getElement());
        }

    }
}
