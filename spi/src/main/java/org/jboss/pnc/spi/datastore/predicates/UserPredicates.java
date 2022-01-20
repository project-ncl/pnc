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

import org.jboss.pnc.model.User;
import org.jboss.pnc.model.User_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.User} entity.
 */
public class UserPredicates {

    public static Predicate<User> withUserName(String name) {
        return (root, query, cb) -> cb.equal(root.get(User_.username), name);
    }

    public static Predicate<User> withEmail(String email) {
        return (root, query, cb) -> cb.equal(root.get(User_.email), email);
    }

}
