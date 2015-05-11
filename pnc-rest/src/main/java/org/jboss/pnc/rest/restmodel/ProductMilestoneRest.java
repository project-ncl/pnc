package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "ProductMilestone")
public class ProductMilestoneRest {

    private Integer id;

    private String version;

    private Date releaseDate;

    private Date plannedStartingDate;

    private Date plannedReleaseDate;

    private String downloadUrl;

    private Integer productVersionId;

    private Integer buildRecordSetId;

    private Integer productReleaseId;

    public ProductMilestoneRest() {
    }

    public ProductMilestoneRest(ProductMilestone productMilestone) {
        this.id = productMilestone.getId();
        this.version = productMilestone.getVersion();
        this.releaseDate = productMilestone.getReleaseDate();
        this.plannedStartingDate = productMilestone.getPlannedStartingDate();
        this.plannedReleaseDate = productMilestone.getPlannedReleaseDate();
        this.downloadUrl = productMilestone.getDownloadUrl();
        this.productVersionId = productMilestone.getProductVersion().getId();
        if (productMilestone.getProductRelease() != null) {
            this.productReleaseId = productMilestone.getProductRelease().getId();
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Date getPlannedStartingDate() {
        return plannedStartingDate;
    }

    public void setPlannedStartingDate(Date plannedStartingDate) {
        this.plannedStartingDate = plannedStartingDate;
    }

    public Date getPlannedReleaseDate() {
        return plannedReleaseDate;
    }

    public void setPlannedReleaseDate(Date plannedReleaseDate) {
        this.plannedReleaseDate = plannedReleaseDate;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Integer getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(Integer productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Integer getProductReleaseId() {
        return productReleaseId;
    }

    public void setProductReleaseId(Integer productReleaseId) {
        this.productReleaseId = productReleaseId;
    }

    public Integer getBuildRecordSetId() {
        return buildRecordSetId;
    }

    public void setBuildRecordSetId(Integer buildRecordSetId) {
        this.buildRecordSetId = buildRecordSetId;
    }

    public ProductMilestone toProductMilestone(ProductVersion productVersion) {
        ProductMilestone productMilestoneToBeUpdated = getProductMilestoneFromProductVersionOrNewOne(productVersion);
        return toProductMilestone(productMilestoneToBeUpdated);
    }

    public ProductMilestone toProductMilestone(ProductMilestone productMilestone) {
        productMilestone.setId(id);
        productMilestone.setVersion(version);
        productMilestone.setReleaseDate(releaseDate);
        productMilestone.setPlannedStartingDate(plannedStartingDate);
        productMilestone.setPlannedReleaseDate(plannedReleaseDate);
        productMilestone.setDownloadUrl(downloadUrl);

        if (buildRecordSetId != null) {
            BuildRecordSet buildRecordSet = BuildRecordSet.Builder.newBuilder().id(buildRecordSetId).build();
            productMilestone.setBuildRecordSet(buildRecordSet);
            buildRecordSet.setProductMilestone(productMilestone);
        }

        if (productReleaseId != null) {
            ProductRelease productRelease = ProductRelease.Builder.newBuilder().id(productReleaseId).build();
            productMilestone.setProductRelease(productRelease);
            productRelease.setProductMilestone(productMilestone);
        }

        return productMilestone;
    }

    /**
     * Checks if ProductMilestone is present in ProductVersion. If it is true - returns it or creates new one otherwise.
     */
    private ProductMilestone getProductMilestoneFromProductVersionOrNewOne(ProductVersion productVersion) {
        List<ProductMilestone> productMilestonesInProductVersion = nullableStreamOf(productVersion.getProductMilestones())
                .filter(productMilestone -> productMilestone.getId().equals(id)).collect(Collectors.toList());

        if (!productMilestonesInProductVersion.isEmpty()) {
            return productMilestonesInProductVersion.get(0);
        }

        ProductMilestone.Builder builder = ProductMilestone.Builder.newBuilder();
        builder.productVersion(productVersion);
        return builder.build();
    }

}
