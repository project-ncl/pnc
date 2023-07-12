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
package org.jboss.pnc.common.validator;

import junit.framework.TestCase;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NoHtmlValidatorTest {
    @Test
    public void testValidator() {
        NoHtmlValidator validator = new NoHtmlValidator();
        assertThat(validator.isValid("hello", null)).isTrue();
        assertThat(validator.isValid("<alert>hello</alert>", null)).isFalse();
        assertThat(validator.isValid("<script>hello</script>", null)).isFalse();
        assertThat(validator.isValid("<?php hello>", null)).isFalse();
        assertThat(validator.isValid("hello heya <div>asdf</div>", null)).isFalse();
        assertThat(validator.isValid("hello heya <a href=\"heloo\" />", null)).isFalse();

        // null has no html!
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid("", null)).isTrue();
    }
}