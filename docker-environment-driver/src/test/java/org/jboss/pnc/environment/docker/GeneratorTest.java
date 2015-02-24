package org.jboss.pnc.environment.docker;

import static org.junit.Assert.*;

import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests of Generator class for DockerEnvironmentDriver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@RunWith(Arquillian.class)
public class GeneratorTest {

    private static final Logger log = Logger.getLogger(GeneratorTest.class.getName());

    private final String STANDARD_CONTAINER_ID_PREFIX = "PNC-jenkins-container-";

    private final int FIRST_JENKINS_PORT = 20000;

    private final int FIRST_SSH_PORT = 30000;

    @Inject
    private Generator generator;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap
                .create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClass(Generator.class);

        log.info("Deployment: " + jar.toString(true));
        return jar;
    }

    @Before
    public void initGenerator() {
        assertNotNull(generator);
        generator.reInit();
    }

    @Test
    public void generateContainerIdTest() {
        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "1", generator.generateContainerId());
        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "2", generator.generateContainerId());
        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "3", generator.generateContainerId());

        // Test if the values are bounded
        for (int i = 0; i < 10000 - 3; i++)
            generator.generateContainerId();

        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "1", generator.generateContainerId());
    }

    @Test
    public void generateJenkinsPortTest() {
        assertEquals(FIRST_JENKINS_PORT, generator.generateJenkinsPort());
        assertEquals(FIRST_JENKINS_PORT + 1, generator.generateJenkinsPort());
        assertEquals(FIRST_JENKINS_PORT + 2, generator.generateJenkinsPort());

        // Test if the values are bounded
        for (int i = 0; i < 10000 - 3; i++)
            generator.generateJenkinsPort();

        assertEquals(FIRST_JENKINS_PORT, generator.generateJenkinsPort());
    }

    @Test
    public void generateSshPortTest() {
        assertEquals(FIRST_SSH_PORT, generator.generateSshPort());
        assertEquals(FIRST_SSH_PORT + 1, generator.generateSshPort());
        assertEquals(FIRST_SSH_PORT + 2, generator.generateSshPort());

        // Test if the values are bounded
        for (int i = 0; i < 10000 - 3; i++)
            generator.generateSshPort();

        assertEquals(FIRST_SSH_PORT, generator.generateSshPort());
    }

    @Test
    public void forceNextValuesTest() {
        int sshPort = 10;
        int jenkinsPort = 10;
        generator.forceNextValues(sshPort, jenkinsPort, "TestId-");
        assertEquals(sshPort, generator.generateSshPort());
        assertEquals(jenkinsPort, generator.generateJenkinsPort());
        assertEquals("TestId-1", generator.generateContainerId());
    }

    @Test
    public void reInitTest() {
        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "1", generator.generateContainerId());
        assertEquals(FIRST_JENKINS_PORT, generator.generateJenkinsPort());
        assertEquals(FIRST_SSH_PORT, generator.generateSshPort());
        generator.reInit();
        assertEquals(STANDARD_CONTAINER_ID_PREFIX + "1", generator.generateContainerId());
        assertEquals(FIRST_JENKINS_PORT, generator.generateJenkinsPort());
        assertEquals(FIRST_SSH_PORT, generator.generateSshPort());
    }

}
