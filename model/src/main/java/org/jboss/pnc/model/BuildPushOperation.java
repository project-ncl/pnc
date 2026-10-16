/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.api.constants.OperationParameters;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue("BuildPush")
public class BuildPushOperation extends Operation {

    private static final long serialVersionUID = 4972591927855499338L;

    /**
     * The build that was being pushed to Brew.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_operation_buildrecord"))
    private BuildRecord build;

    /**
     * The report assigned to this operation.
     */
    @OneToOne(mappedBy = "operation")
    private BuildPushReport report;

    public String getTagPrefix() {
        return getOperationParameters().get(OperationParameters.BUILD_PUSH_TAG_PREFIX);
    }

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    public BuildPushOperation(
            @lombok.Builder.ObtainVia(method = "getId") Base32LongID id,
            @lombok.Builder.ObtainVia(method = "getSubmitTime") Date submitTime,
            @lombok.Builder.ObtainVia(method = "getStartTime") Date startTime,
            @lombok.Builder.ObtainVia(method = "getEndTime") Date endTime,
            @lombok.Builder.ObtainVia(method = "getUser") @NotNull User user,
            @lombok.Builder.ObtainVia(method = "getOperationParameters") Map<String, String> operationParameters,
            @lombok.Builder.ObtainVia(method = "getProgressStatus") ProgressStatus progressStatus,
            @lombok.Builder.ObtainVia(method = "getResult") OperationResult result,
            @lombok.Builder.ObtainVia(method = "getReason") String reason,
            @lombok.Builder.ObtainVia(method = "getProposal") String proposal,
            BuildRecord build,
            BuildPushReport report) {
        super(id, submitTime, startTime, endTime, user, operationParameters, progressStatus, result, reason, proposal);
        this.build = build;
        this.report = report;
    }
}
