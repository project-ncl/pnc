/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @deprecated use pnc-common lib
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Deprecated
public class Sha256 {

    MessageDigest md;

    public Sha256() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("SHA-256");
    }

    public static String digest(String message) throws NoSuchAlgorithmException, IOException {
        return CheckSum.calculateDigest(message, "SHA-256");
    }

    public void add(String message) throws UnsupportedEncodingException {
        md.update(message.getBytes(StandardCharsets.UTF_8));
    }

    public String digest() {
        byte[] digest = md.digest();
        return CheckSum.format(digest);
    }

}
