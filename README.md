Just testing flaky test
hahaha
Project-ncl
===========
A system for managing, executing, and tracking cross-platform builds.


## Running the PNC

PNC is composed of multiple services, to run the system you need:

Minimal:
- PNC Orchestrator (this repo)
- [Indy repository manager](https://github.com/Commonjava/indy)
- [OpenShift](https://www.openshift.org/)
- [RHSSO](https://access.redhat.com/products/red-hat-single-sign-on) / [KeyCloak](http://keycloak.jboss.org/)
- [PostgreSQL database](https://www.postgresql.org/)

Full feature setup:
- [Dependency analysis](https://github.com/project-ncl/dependency-analysis)
- [jBPM server](http://www.jbpm.org/)

PNC is delivered as JEE EAR package that can be deployed to [EAP](http://developers.redhat.com/products/eap/) / [WildFly](http://wildfly.org/) or other JEE application server. 

JEE Server requirements:
- [JBoss EAP/Wildfly Adapter](https://keycloak.gitbooks.io/securing-client-applications-guide/content/topics/oidc/java/jboss-adapter.html) 
- Hibernate as JPA provider
- [PostgreSQL JDBC driver](https://jdbc.postgresql.org/)


## Developing

**Requirements**
- JDK 8
- Maven 3.2
- Java IDE (IntelliJ IDEA community recommended)
    - code style template: https://github.com/project-ncl/ide-config

**UI development**
- Node.js
- npm
- Angular JS
- Bower >= 0.9.1

**REST API documentation and testing**

Swagger api doc is available at http://hostname/pnc-web/apidocs/

*Requires running instance of PNC 


## Building

The default build is executed by running `mvn clean install`.

The default build does not run "integration tests" annotated with @ContainerTest.
Tests annotated with @DebugTest are also skipped by default as they are usually written to run against a remote running server.


### Building with all tests

To run container tests use profile `-Pcontainer-tests`.
Extra parameter `-Deap6.zip.url` is required to provide the location of server distribution archive.
Tests requiring JEE application server (tested with JBoss EAP 7.0)

Example:

	mvn clean install -Pcontainer-tests -Deap.zip.url=file:///home/development/jboss-eap.zip

To run debug tests use `-Pdebug-tests`.

By default it is required to specify `install` phase to run container tests.

To use `verify` phase append property `-DuseTargetBuilds` (no value required) to use artifacts generated during package phase in target folder.

#### Installing application server for integration tests manually

Application server is installed by default to folder target in project top level folder.
During installation additional modules (HSQL, Postgresql JDBC drivers, datasources, RHSSO) required to run integration tests are installed into the server.

To run only installation of application server to specific folder use `-Dtest.server.unpack.dir=`. 

Example to install server to /tmp folder:

	mvn clean verify -Pcontainer-tests -Deap6.zip.url=SERVER_ZIP_URL -pl :test-common,:test-arquillian-container -Dtest.server.unpack.dir=/tmp

#### Running integration tests using pre-installed and running application server

1. Install application server (see Installing application server for integration tests manually)
2. start the server

	sh /tmp/jboss-eap/bin/standalone.sh

3. run integration tests with additional system properties and excluded module test-arquillian-container

	-Darq.container.wf.configuration.jbossHome=/tmp/jboss-eap
	-Darq.container.wf.configuration.allowConnectingToRunningServer=true
	-pl \!:test-arquillian-container

	mvn clean verify -Pcontainer-tests -pl \!:test-arquillian-container -Deap6.zip.url= -DuseTargetBuilds -Darq.container.wf.configuration.jbossHome=/tmp/jboss-eap -Darq.container.wf.configuration.allowConnectingToRunningServer=true


### Running integration tests in Intellij IDEA

1. Install application server (see Installing application server for integration tests manually)
2. start the server

	sh /tmp/jboss-eap/bin/standalone.sh

3. Create new Run/Debug configuration of type Arquillian JUnit

 - setup arquillian container - Click button Configure in Arquillian Container tab, add 'Manual container configuration', add 'Maven dependency' in dependencies, type in text field `org.jboss.as:jboss-as-arquillian-container-managed:7.2.0.Final`
 - select test class or package or module in Configuration tab
 - specify path to application server and allow to connect to running server in VM options:

    -Darq.container.wf.configuration.jbossHome=/tmp/jboss-eap
    -Darq.container.wf.configuration.allowConnectingToRunningServer=true

  - optionally append following properties (no value required) to VM options:
    -DcreateArchiveCopy - to write ear deployed by arquillian to project folder (eg. for content inspection)
    -DuseTargetBuilds - to use artifact generated by package phase in target folder 
    
Property useTargetBuilds can be used together with configuration 'Run maven goal' `mvn package -DskipTests=true` in 'before lunch' tab to deploy code changes without actual need to run mvn clean install


### Building for Production (Postgresql DB)

A Maven profile called `production` is provided to configure the appropriate settings to build a deployment file which is compatible with the postgresql database.

    mvn install -Pproduction

TODO: this is not working currently, integration tests are always run against HSQLDB
The container tests can also be run against postgresql by activating the `container-tests` profile, and the `production` profile.

    mvn install -Pproduction,container-tests

### UI Module Compilation Errors

Due to the need to integrate a modern frontend workflow into a maven project there can occasionally be some complications in a build. Some data is cached by the UI that is not completely cleaned by running `mvn clean`. In case of strange build failures with the UI module please try running: `mvn clean -Dfrontend.clean.force` and this will completely clean out all data. NOTE: with this profile enabled build times will increase by a few minutes as the ui build system will have to retrieve a large amount of previously cached data.


## Configuration

All configurations are centralized in the configuration file `moduleconfig/src/main/resources/pnc-config.json`.

You can copy the file to external location and specify the location via `-Dpnc-config-file=/path/to/pnc-config.json` parameter.

Or you can configure the app using the environment variables listred in the file.

For the configuration descriptions see api doc of classes in `moduleconfig/src/main/java/org/jboss/pnc/common/json/moduleconfig`

### PullingMonitor configuration

If you want to specify the ThreadPool size for `PullingMonitor`, you can do so by using the system property `pulling_monitor_threadpool`.

For example, you can specify via `-Dpulling-monitor-threadpool=10` parameter

You can also specify the environment variable `pulling_monitor_threadpool`.

Similarly, to configure the timeout to wait for the builder pod to start, we can specify it either by the system property or environment property `pulling_monitor_timeout`
Finally, to configure the check interval in seconds for the builder pod to start, we can specify it either by the system property or environment property `pulling_monitor_check_interval`

Note that the system property has precedence over the environment variable if both are defined.


## Database set-up

### Manually Configuring the Datasource for HSQL DB

You will need to download the [hsqldb jar file](http://repo1.maven.org/maven2/org/hsqldb/hsqldb/2.3.3/hsqldb-2.3.3.jar) and copy the jar file into the standalone/deployments directory of your JBoss server.  Check the server log to see that the driver deployed successfully.

From the EAP/Wildfly admin console, select Configuration-->Connector-->Datasources.
Click the "Add" button to add a new datasource and set the required fields.

    Name: NewcastleTestDS
    JNDI Name: java:jboss/datasources/NewcastleTestDS
    JDBC Driver: hsqldb-2.3.3.jar
    Connection URL: jdbc:hsqldb:mem:newcastletestmemdb

You can test the connection before saving the datasource settings.  Finally, enable the datasource, and it is ready to be used by the newcastle application.


### Installing Postgres

Basic installation instructions for Postgres on recent versions of Fedora can be found on the Fedora wiki: https://fedoraproject.org/wiki/PostgreSQL

(Optional) In addition to the postgresql server, it's useful to install the gui admin tool pgadmin3 (dnf install pgadmin3).

Once postgresql is installed and started, you can create and modify the databases using the psql command.

Connect to postgres

    $ psql -h localhost newcastle -U <ROOT_USERNAME>

Create the user "newcastle
Create user

    postgres=# CREATE USER newcastle WITH PASSWORD newcastle;

Create the newcastle database
Create newcastle database

    postgres=# CREATE DATABASE newcastle OWNER newcastle;

Once the database is created, the schema can be built using the SQL files included with the newcastle source code, or it can be created automatically if it doesn't exist.


### Configuring the Datasource

You will need to download and install the PostgreSQL JDBC driver into Wildfly (https://jdbc.postgresql.org/download.html).  Copy the postgresql jdbc driver jar into the standalone/deployments directory of your JBoss server.  Check the server log to see whether the driver deployed successfully.

From the EAP/Wildfly admin console, select Configuration-->Connector-->Datasources.
Click the "Add" button to add a new datasource and set the required fields.

    Name: NewcastleDS
    JNDI Name: java:jboss/datasources/NewcastleDS
    JDBC Driver: postgresql-9.3.jar
    Connection URL: jdbc:postgresql://localhost:5432/newcastle
    Username: newcastle
    Password: newcastle

You can test the connection before saving the datasource settings.


## Authentication

This project comes with possibility to be secured. Security is delivered via Keycloak project http://keycloak.jboss.org/.
To be able to turn on whole project on secure side you need 2 parts to fulfill.
1. **Have running and configured Keycloak server instance.** <br/>
2. **Build & configure security for your PNC installation.**


### Authentication - prepare Keycloak server<br/>
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

Configure keycloak subsystem secure-deployment in EAP/wildfly stanbdalone.xml

1. pncrest additional props -> use these below for skipping ssl & defining rest for accepting only access_token for authentication/authorization.
  * "ssl-required": "none",
  * "bearer-only" : true,
2. pncweb additional props -> look at http://keycloak.github.io/docs/userguide/html/ch08.html#adapter-config to find more about adapter's config.
  * "ssl-required" : "none",
  * "use-resource-role-mappings" : false,
  * "enable-basic-auth" : false,
  * "enable-cors" : true,
  * "cors-max-age" : 1000,
  * "cors-allowed-methods" : "POST,PUT,DELETE,GET",
  * "bearer-only" : false,


### Authentication - build & configure your PNC<br/>
By default PNC project comes with no security at all, it is up to you to turn it on.
In case you want to enable authentication use `-Pauth` together with your build command.
Enabling authentication means following<br/>

1. Your backend REST endpoints will become secured
  * rest.war will use the web.xml with security-constraints & security-roles (rest/src/main/webconfig/auth/web.xml)
2. Your pnc web UI gain the SSO ability and authentication via Keycloak login page.
  * with your first unauthenticated session you will be redirected from pnc web UI into Keycloak login page.
  * you will be asked to provide your credentials.
  * after successful log-in you will be redirected back to pnc web UI.
3. Configure your JEE server (EAP) for keycloak
  * Use -Dauth.eap.home=``<path to your EAP installation>`` with you build command, if you want EAP configure for Keycloak.
  According the http://docs.jboss.org/keycloak/docs/1.1.0.Final/userguide/html/ch08.html#jboss-adapter-installation installation will be performed on server for the given path.

Before running the PNC you have to update the pnc-config.json and set "authenticationProviderId" to "Keycloak" instead of the default "JAAS" if you want to use Keycloank authentication.

## Configuring the Openshift pod definitions

PNC is using OpenShift to provide clean build environmtn for each build.
You can override the pod definitions used to configure the build-agent pod via pnc configuration:

```json
{
  "@module-group": "pnc",
  "configs": [
      {
        "@module-config": "openshift-build-agent",
          "pncBuilderPod": {...},
          "pncBuilderService": {...},
          "pncBuilderRoute": {...},
          "pncBuilderSshRoute": {...}
      },
      ...
  ]
```
You only need to define the key for which you want to modify its definition. There is no need to define all the keys.


## Metrics support

PNC tracks metrics of JVM and its internals via Dropwizard Metrics. The metrics can currently be reported to a Graphite server by specifying as system property or environment variables those properties:
- metrics\_graphite\_server (mandatory)
- metrics\_graphite\_port (mandatory)
- metrics\_graphite\_prefix (mandatory)
- metrics\_graphite\_interval (optional)

If the `metrics_graphite_interval` variable (interval specified in seconds) is not specified, we'll use the default value of 60 seconds to report data to Graphite.

The graphite reporter is configured to report rates per second and durations in terms of milliseconds.
