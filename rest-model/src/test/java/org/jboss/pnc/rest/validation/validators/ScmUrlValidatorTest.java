/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.validation.validators;

import org.junit.Test;

import java.util.stream.Stream;

import static org.jboss.pnc.rest.validation.validators.ScmUrlValidator.isValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 3/9/16
 * Time: 1:51 PM
 */
public class ScmUrlValidatorTest {


    String[] validUrls = new String[]{
            "git@github.com:michalszynkiewicz/dev-utils",
            "github.com:michalszynkiewicz/dev-utils",

            "ssh://michal.szynkiewicz@github.com/michalszynkiewicz/dev-utils",
            "ssh://michal.szynkiewicz:passw4%20rd@github.com/michalszynkiewicz/dev-utils",
            "ssh://git@github.com:22/infinispan/infinispan",
            "ssh://github.com/michalszynkiewicz/dev-utils",

            "https://github.com/project-ncl/pnc",
            "https://github.com:443/project-ncl/pnc",

            "http://github.com/project-ncl/pnc",
            "http://github.com:80/michalszynkiewicz/dev-utils",

            "git://github.com/infinispan/infinispan",
            "git://github.com:9418/infinispan/infinispan"
    };

    String[] invalidUrls = new String[]{
            "github. com:michalszynkiewicz/dev-utils",
            "very invalid",
            ":O",
            "funny://url.git",
            "https:/github.com/project-ncl/pnc",
            "htts://github.com:443/project-ncl/pnc",
            "http//github.com/project-ncl/pnc",
            "giit://github.com/infinispan/infinispan",
            "git//github.com:9418/infinispan/infinispan",
            "ssh://mi chalszynkiewicz@github.com/michalszynkiewicz/dev-utils",

    };

    @Test
    public void shouldAcceptValidUrls() {
        for (final String validUrl : validUrls) {
            Stream.of("", ".git").forEach(
                    suffix -> {
                        String url = validUrl + suffix;
                        assertTrue("Valid url found invalid: " + validUrl, isValid(validUrl));
                        url = url.replace("github.com", "192.30.252.131");
                        assertTrue("Valid url found invalid: " + validUrl, isValid(validUrl));
                    }
            );
        }
    }

    @Test
    public void shouldNotAcceptInvalidUrls() {
        for (String invalidUrl : invalidUrls) {
            assertFalse("Invalid url found valid: " + invalidUrl, isValid(invalidUrl));
        }
    }
}