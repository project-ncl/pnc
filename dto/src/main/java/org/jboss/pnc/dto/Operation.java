/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.util.Map;

import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@PatchSupport
@Data
@Jacksonized
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder(builderMethodName = "operationBuilder", toBuilder = true)
public class Operation extends OperationRef {

    /**
     * The user who started this operation.
     */
    protected final User user;

    /**
     * Map of operation input parameters. These parameters are used by the specific operation for the execution.
     */
    @PatchSupport({ ADD, REMOVE, REPLACE })
    protected final Map<String, String> parameters;
}
