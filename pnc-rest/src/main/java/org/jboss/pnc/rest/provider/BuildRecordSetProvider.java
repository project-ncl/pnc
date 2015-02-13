package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BuildRecordSetProvider {

    private BuildRecordSetRepository buildRecordSetRepository;

    public BuildRecordSetProvider() {
    }

    @Inject
    public BuildRecordSetProvider(BuildRecordSetRepository buildRecordSetRepository) {
        this.buildRecordSetRepository = buildRecordSetRepository;
    }

    public List<BuildRecordSetRest> getAll() {
        return buildRecordSetRepository.findAll().stream().map(buildRecordSet -> new BuildRecordSetRest(buildRecordSet))
                .collect(Collectors.toList());
    }

    public BuildRecordSetRest getSpecific(Integer id) {
        BuildRecordSet buildRecordSet = buildRecordSetRepository.findOne(id);
        if (buildRecordSet != null) {
            return new BuildRecordSetRest(buildRecordSet);
        }
        return null;
    }
}
