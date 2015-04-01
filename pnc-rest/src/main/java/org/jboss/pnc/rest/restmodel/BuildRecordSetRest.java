package org.jboss.pnc.rest.restmodel;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.pnc.common.Identifiable;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductVersion;

@XmlRootElement(name = "BuildRecordSet")
public class BuildRecordSetRest implements Identifiable<Integer> {

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
        BuildRecordSet.Builder builder = BuildRecordSet.Builder.newBuilder();
        builder.id(id);
        builder.milestone(milestone);

        performIfNotNull(productVersionId != null,
                () -> builder.productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build()));

        nullableStreamOf(buildRecordIds).forEach(buildRecordId -> {
            BuildRecord.Builder buildRecordBuilder = BuildRecord.Builder.newBuilder().id(buildRecordId);
            builder.buildRecord(buildRecordBuilder.build());
        });

        return builder.build();
    }

}
