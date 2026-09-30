/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.security;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @deprecated use pnc-common lib
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Deprecated
public class CheckSum {
    static String calculateDigest(String message, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(algorithm);

        StringReader reader = new StringReader(message);
        char[] buffer = new char[1024];

        while (true) {
            int read = reader.read(buffer);
            if (read == -1) {
                break;
            }
            md.update(String.valueOf(buffer, 0, read).getBytes(StandardCharsets.UTF_8));
        }

        byte[] digest = md.digest();
        return format(digest);
    }

    static String format(byte[] digest) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
