/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

import java.io.Serializable;
import java.util.List;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.model.Artifact;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public interface RepositoryManagerResult extends Serializable {
    List<Artifact> getBuiltArtifacts();

    List<Artifact> getDependencies();

    String getBuildContentId();

    CompletionStatus getCompletionStatus();

}
