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

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.hibernate.envers.Audited;

/**
 * The Class Environment.
 */
@Entity
@Audited
public class Environment implements GenericEntity<Integer> {

    private static final long serialVersionUID = 8213767399060607637L;

    public static final String DEFAULT_SORTING_FIELD = "id";
    public static final String SEQUENCE_NAME = "environment_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Enumerated(EnumType.STRING)
    private BuildType buildType;

    @Enumerated(EnumType.STRING)
    private OperationalSystem operationalSystem;

    /**
     * Instantiates a new environment.
     */
    public Environment() {
    }

    /**
     * Instantiates a new environment.
     *
     * @param buildType the build type
     * @param operationalSystem the operational system
     */
    public Environment(BuildType buildType, OperationalSystem operationalSystem) {
        this.buildType = buildType;
        this.operationalSystem = operationalSystem;
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
     * Gets the operational system.
     *
     * @return the operational system
     */
    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    /**
     * Sets the builds the type.
     *
     * @param buildType the new builds the type
     */
    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    /**
     * Gets the builds the type.
     *
     * @return the builds the type
     */
    public BuildType getBuildType() {
        return buildType;
    }

    /**
     * Sets the operational system.
     *
     * @param operationalSystem the new operational system
     */
    public void setOperationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Environment [operationalSystem=" + operationalSystem + ", buildType=" + buildType + "]";
    }

    public static class Builder {

        private BuildType buildType = BuildType.JAVA;
        private OperationalSystem operationalSystem = OperationalSystem.LINUX;
        private Integer id;

        private Builder() {

        }

        public static Builder defaultEnvironment() {
            return new Builder();
        }

        public static Builder emptyEnvironment() {
            return new Builder().id(null).buildType(null).operationalSystem(null);
        }

        public Environment build() {
            Environment environment = new Environment();
            environment.setId(id);
            environment.setBuildType(buildType);
            environment.setOperationalSystem(operationalSystem);
            return environment;
        }

        public OperationalSystem getOperationalSystem() {
            return operationalSystem;
        }

        public Builder buildType(BuildType buildType) {
            this.buildType = buildType;
            return this;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        private Builder operationalSystem(OperationalSystem operationalSystem) {
            this.operationalSystem = operationalSystem;
            return this;
        }
    }
}
