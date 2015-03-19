#Project-ncl

Building Project
----------------
Requirements:

* JDK 8
* Maven 3.2

Command line arguments:

The default build is executed by running `mvn clean install`.<br />
By default the tests that require remote services and integration tests are disabled.<br />
In order to run remote and integration tests you have to specify remote services location and credentials by editing configuration file `common/src/main/resources/pnc-config.json`.<br />
By default the configuration file uses env variables, you can set required variables (see file for list of them) instead of editing the file itself.<br />
If you want to use a different (external) config file location you can define a path to it with `-Dpnc-config-file=/path/to/pnc-config.json`.

Remote tests are defined by class name *RemoteTest.java<br />
To run remote test use `mvn clean install -DremoteTest=true`

Integration tests are placed in module "integrations-tests" to run them use `-Pintegration-test`.

To run remote and integration tests combine both commands `mvn clean install -DremoteTest=true -Pintegration-test`.

Environment variables, which can be used to set up application:

* `PNC_APROX_URL` - URL to AProx repository
* `PNC_DOCKER_IP` - IP address of host with Docker daemon
* `PNC_DOCKER_CONT_USER` - User account in image used in Docker
* `PNC_DOCKER_CONT_PASSWORD` - User's password set up by variable `PNC_DOCKER_CONT_USER`
* `PNC_DOCKER_IMAGE_ID` - ImageID of image on Docker host
* `PNC_DOCKER_IMAGE_FIREWALL_ALLOWED` - List of allowed destinations by firewall in Docker container. <br /> Format: \<IPv4>:\<Port>(,\<IPv4>:\<Port>)+


Set up of Docker host
------------
This part describes an expected way how to set up host with running Docker daemon.
Currently is used Docker daemon, which listens on unprotected Docker control socket (port 2375).

Steps to set up Docker daemon:

1. Prepare socket config: Create file `/etc/systemd/system/docker-tcp.socket` with content: <br />
    [Unit] <br />
    Description=Docker Socket for the API <br /><br />
    [Socket] <br />
    ListenStream=2375 <br />
    Service=docker.service <br /><br />
    [Install] <br />
    WantedBy=sockets.target 
2. Enable `docker-tcp` service: Run `sudo systemctl enable docker-tcp.socket`
3. Start `docker-tcp` service: Run `sudo systemctl start docker-tcp.socket`
4. Start `docker` service: Run `sudo systemctl start docker`
5. Verify the service: Run `docker -H tcp://127.0.0.1:2375 version`. If you get response in 1-2 seconds without errors, the service is running.
6. Add image to Docker daemon: The Docker daemon has to have imported image, which is specified by environment variable `PNC_DOCKER_IMAGE_ID` (or is set in pnc-config.json file) You can use `docker pull` to download image from remote repository or `docker build` to create image from Dockerfile. 


Possible issues:
------------
* It is not possible to create Docker environment, because the client cannot connect to Docker host using SSH. Solution: You have to  allow using strong ciphers in JCE (http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)


Main Modules
------------
* `datastore`: Implementation of pnc-spi:org.jboss.pnc.spi.datastore
* `jenkins-build-driver`: Implementation of pnc-spi:org.jboss.pnc.spi.builddriver
* `maven-repository-manager`: Implementation of pnc-spi:org.jboss.pnc.spi.repositorymanager
* `pnc-core`: Contains implementations of action-controllers, which include the business logic for orchestrating builds, test runs, etc. Action controllers are used to isolate logic from the REST API, so it can be reused in embedded scenarios
* `pnc-model`: Contains domain model for the orchestrator. This is just model classes + serialization helpers, and would also be suitable for writing a java client api to support integration
* `pnc-rest-bindings`: REST API. This is a series of classes that use JAX-RS to translate HTTP communications to calls into the action controllers in the core, and format any output (such as constructing resource URLs, etc.)
* `pnc-spi`: Contains all SPI interfaces the orchestrator will use to coordinate its sub-services for provisioning environments and repositories, triggering builds, storing domain objects. It is meant to be used in conjunction with pnc-model
* `pnc-web`: Contains Web UI resoures (html + js pages, images etc.)

