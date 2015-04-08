package org.jboss.pnc.common.json;

import org.jboss.pnc.common.json.moduleconfig.AuthenticationModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.DockerEnvironmentDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
 
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "@module-config")
@JsonSubTypes({ 
    @Type(value = JenkinsBuildDriverModuleConfig.class, name = "jenkins-build-driver"),
    @Type(value = MavenRepoDriverModuleConfig.class, name = "maven-repo-driver"),
    @Type(value = DockerEnvironmentDriverModuleConfig.class, name = "docker-environment-driver"),
    @Type(value = AuthenticationModuleConfig.class, name = "authentication-config"),
    })
public abstract class AbstractModuleConfig {}