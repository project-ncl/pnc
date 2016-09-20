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
package org.jboss.pnc.common.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Sha256 {

    MessageDigest md;

    public Sha256() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("SHA-256");
    }

    public static String digest(String message)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(message.getBytes("UTF-8"));
        byte[] digest = md.digest();
        return format(digest);
    }

    public void add(String message) throws UnsupportedEncodingException {
        md.update(message.getBytes("UTF-8"));
    }

    public String digest() {
        byte[] digest = md.digest();
        return format(digest);
    }

    private static String format(byte[] digest) {
        return String.format("%064x", new java.math.BigInteger(1, digest));
    }

}
