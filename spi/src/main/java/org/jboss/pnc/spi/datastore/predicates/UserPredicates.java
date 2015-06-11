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
