#Project-ncl

Building Project
----------------
Requirements:

* JDK 8
* Maven 3.2

Command line arguments:

The default build is executed by running `ḿvn clean install`.<br />
By default the tests that require remote services and integration tests are disabled.<br />
In order to run remote and integration tests you have to specify remote services location and credentials by edit configuration file `common/src/main/resources/pnc-config.json`.<br />
By default the configuration file uses env variables, you can set required variables (see file for list of them) instead of editing the file itself.<br />
If you want to use different (external) config file location you can define path to it with `-Dpnc-config-file=/path/to/pnc-config.json`.

Remote tests are defined by class name *RemoteTest.java<br />
To run remote test use `ḿvn clean install -DremoteTest=true`

Integration tests are placed in module "integrations-tests" to run them use `-Pintegration-test`.

To run remote and integration tests combine both commands `ḿvn clean install -DremoteTest=true -Pintegration-test`.


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
