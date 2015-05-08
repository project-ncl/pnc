package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

import static org.jboss.pnc.rest.utils.Utility.performIfNotNull;

@XmlRootElement(name = "ProductRelease")
public class ProductReleaseRest {

    private Integer id;

    private String version;

    private Date releaseDate;

    private String downloadUrl;

    private Integer productVersionId;

    private Integer buildRecordSetId;

    private Integer productMilestoneId;

    public ProductReleaseRest() {
    }

    public ProductReleaseRest(ProductRelease productRelease) {
        this.id = productRelease.getId();
        this.version = productRelease.getVersion();
        this.releaseDate = productRelease.getReleaseDate();

        this.downloadUrl = productRelease.getDownloadUrl();
        this.productVersionId = productRelease.getProductVersion().getId();
        if (productRelease.getProductMilestone() != null) {
            this.productMilestoneId = productRelease.getProductMilestone().getId();
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

    public Integer getProductMilestoneId() {
        return productMilestoneId;
    }

    public void setProductMilestoneId(Integer productMilestoneId) {
        this.productMilestoneId = productMilestoneId;
    }

    public Integer getBuildRecordSetId() {
        return buildRecordSetId;
    }

    public void setBuildRecordSetId(Integer buildRecordSetId) {
        this.buildRecordSetId = buildRecordSetId;
    }

    public ProductRelease toProductRelease() {
        ProductRelease.Builder builder = ProductRelease.Builder.newBuilder();
        builder.id(id);
        builder.version(version);
        builder.releaseDate(releaseDate);
        builder.downloadUrl(downloadUrl);

        performIfNotNull(productVersionId != null,
                () -> builder.productVersion(ProductVersion.Builder.newBuilder().id(productVersionId).build()));
        performIfNotNull(buildRecordSetId != null,
                () -> builder.buildRecordSet(BuildRecordSet.Builder.newBuilder().id(buildRecordSetId).build()));
        performIfNotNull(productMilestoneId != null,
                () -> builder.productMilestone(ProductMilestone.Builder.newBuilder().id(productMilestoneId).build()));

        return builder.build();

    }
}
