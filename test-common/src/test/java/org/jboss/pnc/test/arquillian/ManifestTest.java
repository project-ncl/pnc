/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.test.arquillian;

import static org.jboss.pnc.test.arquillian.ShrinkwrapDeployerUtils.addManifestDependencies;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.impl.base.path.PathUtil;
import org.junit.Assert;
import org.junit.Test;

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
