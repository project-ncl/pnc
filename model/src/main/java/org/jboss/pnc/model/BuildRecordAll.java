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

import org.hibernate.annotations.Type;
import org.jboss.pnc.common.security.Md5;
import org.jboss.pnc.common.security.Sha256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * <p>
 * This class contains the build result of a project configuration, and contains additional metadata, as the build script, the
 * starting and ending time of a build, the status of the build, the sources url used, the user that triggered the build, plus
 * all the Artifacts that were built and all the Artifacts that were used for the final build. It stores also the buildDriverID
 * that was used to run the build, the system Image where is was run in, and is mapped to a BuildRecordSet, that encapsulates
 * the set of buildRecord that compose a Product
 */
@Entity
@Table(
    name = "buildrecord",
    indexes = {
        @Index(name = "idx_buildrecord_user", columnList = "user_id"),
        @Index(name="idx_buildrecord_buildenvironment", columnList = "buildenvironment_id"),
        @Index(name="idx_buildrecord_buildconfigsetrecord", columnList = "buildconfigsetrecord_id")
})
public class BuildRecordAll extends BuildRecord implements GenericEntity<Integer> {

    private static Logger logger = LoggerFactory.getLogger(BuildRecordAll.class);

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    @Column(updatable = false)
    private String buildLog;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Basic(fetch = FetchType.LAZY)
    @Column(updatable = false)
    private String repourLog;


    /**
     * Instantiates a new project build result.
     */
    public BuildRecordAll() {
        super();
    }

    public String getBuildLog() {
        return buildLog;
    }

    public void setBuildLog(String buildLog) {
        this.buildLog = buildLog;
    }

    public String getRepourLog() {
        return repourLog;
    }

    public void setRepourLog(String repourLog) {
        this.repourLog = repourLog;
    }

    public static class Builder {

        private Integer id;

        private String buildContentId;

        private Boolean temporaryBuild;

        private Date submitTime;

        private Date startTime;

        private Date endTime;

        private BuildConfigurationAudited buildConfigurationAudited;

        private Integer buildConfigurationAuditedId;

        private Integer buildConfigurationAuditedRev;

        private User user;

        private String scmRepoURL;

        private String scmRevision;

        private String repourLog = "";

        private String buildLog = "";

        private BuildStatus status;

        private Set<Artifact> builtArtifacts;

        private Set<Artifact> dependencies;

        private BuildEnvironment buildEnvironment;

        private ProductMilestone productMilestone;

        private BuildConfigSetRecord buildConfigSetRecord;

        private String sshCommand;

        private String sshPassword;

        private String executionRootName;

        private String executionRootVersion;

        private Map<String, String> attributes = new HashMap<>();

