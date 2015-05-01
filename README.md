#Project-ncl

Building Project
----------------
Requirements:

* JDK 8
* Maven 3.2


Building
--------
The default build is executed by running `mvn clean install`.<br />
By default the tests that require remote services and integration tests are disabled.<br />

Integration tests are placed in module "integration-test" and most of them needs a JEE server (Wildfly or EAP).
In order to run them you need to specify `-Pcontainer-tests`.

Remote tests requires turning on additional Maven profile - `-Premote-tests`.
In order to run remote and integration tests you have to specify remote services location and credentials by editing configuration file `common/src/main/resources/pnc-config.json` and use both profiles (`-Pcontainer-tests` and `-Premote-tests`)<br />
By default the configuration file uses env variables, you can set required variables (see file for list of them) instead of editing the file itself.<br />
If you want to use a different (external) config file location you can define a path to it with `-Dpnc-config-file=/path/to/pnc-config.json`.


Integration and Container tests
-------------------------------
There is a slight difference between **integration** and **container** test. By a **container** test we understand a test which needs a JEE server to run.
An **integration** test checks if several modules work correctly together.


Environmental variables
-----------------------

Environment variables, which can be used to set up application:

* `PNC_JENKINS_USERNAME` - Username of user created in Jenkins server inside the Docker container
* `PNC_JENKINS_PASSWORD` - Password of user specified with `PNC_JENKINS_USERNAME`
* `PNC_JENKINS_URL` - URL of Jenkins instance dedicated or in docker container
* `PNC_JENKINS_PORT` - Port of Jenkins dedicated or in docker container specified with `PNC_JENKINS_URL`
* `PNC_APROX_URL` - URL to AProx repository
* `PNC_DOCKER_IP` - IP address of host with Docker daemon
* `PNC_DOCKER_CONT_USER` - User account in image used in Docker
* `PNC_DOCKER_CONT_PASSWORD` - User's password set up by variable `PNC_DOCKER_CONT_USER`
* `PNC_DOCKER_IMAGE_ID` - ImageID of image on Docker host
* `PNC_DOCKER_IMAGE_FIREWALL_ALLOWED` - List of allowed destinations by firewall in Docker container. <br /> Format: \<IPv4>:\<Port>(,\<IPv4>:\<Port>)+
You can set it to "all" and network isolation will be skipped, in case of not setting it up at all
all network traffic will be dropped
* `PNC_EXT_REST_BASE_URL` - Base URL of REST endpoint services to be accessed from external resources
* `PNC_EXT_OAUTH_USERNAME` - Username to be able to authenticate against pnc authentication service provider
* `PNC_EXT_OAUTH_PASSWORD` -  Password to be able to authenticate against pnc authentication service provider


Set up of Docker host
------------
This part describes an expected way how to set up host with running Docker daemon with systemd.
Currently is used Docker daemon, which listens on unprotected Docker control socket (port 2375).

Steps to set up Docker daemon:

1. Install docker with `yum install docker-io`
2. Edit /etc/sysconfig/docker file to enable tcp connection, using external data storage on disk outside root filesystem and you can set up an additional docker image registry to official hub.docker.com:

```
OPTIONS='--selinux-enabled -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock -g /mnt/docker/data'
INSECURE_REGISTRY='--insecure-registry <your-internal-remote-docker-registry>'
```

3. Enable `docker` service: Run `sudo systemctl enable docker`
4. Start `docker` service: Run `sudo systemctl start docker`
5. Verify the service: Run `docker -H tcp://127.0.0.1:2375 version`. If you get response in 1-2 seconds without errors, the service is running.
6. Add image to Docker daemon: The Docker daemon has to have imported image, which is specified by environment variable `PNC_DOCKER_IMAGE_ID` (or is set in pnc-config.json file) You can use `docker pull` to download image from remote repository or `docker build` to create image from Dockerfile.


Authentication:
---------------
The default build with command `mvn clean install` comes with no authentication. In case you want to enable authentication
use -Dauth=true together with your build command.
Enabling authentication meand following
1. Your backend REST endpoints will become secured
    - inside pnc-rest.war under folder WEB-INF are added files from /pnc-rest/src/main/auth
    - keycloak.json file is configuration file managing connection to Keycloak server
    - web.xml file where you define security-constraints & security-roles, which specifies users
      authrorization's to each REST endpoint
2. Your pnc web UI gain the SSO ability and authentication via Keycloak login page.
    - with your first unauthenticated session you will be redirected from pnc web UI into
      Keycloak login page and asked to provide your credentials. After successful log-in you
      will be redirected back to pnc web UI.

Configure your JEE server (EAP) for keycloak
 Use -Dauth.eap.home=<path to your EAP installation> with you build command, if you want EAP configure for Keycloak.
 According the http://docs.jboss.org/keycloak/docs/1.1.0.Final/userguide/html/ch08.html#jboss-adapter-installation installation
 will be performed on server for the given path.



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
* `pnc-processes`: Contains jBPM processes for PNC
* `pnc-web`: Contains Web UI resoures (html + js pages, images etc.)

Building with Postgresql
-----------------------
A Maven profile called `postgresql` is provided to configure the appropriate settings to build a deployment file which is compatible with the postgresql database.

    mvn install -Ppostgresql

The container tests can also be run against postgresql by activating the `container-tests` profile, the `postgresql` profile, and the `postgresql-container-tests` profile.

    mvn install -Ppostgresql,container-tests,postgresql-container-tests
