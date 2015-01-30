package org.jboss.pnc.rest.provider;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.jboss.pnc.rest.restmodel.UserRest;

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

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting) {

        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return userRepository.findAll().stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(userRepository.findAll(buildPageRequest(pageIndex, pageSize, field, sorting)));
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
        User user = userRest.getUser();
        user = userRepository.save(user);
        return user.getId();
    }

}