        public Builder() {
            builtArtifacts = new HashSet<>();
            dependencies = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildRecordAll build() {
            BuildRecordAll buildRecord = new BuildRecordAll();
            buildRecord.setId(id);
            buildRecord.setBuildContentId(buildContentId);
            buildRecord.setSubmitTime(submitTime);
            buildRecord.setStartTime(startTime);
            buildRecord.setEndTime(endTime);
            buildRecord.setUser(user);
            buildRecord.setScmRepoURL(scmRepoURL);
            buildRecord.setScmRevision(scmRevision);
            buildRecord.setStatus(status);
            buildRecord.setBuildEnvironment(buildEnvironment);
            buildRecord.setProductMilestone(productMilestone);
            buildRecord.setAttributes(attributes);
            buildRecord.setSshCommand(sshCommand);
            buildRecord.setSshPassword(sshPassword);
            buildRecord.setExecutionRootName(executionRootName);
            buildRecord.setExecutionRootVersion(executionRootVersion);
            buildRecord.setBuildConfigurationId(buildConfigurationAuditedId);
            buildRecord.setBuildConfigurationRev(buildConfigurationAuditedRev);

            buildRecord.setRepourLog(repourLog);
            buildRecord.setRepourLogSize(repourLog.length());
            buildRecord.setBuildLog(buildLog);
            buildRecord.setBuildLogSize(buildLog.length());

            if (temporaryBuild == null) {
                temporaryBuild = true;
            }
            buildRecord.setTemporaryBuild(temporaryBuild);

            try {
                buildRecord.setBuildLogMd5(Md5.digest(buildLog));
                buildRecord.setBuildLogSha256(Sha256.digest(buildLog));

                buildRecord.setRepourLogMd5(Md5.digest(repourLog));
                buildRecord.setRepourLogSha256(Sha256.digest(repourLog));

            } catch (NoSuchAlgorithmException | IOException e) {
                logger.error("Cannot compute log checksum.", e);
                throw new RuntimeException("Cannot compute log checksum.", e);
            }

            if (buildConfigurationAudited != null) {
                setBuildConfigurationAuditedIfValid(buildRecord,
                        buildConfigurationAuditedId,
                        buildConfigurationAuditedRev,
                        buildConfigurationAudited);
            }

            if (buildConfigSetRecord != null) {
                buildRecord.setBuildConfigSetRecord(buildConfigSetRecord);
            }

            buildRecord.setBuiltArtifacts(builtArtifacts);
            buildRecord.setDependencies(dependencies);

            return buildRecord;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder buildContentId(String buildContentId) {
            this.buildContentId = buildContentId;
            return this;
        }

        public Builder temporaryBuild(boolean temporaryBuild) {
            this.temporaryBuild = temporaryBuild;
            return this;
        }

        public Builder submitTime(Date submitTime) {
            this.submitTime = submitTime;
            return this;
        }

        public Builder startTime(Date startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Date endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder buildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
            this.buildConfigurationAudited = buildConfigurationAudited;
            return this;
        }

        public Builder buildConfigurationAuditedId(Integer buildConfigurationAuditedId) {
            this.buildConfigurationAuditedId = buildConfigurationAuditedId;
            return this;
        }

        public Builder buildConfigurationAuditedRev(Integer buildConfigurationAuditedRev) {
            this.buildConfigurationAuditedRev = buildConfigurationAuditedRev;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder scmRepoURL(String scmRepoURL) {
            this.scmRepoURL = scmRepoURL;
            return this;
        }

        public Builder scmRevision(String scmRevision) {
            this.scmRevision = scmRevision;
            return this;
        }

        public Builder buildLog(String buildLog) {
            this.buildLog = buildLog;
            return this;
        }

        public Builder appendLog(String buildLog) {
            this.buildLog += buildLog;
            return this;
        }

        public Builder status(BuildStatus status) {
            this.status = status;
            return this;
        }

        public Builder builtArtifact(Artifact builtArtifact) {
            this.builtArtifacts.add(builtArtifact);
            return this;
        }

        public Builder builtArtifacts(Set<Artifact> builtArtifacts) {
            this.builtArtifacts = builtArtifacts;
            return this;
        }

        public Builder builtArtifacts(List<Artifact> builtArtifacts) {
            this.builtArtifacts.addAll(builtArtifacts);
            return this;
        }

        public Builder dependency(Artifact artifact) {
            this.dependencies.add(artifact);
            return this;
        }

        public Builder dependencies(Set<Artifact> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder dependencies(List<Artifact> dependencies) {
            this.dependencies.addAll(dependencies);
            return this;
        }

        @Deprecated
        public Builder buildEnvironment(BuildEnvironment buildEnvironment) {
            this.buildEnvironment = buildEnvironment;
            return this;
        }

        public Builder productMilestone(ProductMilestone productMilestone) {
            this.productMilestone = productMilestone;
            return this;
        }

        public Builder buildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
            this.buildConfigSetRecord = buildConfigSetRecord;
            return this;
        }

        public BuildRecordAll.Builder attributes(Map<String, String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public BuildRecordAll.Builder sshCommand(String sshCommand) {
            this.sshCommand = sshCommand;
            return this;
        }

        public BuildRecordAll.Builder sshPassword(String sshPassword) {
            this.sshPassword = sshPassword;
            return this;
        }

        public BuildRecordAll.Builder executionRootName(String executionRootName) {
            this.executionRootName = executionRootName;
            return this;
        }

        public BuildRecordAll.Builder executionRootVersion(String executionRootVersion) {
            this.executionRootVersion = executionRootVersion;
            return this;
        }

        public BuildRecordAll.Builder attribute(String key, String value) {
            this.attributes.put(key, value);
            return this;
        }

        public BuildRecordAll.Builder repourLog(String log) {
            this.repourLog = log;
            return this;
        }
    }

}
