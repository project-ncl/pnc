package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class UserProvider {

    private UserRepository userRepository;

    // needed for EJB/CDI
    public UserProvider() {
    }

    @Inject
    public UserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserRest> getAll(Integer pageIndex, Integer pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(User.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);
        return nullableStreamOf(userRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
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

    public Function<? super User, ? extends UserRest> toRestModel() {
        return user -> new UserRest(user);
    }
}
