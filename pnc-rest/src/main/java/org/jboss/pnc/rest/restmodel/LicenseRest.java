package org.jboss.pnc.rest.restmodel;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.pnc.model.License;
import org.jboss.pnc.model.builder.LicenseBuilder;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@XmlRootElement(name = "License")
public class LicenseRest {

    private Integer id;

    private String fullName;

    private String fullContent;

    private String refUrl;

    private String shortName;

    private List<Integer> projects;

    public LicenseRest() {

    }

    public LicenseRest(License license) {
        this.id = license.getId();
        this.fullName = license.getFullName();
        this.fullContent = license.getFullContent();
        this.refUrl = license.getRefUrl();
        this.shortName = license.getShortName();
        this.projects = nullableStreamOf(license.getProjects()).map(project -> project.getId()).collect(Collectors.toList());
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the fullContent
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * @param fullContent the fullContent to set
     */
    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }

    /**
     * @return the refUrl
     */
    public String getRefUrl() {
        return refUrl;
    }

    /**
     * @param refUrl the refUrl to set
     */
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return the projects
     */
    public List<Integer> getProjects() {
        return projects;
    }

    /**
     * @param projects the projects to set
     */
    public void setProjects(List<Integer> projects) {
        this.projects = projects;
    }

    @XmlTransient
    public License getLicense(LicenseRest licenseRest) {
        LicenseBuilder licenseBuilder = LicenseBuilder.newBuilder();
        licenseBuilder.id(id);
        licenseBuilder.fullName(fullName);
        licenseBuilder.fullContent(fullContent);
        licenseBuilder.refUrl(refUrl);
        licenseBuilder.shortName(shortName);

        return licenseBuilder.build();
    }

}
