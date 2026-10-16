/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.ProjectRef;

public interface ProjectProvider extends Provider<Integer, org.jboss.pnc.model.Project, Project, ProjectRef> {
}
