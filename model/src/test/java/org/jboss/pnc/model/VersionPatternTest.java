/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class VersionPatternTest {


    @Test
    public void testMilestoneVersionPattern() {
        Pattern pattern = Pattern.compile(ProductMilestone.VERSION_PATTERN);

        Assert.assertTrue(pattern.matcher("1.2.3.Final").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final_1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final-1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.CR1.CD2").matches());
        Assert.assertTrue(pattern.matcher("1.2.CR1.CD2").matches());
        Assert.assertTrue(pattern.matcher("1.0.0-CD1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3").matches());
        Assert.assertTrue(pattern.matcher("1.2.CR1.3").matches());

        Assert.assertFalse(pattern.matcher("1.0").matches());
        Assert.assertFalse(pattern.matcher("1.0.").matches());
        Assert.assertFalse(pattern.matcher("1.3.-").matches());
        Assert.assertFalse(pattern.matcher("1.2.3.-").matches());
    }

    @Test
    public void testProductReleaseVersionPattern() {
        Pattern pattern = Pattern.compile(ProductRelease.VERSION_PATTERN);

        Assert.assertTrue(pattern.matcher("1.2.3.Final").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final_1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final-1").matches());
        Assert.assertFalse(pattern.matcher("1.2.3.CR1.CD2").matches());
        Assert.assertFalse(pattern.matcher("1.2.3").matches());
    }
}