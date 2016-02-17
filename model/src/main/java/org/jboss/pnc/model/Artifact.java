/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * Class that maps the artifacts created and/or used by the builds of the projects.
 * The "type" indicates the genesis of the artifact, whether it has been imported from 
 * external repositories, or built internally.
 * 
 * The repoType indicated the type of repository which is used to distributed the artifact.
 * The repoType repo indicates the format for the identifier field.
 * 
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "identifier", "checksum" }) )
public abstract class Artifact implements GenericEntity<Integer> {

    private static final long serialVersionUID = -2368833657284575734L;
    public static final String SEQUENCE_NAME = "artifact_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Contains a string which uniquely identifies the artifact in a repository.
     * For example, for a maven artifact this is the GATVC (groupId:artifactId:type:version[:qualifier]
     * The format of the identifier string is determined by the repoType
     */
    @NotNull
    @Column(updatable=false)
    private String identifier;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc).  This field determines
     * the format of the identifier string.
     */
    @NotNull
    @Column(updatable=false)
    private RepositoryType repoType;

    @NotNull
    @Column(updatable=false)
    private String checksum;

    @Column(updatable=false)
    private String filename;

    /**
     * Repository URL where the artifact file is available.
     */
    @Column(updatable=false)
    private String deployUrl;

    @Column(insertable=false, updatable=false)
    private String type;

    /**
     * The builds for which this artifact is a dependency
     */
    @NotNull
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildRecord> dependantBuildRecords;

    /**
     * Instantiates a new artifact.
     */
    public Artifact() {
        dependantBuildRecords = new HashSet<BuildRecord>();
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
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the identifier.
     * 
     * The identifier should contain different logic depending on the artifact type: i.e Maven should contain the GAV, NPM and
     * CocoaPOD should be identified differently
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the new identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the checksum.
     *
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum.
     *
     * @param checksum the new checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename.
     *
     * @param filename the new filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Gets the deploy url.
     *
     * @return the deploy url
     */
    public String getDeployUrl() {
        return deployUrl;
    }

    /**
     * Sets the deploy url.
     *
     * @param deployUrl the new deploy url
     */
    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    /**
     * Gets the type of the artifact, i.e. whether it has been imported or built internally.
     * The possible types are defined by string constants in the ArtifactType interface.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    public Set<BuildRecord> getDependantBuildRecords() {
        return dependantBuildRecords;
    }

    public void setDependantBuildRecords(Set<BuildRecord> buildRecords) {
        this.dependantBuildRecords = buildRecords;
    }

    public void addDependantBuildRecord(BuildRecord buildRecord) {
        if (!dependantBuildRecords.contains(buildRecord)) {
            this.dependantBuildRecords.add(buildRecord);
        }
    }

    /**
     * @return the repoType
     */
    public RepositoryType getRepoType() {
        return repoType;
    }

    /**
     * @param repoType the repoType to set
     */
    public void setRepoType(RepositoryType repoType) {
        this.repoType = repoType;
    }

    @Override
    public String toString() {
        return "Artifact [id: " + id + ", filename=" + filename + "]";
    }

}
