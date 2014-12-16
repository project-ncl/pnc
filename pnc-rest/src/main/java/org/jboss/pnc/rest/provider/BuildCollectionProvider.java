package org.jboss.pnc.rest.provider;


import org.jboss.pnc.datastore.repositories.BuildCollectionRepository;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.rest.restmodel.BuildCollectionRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BuildCollectionProvider {

    private BuildCollectionRepository buildCollectionRepository;

    public BuildCollectionProvider() {
    }

    @Inject
    public BuildCollectionProvider(BuildCollectionRepository buildCollectionRepository) {
        this.buildCollectionRepository = buildCollectionRepository;
    }

    public List<BuildCollectionRest> getAll() {
        return buildCollectionRepository.findAll().stream()
                .map(buildCollection -> new BuildCollectionRest(buildCollection))
                .collect(Collectors.toList());
    }

    public BuildCollectionRest getSpecific(Integer id) {
        BuildCollection buildCollection = buildCollectionRepository.findOne(id);
        if(buildCollection != null) {
            return new BuildCollectionRest(buildCollection);
        }
        return null;
    }
}
