package org.jboss.pnc.environment.docker;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for DockerEnnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(Arquillian.class)
public class DockerEnvironmentDriverTest {

    @Inject
    private DockerEnvironmentDriver dockerEnvDriver;
    
    @Inject 
    private Generator generator;
    
    private static final Logger log = Logger.getLogger(DockerEnvironmentDriverTest.class.getName());

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive testedEjb = ShrinkWrap
                .create(WebArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("jenkins-maven-config.xml")
                .addPackages(true, DockerEnvironmentDriver.class.getPackage());

        JavaArchive[] libs = Maven.configureResolver()
                .withMavenCentralRepo(true)
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .as(JavaArchive.class);

        testedEjb.addAsLibraries(libs);

        log.info("Deployment: " + testedEjb.toString(true));
        return testedEjb;
    }

    @Test
    public void injectedTypeTest() {
        assertNotNull(dockerEnvDriver);
    }

    @Test
    public void buildEnvironmentTest() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
//        dockerEnvDriver.

         
    }

}
