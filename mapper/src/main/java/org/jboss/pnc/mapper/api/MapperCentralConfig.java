/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mapper.api;

import static org.mapstruct.MappingConstants.ComponentModel.CDI;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@MapperConfig(
        unmappedSourcePolicy = ReportingPolicy.WARN,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        implementationPackage = "org.jboss.pnc.mapper",
        componentModel = CDI,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface MapperCentralConfig {
}
