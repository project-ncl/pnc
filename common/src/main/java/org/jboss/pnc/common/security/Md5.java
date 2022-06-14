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
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @deprecated use pnc-common lib
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Deprecated
public class Md5 {

    private MessageDigest md;

    public Md5() throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance("MD5");
    }

    public static String digest(String message) throws NoSuchAlgorithmException, IOException {
        return CheckSum.calculateDigest(message, "MD5");
    }

    public void add(String message) throws UnsupportedEncodingException {
        md.update(message.getBytes(StandardCharsets.UTF_8));
    }

    public String digest() {
        byte[] digest = md.digest();
        return CheckSum.format(digest);
    }

}
