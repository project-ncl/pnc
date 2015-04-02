# Maven Repository Driver

This repository driver is designed to work with [Aprox](https://github.com/jdcasey/aprox) via its client API.

## Development Instance

The development instance of AProx *(see Shortcut docs)* is configured to auto-update every time a new AProx snapshot is deployed to [Sonatype's OSS Repository](https://oss.sonatype.org/content/repositories/snapshots/org/commonjava/aprox/launch/aprox-launcher-savant). Snapshots are **not** automatically deployed (via CI or other means), since development of a new feature may require several commits over a few days to complete. Manually deploying snapshots gives us the ability to avoid triggering auto-update until we believe the new feature is ready for testing.

The auto-update takes place via cron (`/etc/crontab`) on the dev/demo VM, using the [aprox-docker-utils](https://github.com/jdcasey/aprox-docker) utilities.

The development instance uses two Docker containers, one for volumes (storage) and the other for the deployed code (logic). They are called `aprox-volumes` and `aprox` respectively, and have `systemd` services that correspond to each container as follows:

* `aprox-server.service` - Controls `aprox` container
* `aprox-volumes.service` - Controls 'aprox-volumes` container

## Demo Instance

The demo instance of AProx (in place to separate new feature development from the version required to support the demo of the latest complete release of PNC), is deployed on the same server. The version of this AProx instance is obviously more tightly controlled, and updates happen as required to support PNC releases (not automatically).

The demo instance uses two Docker containers, one for volumes (storage) and the other for the deployed code (logic). They are called `aprox-volumes-old` and `aprox-old` respectively, and have `systemd` services that correspond to each container as follows:

* `aprox-server-old.service` - Controls `aprox-old` container
* `aprox-volumes-old.service` - Controls 'aprox-volumes-old` container

Currently, updating the aprox-old container requires issuing the following commands:

    #!/bin/bash
    
    systemctl stop aprox-server-old
    docker stop aprox-old
    docker rm aprox-old
    
    #edit /root/aprox-docker-utils.old/init-aprox-server-old to update APROX_VERSION
    /root/aprox-docker-utils-old/init-aprox-server-old
    
    #wait for startup to complete, then CTL-C
    docker stop aprox-old
    systemctl start aprox-server-old

At some point in the near future, we will try to capture these commands in a utility script that provides help feedback and other amenities.