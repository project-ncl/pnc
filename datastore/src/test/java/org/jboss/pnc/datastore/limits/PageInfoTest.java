/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.limits;

import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectZeroSize() throws Exception {
        // given
        int size = 0;
        int offset = 0;

        // when
        defaultPageInfoProducer.getPageInfo(offset, size);
    }

}