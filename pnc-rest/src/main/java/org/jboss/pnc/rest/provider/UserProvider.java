package org.jboss.pnc.rest.provider;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.UserRepository;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.UserRest;

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

    public List<UserRest> getAll() {
        return userRepository.findAll().stream().map(user -> new UserRest(user)).collect(Collectors.toList());
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
