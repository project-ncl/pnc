#Project-ncl

Main Modules:
* `datastore`: Implementation of pnc-spi:org.jboss.pnc.spi.datastore
* `jenkins-build-driver`: Implementation of pnc-spi:org.jboss.pnc.spi.builddriver
* `maven-repository-manager`: Implementation of pnc-spi:org.jboss.pnc.spi.repositorymanager
* `pnc-core`: Contains implementations of action-controllers, which include the business logic for orchestrating builds, test runs, etc. Action controllers are used to isolate logic from the REST API, so it can be reused in embedded scenarios
* `pnc-model`: Contains domain model for the orchestrator. This is just model classes + serialization helpers, and would also be suitable for writing a java client api to support integration
* `pnc-rest-bindings`: REST API. This is a series of classes that use JAX-RS to translate HTTP communications to calls into the action controllers in the core, and format any output (such as constructing resource URLs, etc.)
* `pnc-spi`: Contains all SPI interfaces the orchestrator will use to coordinate its sub-services for provisioning environments and repositories, triggering builds, storing domain objects. It is meant to be used in conjunction with pnc-model
* `pnc-web`: Contains Web UI resoures (html + js pages, images etc.)
