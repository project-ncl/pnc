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
package org.jboss.pnc.rest.restmodel;

import io.swagger.annotations.ApiModelProperty;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Environment")
public class EnvironmentRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @ApiModelProperty(dataType = "string")
    private BuildType buildType;

    @ApiModelProperty(dataType = "string")
    private OperationalSystem operationalSystem;

    public EnvironmentRest() {
    }

    public EnvironmentRest(Environment environment) {
        this.id = environment.getId();
        this.buildType = environment.getBuildType();
        this.operationalSystem = environment.getOperationalSystem();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    public void setOperationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
    }

    public Environment toEnvironment() {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setBuildType(buildType);
        environment.setOperationalSystem(operationalSystem);
        return environment;
    }
}
