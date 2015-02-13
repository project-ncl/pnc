package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "BuildRecordSet")
public class BuildRecordSetRest {

    private Integer id;

    private Integer productBuildNumber;

    private ProductMilestone milestone;

    private Integer productVersionId;

    private List<Integer> buildRecordIds;

    public BuildRecordSetRest() {
    }

    public BuildRecordSetRest(BuildRecordSet buildRecordSet) {
        this.id = buildRecordSet.getId();
        this.productBuildNumber = buildRecordSet.getProductBuildNumber();
        this.milestone = buildRecordSet.getMilestone();
        performIfNotNull(buildRecordSet.getProductVersion() != null, () ->this.productVersionId = buildRecordSet.getProductVersion().getId());
        this.buildRecordIds = nullableStreamOf(buildRecordSet.getBuildRecord())
                .map(buildRecord -> buildRecord.getId())
                .collect(Collectors.toList());

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductBuildNumber() {
        return productBuildNumber;
    }

    public void setProductBuildNumber(Integer productBuildNumber) {
        this.productBuildNumber = productBuildNumber;
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

}
