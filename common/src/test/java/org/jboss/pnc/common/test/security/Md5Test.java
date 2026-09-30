/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.test.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.jboss.pnc.common.security.Md5;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Md5Test {

    @Test
    public void calculateMd5() throws IOException, NoSuchAlgorithmException {
        String encoded = Md5.digest("The quick brown fox jumps over the lazy dog.");
        String expected = "e4d909c290d0fb1ca068ffaddf22cbd0";

        Assert.assertEquals("Md5 should be 32 chars long.", 32, encoded.length());
        Assert.assertEquals(expected, encoded);
    }

    @Test
    public void testWithLeadingZeroInTheSum() throws IOException, NoSuchAlgorithmException {
        String encoded = Md5.digest("363");
        String expected = "00411460f7c92d2124a67ea0f4cb5f85";
        Assert.assertEquals(expected, encoded);

        encoded = Md5.digest("a");
        expected = "0cc175b9c0f1b6a831c399e269772661";
        Assert.assertEquals(expected, encoded);
    }

    @Test
    public void addingToMd5() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expected = "e4d909c290d0fb1ca068ffaddf22cbd0";

        Md5 md5 = new Md5();
        md5.add("The quick brown fox ");
        md5.add("jumps over the lazy dog.");
        String encoded = md5.digest();

        Assert.assertEquals("Md5 should be 32 chars long.", 32, encoded.length());
        Assert.assertEquals(expected, encoded);
    }
}
