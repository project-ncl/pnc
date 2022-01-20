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
package org.jboss.pnc.termdbuilddriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 2/29/16 Time: 2:01 PM
 */
public class ZipUtils {
    public static void unzipToDir(Path dir, String resourcePath) throws IOException {
        InputStream repoZipStream = ZipUtils.class.getResourceAsStream(resourcePath);
        ZipInputStream zipStream = new ZipInputStream(repoZipStream);

        ZipEntry entry;
        while ((entry = zipStream.getNextEntry()) != null) {
            if (!entry.isDirectory()) {
                unzipFile(dir, entry, zipStream);
            }
        }
    }

    private static void unzipFile(Path targetDir, ZipEntry entry, ZipInputStream stream) throws IOException {
        String name = entry.getName();
        File outFile = new File(targetDir.toFile(), name);
        Path parent = outFile.toPath().getParent();
        if (!Files.exists(parent) && !parent.toFile().mkdirs()) {
            throw new IllegalStateException("Unable to create directory: " + parent);
        }
        byte[] buffer = new byte[10000];
        if (!outFile.createNewFile()) {
            throw new IllegalStateException("Unable to create file: " + outFile);
        }
        int read;
        try (FileOutputStream outputStream = new FileOutputStream(outFile)) {
            while ((read = stream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, read);
            }
        }
    }
}
