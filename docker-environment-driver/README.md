Docker Environment Driver
=========================

Docker Environment Driver is used for creating PNC builder environment.

It can uses locally configured docker host or remotelly configured docker host. It depends how you set up your environment.

Configuration of docker host is described at [Set up of Docker host](../README.md#set-up-of-docker-host)

After configuring docker host you need to set up the [PNC Enviromental variables](../README.md#environmental-variables) which starts with `PNC_DOCKER_*` or use them in `pnc-config.json`

Docker image usage is described in [PNC Docker README.md](src/main/docker/README.md)
