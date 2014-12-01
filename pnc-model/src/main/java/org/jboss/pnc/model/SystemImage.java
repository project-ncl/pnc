package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The Class SystemImage, selected by the Environment Driver to run a build, based on the buildConfiguration requirements
 *
 * @author avibelli
 */
@Entity
@Table(name = "system_image")
@NamedQuery(name = "SystemImage.findAll", query = "SELECT s FROM SystemImage s")
public class SystemImage implements Serializable {

    private static final long serialVersionUID = 3170247997550146257L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "environment_id")
    private Environment environment;

    private String name;

    private String description;

    @Column(name = "image_blob")
    private byte[] imageBlob;

    @Column(name = "image_url")
    private String imageUrl;

    /**
     * Instantiates a new system image.
     */
    public SystemImage() {
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the environment.
     *
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment.
     *
     * @param environment the new environment
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description the new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the image blob.
     *
     * @return the image blob
     */
    public byte[] getImageBlob() {
        return imageBlob;
    }

    /**
     * Sets the image blob.
     *
     * @param imageBlob the new image blob
     */
    public void setImageBlob(byte[] imageBlob) {
        this.imageBlob = imageBlob;
    }

    /**
     * Gets the image url.
     *
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the image url.
     *
     * @param imageUrl the new image url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SystemImage)) {
            return false;
        }

        SystemImage s = (SystemImage) o;
        return this.id.equals(s.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SystemImage [name=" + name + "]";
    }

}
