/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;

import org.jboss.pnc.dto.BuildConfiguration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

/**
 * Repository creation configuration object.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Setter
@Getter
@ToString
@Builder(builderClassName = "RepositoryCreationProcessBuilder")
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryCreationProcess implements Serializable {

    private RepositoryConfiguration repositoryConfiguration;
    private BuildConfiguration buildConfiguration;
    private String revision;

}
