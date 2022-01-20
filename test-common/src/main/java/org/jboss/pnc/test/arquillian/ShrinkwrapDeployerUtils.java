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

package org.jboss.pnc.test.arquillian;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.asset.ZipFileEntryAsset;
import org.jboss.shrinkwrap.impl.base.path.PathUtil;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ShrinkwrapDeployerUtils {

    private static Logger logger = LoggerFactory.getLogger(ShrinkwrapDeployerUtils.class);

    public static void addPomLibs(JavaArchive jar, String gav) {
        JavaArchive[] libs = Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve(gav)
                .withTransitivity()
                .as(JavaArchive.class);
        for (JavaArchive lib : libs) {
            jar.merge(lib);
        }
    }

    /**
     * Adds the dependencies to MANIFEST.MF. If there is no MANIFEST.MF it is created. If there is an existing
     * MANIFEST.MF with the dependencies, new dependencies are appended.
     *
     * @param ear
     * @param dependencies to add to the manifest
     */
    public static void addManifestDependencies(EnterpriseArchive ear, String... dependencies) {
        if (dependencies.length == 0) {
            return;
        }
        Node node = ear.get(PathUtil.composeAbsoluteContext("META-INF", "MANIFEST.MF"));
        Manifest newManifest;
        String existingDependencies;
        if (node != null) {
            ZipFileEntryAsset manifest = (ZipFileEntryAsset) node.getAsset();
            try (InputStream inputStream = manifest.openStream()) {
                newManifest = new Manifest(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Cannot read MANIFEST.MF", e);
            }
            existingDependencies = newManifest.getMainAttributes().getValue("Dependencies") + ", ";
        } else {
            newManifest = new Manifest();
            String vername = Attributes.Name.MANIFEST_VERSION.toString();
            newManifest.getMainAttributes().putValue(vername, "1.0");
            existingDependencies = "";
        }
        String newDependencies = Arrays.stream(dependencies).collect(Collectors.joining(", "));
        newManifest.getMainAttributes().putValue("Dependencies", existingDependencies + newDependencies);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            newManifest.write(bos);
            String manifestContent = new String(bos.toByteArray(), StandardCharsets.UTF_8);
            logger.debug("MANIFEST.MF: {}", manifestContent);
            ear.addAsManifestResource(new StringAsset(manifestContent), "MANIFEST.MF");
        } catch (IOException e) {
            throw new RuntimeException("Cannot write MANIFEST.MF", e);
        }
    }
}
