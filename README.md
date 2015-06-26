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
* `PNC_DOCKER_PROXY_SERVER` - IP address or hostname of proxy server
* `PNC_DOCKER_PROXY_PORT` - port of proxy server where it is listening
* `PNC_DOCKER_IMAGE_FIREWALL_ALLOWED` - List of allowed destinations by firewall in Docker container. <br /> Format: \<IPv4>:\<Port>(,\<IPv4>:\<Port>)+
You can set it to "all" and network isolation will be skipped, in case of not setting it up at all
all network traffic will be dropped
* `PNC_EXT_REST_BASE_URL` - Base URL of REST endpoint services to be accessed from external resources
* `PNC_EXT_OAUTH_USERNAME` - Username to be able to authenticate against pnc authentication service provider
* `PNC_EXT_OAUTH_PASSWORD` -  Password to be able to authenticate against pnc authentication service provider
* `PNC_BPM_USERNAME` -  Username user to authenticate against remote BPM server for build signal callbacks
* `PNC_BPM_PASSWORD` -  Password for `PNC_BPM_USERNAME`


Set up of Docker host
------------
This part describes an expected way how to set up host with running Docker daemon with systemd.
Currently is used Docker daemon, which listens on unprotected Docker control socket (port 2375).

Steps to set up Docker daemon:

1. Install docker with `yum install docker-io` (the package on Fedora 22 was renamed to _docker_)
2. Edit /etc/sysconfig/docker file to enable tcp connection, using external data storage on disk outside root filesystem and you can set up an additional docker image registry to official hub.docker.com:

```
OPTIONS='--selinux-enabled -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock -g /mnt/docker/data'
INSECURE_REGISTRY='--insecure-registry <your-internal-remote-docker-registry>'
```

3. Enable `docker` service: Run `sudo systemctl enable docker`
4. Start `docker` service: Run `sudo systemctl start docker`
5. Verify the service: Run `docker -H tcp://127.0.0.1:2375 version`. If you get response in 1-2 seconds without errors, the service is running.
6. Add image to Docker daemon: The Docker daemon has to have imported image, which is specified by environment variable `PNC_DOCKER_IMAGE_ID` (or is set in pnc-config.json file) You can use `docker pull` to download image from remote repository or `docker build` to create image from Dockerfile.


##Authentication:
This project comes with possibility to be secured. Security is delivered via Keycloak project http://keycloak.jboss.org/.
To be able to turn on whole project on secure side you need 2 parts to fulfill.
1. **Have running and configured Keycloak server instance.** <br/>
2. **Build & configure security for your PNC installation.**

###Authentication - prepare Keycloak server<br/>
**PRE-REQUIREMENTS:** <br/>

Install your Keycloak server standalone or in Openshift according to 
* http://keycloak.github.io/docs/userguide/html/server-installation.html.
* http://keycloak.github.io/docs/userguide/html/openshift.html
<br/>

**Configure Keycloak server**<br/>

1. Create your realm 
By default the Keycloak server comes with "master realm", which is for demo purpose, so please create your own realm.
<br/>
2. Put your Direct Grant API on at https://`<your-server>`/auth/admin/master/console/#/realms/`<your-realm>`/login-settings
<br/>
3. Add/Create your users via https://`<your-server>`/auth/admin/master/console/#/realms/`<your-realm>`/users
<br/>
4. Define roles and assign users to it via https://`<your-server>`/auth/admin/master/console/#/realms/`<your-realm>`/roles
<br/>
5. Create 3 client app's at https://`<your-server>`/auth/admin/master/console/#/realms/`<your-realm>`/clients 
<br/>
6. First client app for pncweb UI with:
  * Client Protocol = openid-connect
  * Access Type = confidental
  * Valid Redirect URIs = http://localhost:8080/pnc-web/*  (Please add URI's for different host's as you need for your installed pnc Web UI's)
  * Go to "Installation" tab and select "Keycloak JSON" format and copy&paste or download the installation.
