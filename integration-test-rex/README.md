Setup
-----
Rex, Keycloak and BPM are services required to run the tests.
Rex and Keycloak are running in TestContainers, BPM in mocked using WireMock.

Process of the test build:
- Orch get auth token from Keycloak (services are configured with auth and the token is passed between them)
- Orch sends the build tasks to Rex
- Rex does the task dependency resolution and submit the tasks one by one(when ready) to BPM
- BPM notifies Rex of task completion
- Rex notifies Orch of the task completion and submits new (ready) tasks to BPM

Keycloak realm export
----------------------
- Export realm using web UI
- Manually add uesrs to the exported realm, because they are not included in the export

Example export file:
https://github.com/keycloak/keycloak/blob/main/examples/js-console/example-realm.json
