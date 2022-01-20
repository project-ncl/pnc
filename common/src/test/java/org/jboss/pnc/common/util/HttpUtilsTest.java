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
package org.jboss.pnc.common.util;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class HttpUtilsTest {

    @Test
    public void testIsSuccess() {
        Assertions.assertThat(HttpUtils.isSuccess(200)).isTrue();
        Assertions.assertThat(HttpUtils.isSuccess(201)).isTrue();
        Assertions.assertThat(HttpUtils.isSuccess(202)).isTrue();
        Assertions.assertThat(HttpUtils.isSuccess(226)).isTrue();
    }

}
