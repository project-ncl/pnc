/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildPageInfo {

    private final int pageIndex;
    private final int pageSize;
    private final String sort;
    private final String q;
    private final boolean latest;
    private final boolean running;
    private final String buildConfigName;

}
