package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QUser;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class UserPredicates {

    public static BooleanExpression withUsername(String username) {
        return createNotNullPredicate(username != null, () -> QUser.user.username.eq(username));
    }

    public static BooleanExpression withEmail(String email) {
        return createNotNullPredicate(email != null, () -> QUser.user.email.eq(email));
    }

}
