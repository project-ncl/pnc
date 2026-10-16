/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.dto.validation.groups.WhenImporting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Jacksonized
@Builder(builderClassName = "Builder")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImportBuildsRequest {

    @Valid
    @NotNull(groups = WhenImporting.class)
    private final List<@NotNull(groups = WhenImporting.class) BuildImport> imports;
}
