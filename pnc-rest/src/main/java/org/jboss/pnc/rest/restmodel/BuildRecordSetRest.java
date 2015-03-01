package org.jboss.pnc.rest.restmodel;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.builder.BuildRecordBuilder;
import org.jboss.pnc.model.builder.BuildRecordSetBuilder;
import org.jboss.pnc.model.builder.ProductVersionBuilder;

@XmlRootElement(name = "BuildRecordSet")
public class BuildRecordSetRest {

    private Integer id;

    private ProductMilestone milestone;

    private Integer productVersionId;

    private List<Integer> buildRecordIds;

    public BuildRecordSetRest() {
    }

    public BuildRecordSetRest(BuildRecordSet buildRecordSet) {
        this.id = buildRecordSet.getId();
        this.milestone = buildRecordSet.getMilestone();
        performIfNotNull(buildRecordSet.getProductVersion() != null, () -> this.productVersionId = buildRecordSet
                .getProductVersion().getId());
        this.buildRecordIds = nullableStreamOf(buildRecordSet.getBuildRecord()).map(buildRecord -> buildRecord.getId())
                .collect(Collectors.toList());

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ProductMilestone getMilestone() {
        return milestone;
    }

    public void setMilestone(ProductMilestone milestone) {
        this.milestone = milestone;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public List<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }

    public void setBuildRecordIds(List<Integer> buildRecordIds) {
        this.buildRecordIds = buildRecordIds;
    }

    public BuildRecordSet toBuildRecordSet() {
        BuildRecordSetBuilder builder = BuildRecordSetBuilder.newBuilder();
        builder.id(id);
        builder.milestone(milestone);

        performIfNotNull(productVersionId != null,
                () -> builder.productVersion(ProductVersionBuilder.newBuilder().id(productVersionId).build()));

        nullableStreamOf(buildRecordIds).forEach(buildRecordId -> {
            BuildRecordBuilder buildRecordBuilder = BuildRecordBuilder.newBuilder().id(buildRecordId);
            builder.buildRecord(buildRecordBuilder.build());
        });

        return builder.build();
    }

}
