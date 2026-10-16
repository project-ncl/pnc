/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.limits;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.junit.Test;

public class PageInfoTest {

    private final DefaultPageInfoProducer defaultPageInfoProducer = new DefaultPageInfoProducer();

    @Test
    public void shouldReturnCustomLimits() throws Exception {
        // given
        int size = 12;
        int offset = 13;

        // when
        PageInfo testedLimits = defaultPageInfoProducer.getPageInfo(offset, size);

        // then
        assertThat(testedLimits.getPageOffset()).isEqualTo(13);
        assertThat(testedLimits.getPageSize()).isEqualTo(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNegativeSize() throws Exception {
        // given
        int size = -12;
        int offset = 0;

        // when
        defaultPageInfoProducer.getPageInfo(offset, size);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNegativeOffset() throws Exception {
        // given
        int size = 1;
        int offset = -112;

        // when
        defaultPageInfoProducer.getPageInfo(offset, size);
    }

}