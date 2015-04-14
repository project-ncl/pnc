package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.UserRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StringUtils.isEmpty;
import static org.jboss.pnc.datastore.predicates.UserPredicates.withEmail;
import static org.jboss.pnc.datastore.predicates.UserPredicates.withUsername;
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

    public List<UserRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
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
        Preconditions.checkArgument(userRest.getId() == null, "Id must be null");
        Preconditions.checkArgument(!isEmpty(userRest.getUsername()), "Username is required");
        Preconditions.checkArgument(!isEmpty(userRest.getEmail()), "Email is required");
        checkForConflictingUser(userRest);

        User user = userRest.toUser();
        user = userRepository.save(user);
        return user.getId();
    }

    public Integer update(Integer id, UserRest userRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(userRest.getId() == null || userRest.getId().equals(id),
                "Entity id does not match the id to update");
        Preconditions.checkArgument(!isEmpty(userRest.getUsername()), "Username is required");
        Preconditions.checkArgument(!isEmpty(userRest.getEmail()), "Email is required");
        userRest.setId(id);
        checkForConflictingUser(userRest);

        User user = userRepository.findOne(id);
        Preconditions.checkArgument(user != null, "Couldn't find user with id " + id);
        user = userRepository.save(userRest.toUser());
        return user.getId();
    }

    public Function<? super User, ? extends UserRest> toRestModel() {
        return user -> new UserRest(user);
    }

    /**
     * Check if the user object contains the required fields and does not conflict with
     * existing users in the database.
     * 
     * @throws IllegalArgumentException if the user is not valid
     * @param userRest
     */
    public void checkForConflictingUser(UserRest userRest) {
        Iterable<User> conflictingUsers = userRepository.findAll(withUsername(userRest.getUsername()).or(withEmail(userRest.getEmail())));
        for (User conflict : conflictingUsers) {
            if ( !conflict.getId().equals(userRest.getId()) ) {
                if ( userRest.getUsername().equals(conflict.getUsername())) {
                    throw new IllegalArgumentException("Username already in use by another user");
                }
                if ( userRest.getEmail().equals(conflict.getEmail())) {
                    throw new IllegalArgumentException("Email address already in use by another user");
                }
            }
        }
        return;
    }
}
