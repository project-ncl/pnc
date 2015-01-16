package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.ProductMilestone;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.provider.Utility.performIfNotNull;

@XmlRootElement(name = "BuildCollection")
public class BuildCollectionRest {

    private Integer id;

    private Integer productBuildNumber;

    private ProductMilestone milestone;

    private Integer productVersionId;

    private List<Integer> buildRecordIds;

    public BuildCollectionRest() {
    }

    public BuildCollectionRest(BuildCollection buildCollection) {
        this.id = buildCollection.getId();
        this.productBuildNumber = buildCollection.getProductBuildNumber();
        this.milestone = buildCollection.getMilestone();
        performIfNotNull(buildCollection.getProductVersion() != null, () ->this.productVersionId = buildCollection.getProductVersion().getId());
        this.buildRecordIds = nullableStreamOf(buildCollection.getBuildRecord())
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
