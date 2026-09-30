/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.test.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.jboss.pnc.common.security.Sha256;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Sha256Test {

    @Test
    public void calculateSha256() throws IOException, NoSuchAlgorithmException {
        String encoded = Sha256.digest("The quick brown fox jumps over the lazy dog.");
        String expected = "ef537f25c895bfa782526529a9b63d97aa631564d5d789c2b765448c8635fb6c";

        Assert.assertEquals("Sha 256 should be 64 chars long.", encoded.length(), 64);
        Assert.assertEquals(expected, encoded);
    }

    @Test
    public void testWithLeadingZeroInTheSum() throws IOException, NoSuchAlgorithmException {
        String encoded = Sha256.digest("886");
        String expected = "000f21ac06aceb9cdd0575e82d0d85fc39bed0a7a1d71970ba1641666a44f530";
        Assert.assertEquals(expected, encoded);

        encoded = Sha256.digest("96952");
        expected = "00064ea7e7d6798cc16d9e7723150ee9a170416f05a61b7d45edd2c28ecd69f6";
        Assert.assertEquals(expected, encoded);
    }

    @Test
    public void addingToSha256() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String expected = "ef537f25c895bfa782526529a9b63d97aa631564d5d789c2b765448c8635fb6c";

        Sha256 sha256 = new Sha256();
        sha256.add("The quick brown fox ");
        sha256.add("jumps over the lazy dog.");
        String encoded = sha256.digest();

        Assert.assertEquals("Sha 256 should be 64 chars long.", encoded.length(), 64);
        Assert.assertEquals(expected, encoded);
    }
}
