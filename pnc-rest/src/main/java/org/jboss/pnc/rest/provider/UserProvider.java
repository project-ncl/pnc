package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.rest.restmodel.UserRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class UserProvider extends BasePaginationProvider<UserRest, User> {

    private UserRepository userRepository;

    // needed for EJB/CDI
    public UserProvider() {
    }

    @Inject
    public UserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super User, ? extends UserRest> toRestModel() {
        return user -> new UserRest(user);
    }

    @Override
    public String getDefaultSortingField() {
        return User.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(User.class, query);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return nullableStreamOf(userRepository.findAll(filteringCriteria.get())).map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(userRepository.findAll(filteringCriteria.get(), buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public UserRest getSpecific(Integer userId) {
        User user = userRepository.findOne(userId);
        if (user != null) {
            return new UserRest(user);
        }
        return null;
    }

    public Integer store(UserRest userRest) {
        User user = userRest.toUser();
        user = userRepository.save(user);
        return user.getId();
    }

}
