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
