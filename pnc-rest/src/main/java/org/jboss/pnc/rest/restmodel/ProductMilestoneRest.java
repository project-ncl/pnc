package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "ProductMilestone")
public class ProductMilestoneRest {

    private Integer id;

    private String version;

    private Date releaseDate;

    private Date startingDate;

    private Date endingDate;

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
        this.startingDate = productMilestone.getStartingDate();
        this.endingDate = productMilestone.getEndingDate();
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

    public Date getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Date startingDate) {
        this.startingDate = startingDate;
    }

    public Date getEndingDate() {
        return endingDate;
    }

    public void setEndingDate(Date endingDate) {
        this.endingDate = endingDate;
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

    public ProductMilestone toProductMilestone() {
        ProductMilestone.Builder builder = ProductMilestone.Builder.newBuilder();
        builder.id(id);
        builder.version(version);
        builder.releaseDate(releaseDate);
        builder.startingDate(startingDate);
        builder.endingDate(endingDate);
        builder.downloadUrl(downloadUrl);

        performIfNotNull(productVersionId != null,
                () -> builder.productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build()));
        performIfNotNull(buildRecordSetId != null,
                () -> builder.buildRecordSet(BuildRecordSet.Builder.newBuilder().id(buildRecordSetId).build()));
        performIfNotNull(productReleaseId != null,
                () -> builder.productRelease(ProductRelease.Builder.newBuilder().id(productReleaseId).build()));

        return builder.build();
    }

}