7. Second client app for pncrest with:
  * Client Protocol = openid-connect
  * Access Type = confidental
  * Valid Redirect URIs = http://localhost:8080/pnc-rest/*  (Please add as much URI's as you need for your installed pnc rest's)
  * Go to "Installation" tab and select "Keycloak JSON" format and copy&paste or download the installation.
8. Third client app for pncdirect with:
  * Client Protocol = openid-connect
  * Direct Grants Only = ON
  * Access Type = public
  * Go to "Installation" tab and select "Keycloak JSON" format and copy&paste or download the installation.
<br/>

**HINTS** <br/>

1. pncrest keycloak.json additional props -> use these below for skipping ssl & defining rest for accepting only access_token for authentication/authorization.
  * "ssl-required": "none",
  * "bearer-only" : true,
2. pncweb keycloak.json additional props -> look at http://keycloak.github.io/docs/userguide/html/ch08.html#adapter-config to find more about adapter's config.
  * "ssl-required" : "none",
  * "use-resource-role-mappings" : false,
  * "enable-basic-auth" : false,
  * "enable-cors" : true,
  * "cors-max-age" : 1000,
  * "cors-allowed-methods" : "POST,PUT,DELETE,GET",
  * "bearer-only" : false,

         

###Authentication - build & configure your PNC<br/>
By default PNC project comes with no security at all, it is up to you to turn it on.
In case you want to enable authentication use `-Dauth=true` together with your build command.
Enabling authentication means following<br/>

1. Your backend REST endpoints will become secured
  * inside rest.war under folder WEB-INF are added files from rest/src/main/auth
  * keycloak.json file is configuration file managing connection to Keycloak server
  * web.xml file where you define security-constraints & security-roles, which specifies users authrorization's to each REST endpoint
2. Your pnc web UI gain the SSO ability and authentication via Keycloak login page.
  * with your first unauthenticated session you will be redirected from pnc web UI into Keycloak login page.
  * you will be asked to provide your credentials.
  * after successful log-in you will be redirected back to pnc web UI.
3. Configure your JEE server (EAP) for keycloak
  * Use -Dauth.eap.home=``<path to your EAP installation>`` with you build command, if you want EAP configure for Keycloak.
  According the http://docs.jboss.org/keycloak/docs/1.1.0.Final/userguide/html/ch08.html#jboss-adapter-installation installation will be performed on server for the given path.



Possible issues:
------------
* It is not possible to create Docker environment, because the client cannot connect to Docker host using SSH. Solution: You have to  allow using strong ciphers in JCE (http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)


Main Modules
------------
* `datastore`: Implementation of spi:org.jboss.pnc.spi.datastore
* `jenkins-build-driver`: Implementation of spi:org.jboss.pnc.spi.builddriver
* `maven-repository-manager`: Implementation of spi:org.jboss.pnc.spi.repositorymanager
* `build-coordinator`: Contains implementations of action-controllers, which include the business logic for orchestrating builds, test runs, etc. Action controllers are used to isolate logic from the REST API, so it can be reused in embedded scenarios
* `model`: Contains domain model for the orchestrator. This is just model classes + serialization helpers, and would also be suitable for writing a java client api to support integration
* `rest`: REST API. This is a series of classes that use JAX-RS to translate HTTP communications to calls into the action controllers in the core, and format any output (such as constructing resource URLs, etc.)
* `spi`: Contains all SPI interfaces the orchestrator will use to coordinate its sub-services for provisioning environments and repositories, triggering builds, storing domain objects. It is meant to be used in conjunction with pnc-model
* `processes`: Contains jBPM processes for PNC
* `web`: Contains Web UI resoures (html + js pages, images etc.)

Building with Postgresql
-----------------------
A Maven profile called `postgresql` is provided to configure the appropriate settings to build a deployment file which is compatible with the postgresql database.

    mvn install -Ppostgresql

The container tests can also be run against postgresql by activating the `container-tests` profile, the `postgresql` profile, and the `postgresql-container-tests` profile.

    mvn install -Ppostgresql,container-tests,postgresql-container-tests
