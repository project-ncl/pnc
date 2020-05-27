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
package org.jboss.pnc.bpm.causeway;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ApplicationScoped
public class InProgress {
    private Map<Long, Context> inProgress = new ConcurrentHashMap<>();

    public boolean add(Long id, String tagPrefix, String pushResultId) {
        return inProgress.putIfAbsent(id, new Context(id, tagPrefix, pushResultId)) == null;
    }

    public Context remove(Long id) {
        return inProgress.remove(id);
    }

    public Set<Context> getAll() {
        return Collections.unmodifiableSet(inProgress.values().stream().collect(Collectors.toSet()));
    }

    public Context get(Long id) {
        return inProgress.get(id);
    }

    @Getter
    @AllArgsConstructor
    public class Context {
        Long id;
        String tagPrefix;
        String pushResultId;
    }
}
