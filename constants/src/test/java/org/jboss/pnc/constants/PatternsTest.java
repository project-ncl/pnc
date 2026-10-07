/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.constants;

import static org.jboss.pnc.constants.Patterns.PRODUCT_MILESTONE_VERSION;
import static org.jboss.pnc.constants.Patterns.PRODUCT_RELEASE_VERSION;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PatternsTest {

    @Test
    public void testMilestoneVersionPattern() {
        Pattern pattern = Pattern.compile(PRODUCT_MILESTONE_VERSION);

        Assert.assertTrue(pattern.matcher("1.2.3.Final").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final_1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final-1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.CR1.CD2").matches());
        Assert.assertTrue(pattern.matcher("1.2.CR1.CD2").matches());
        Assert.assertTrue(pattern.matcher("1.2.Final").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.CR1.CD2.ER1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3").matches());
        Assert.assertTrue(pattern.matcher("1.0.0-CD1").matches());
        Assert.assertTrue(pattern.matcher("1.2.CR1.3").matches());

        Assert.assertFalse(pattern.matcher("1.CR1").matches());
        Assert.assertFalse(pattern.matcher("1.0").matches());
        Assert.assertFalse(pattern.matcher("1.0.").matches());
        Assert.assertFalse(pattern.matcher("1.3.-").matches());
        Assert.assertFalse(pattern.matcher("1.2.3.-").matches());
    }

    @Test
    public void testProductReleaseVersionPattern() {
        Pattern pattern = Pattern.compile(PRODUCT_RELEASE_VERSION);

        Assert.assertTrue(pattern.matcher("1.2.3.Final").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final_1").matches());
        Assert.assertTrue(pattern.matcher("1.2.3.Final-1").matches());
        Assert.assertFalse(pattern.matcher("1.2.3.CR1.CD2").matches());
        Assert.assertFalse(pattern.matcher("1.2.3").matches());
    }
}
