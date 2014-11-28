package org.jboss.pnc.integration.deployments;

import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

import java.io.File;
import java.util.stream.Stream;

public class Deployments {

    public static EnterpriseArchive baseEar() {
        EnterpriseArchive webArchive = ShrinkWrap.create(EnterpriseArchive.class);

        PomEquippedResolveStage mavenResolver = Maven.resolver().loadPomFromFile(new File("pom.xml"));
        File[] manuallyAddedLibs = mavenResolver.resolve("org.jboss.pnc:ear-package:ear:1.0-SNAPSHOT").withoutTransitivity().asFile();
        Stream.of(manuallyAddedLibs).forEach(lib -> webArchive.merge(ShrinkWrap.create(ZipImporter.class).
                importFrom(lib).as(GenericArchive.class)));

        return webArchive;
    }
}
