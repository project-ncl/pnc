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
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.impl.base.path.PathUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import static org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils.addManifestDependencies;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ManifestTest {

    @Test
    public void shouldCreateManifestWithDependencyDefinition() {
        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "datastore-test.ear");
        String dependency = "com.google.guava";
        addManifestDependencies(ear, dependency);
        Node node = ear.get(PathUtil.composeAbsoluteContext("META-INF", "MANIFEST.MF"));

        Manifest manifest;
        try (InputStream inputStream = node.getAsset().openStream()) {
            manifest = new Manifest(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Cannot read MANIFEST.MF", e);
        }
        String dependencies = manifest.getMainAttributes().getValue("Dependencies");
        Assert.assertEquals(dependency, dependencies);
    }
}
