# Maven Repository Driver

This repository driver is designed to work with [Aprox](https://github.com/jdcasey/aprox) via its client API.

## Development Instance

The development instance of AProx *(see Shortcut docs)* is configured to auto-update every time a new AProx snapshot is deployed to [Sonatype's OSS Repository](https://oss.sonatype.org/content/repositories/snapshots/org/commonjava/aprox/launch/aprox-launcher-savant). Snapshots are **not** automatically deployed (via CI or other means), since development of a new feature may require several commits over a few days to complete. Manually deploying snapshots gives us the ability to avoid triggering auto-update until we believe the new feature is ready for testing.

The auto-update takes place via cron (`/etc/crontab`) on the dev/demo VM, using the [aprox-docker-utils](https://github.com/jdcasey/aprox-docker) utilities.

The development instance uses two Docker containers, one for volumes (storage) and the other for the deployed code (logic). They are called `aprox-volumes` and `aprox` respectively, and have `systemd` services that correspond to each container as follows:

* `aprox-server.service` - Controls `aprox` container
* `aprox-volumes.service` - Controls 'aprox-volumes` container

### Installing from Scratch

    #!/bin/bash
    
    curl http://repo.maven.apache.org/maven2/org/commonjava/aprox/docker/aprox-docker-utils/0.19.1-3/aprox-docker-utils-0.19.1-3.tar.gz | tar -zxvf -C /root
    
    cd /root/aprox-docker-utils/systemd
    cp aprox-volumes.service aprox-server.service /etc/systemd/system
    systemctl enable aprox-volumes
    systemctl enable aprox-server
    cd /root/aprox-docker-utils
    
    ./init-aprox-volumes
    ./init-aprox-server -p 80 -q
    
    docker stop aprox aprox-volumes
    systemctl start aprox-volumes
    systemctl start aprox-server
    
    journalctl -f
    # wait to verify server comes up.
    
    echo "*/15 * * * * root /root/aprox-docker-utils/autodeploy-url -s aprox-server -u https://oss.sonatype.org/content/repositories/snapshots/org/commonjava/aprox/launch/aprox-launcher-savant/0.19.2-SNAPSHOT/maven-metadata.xml /root/aprox-docker-utils/init-aprox-server -U {url} -p 80 -q >> /etc/crontab
    systemctl restart crond

The script above grabs a copy of the aprox-docker-utils tarball (source [here](https://github.com/jdcasey/aprox-docker)), unpacks it, and then installs/enables the systemd services from it. From there, it initializes the Docker containers for `aprox-volumes` (storage) and `aprox` (the server itself) using utility scripts from that tarball. Once that's done, it stops the containers using docker, and restarts them under systemd's control (not sure if stop/restart is strictly necessary). Finally, it adds a reference to the `autodeploy-url` utility script to the system-wide crontab. This script will monitor the given remote metadata file for updates to the deployed snapshot timestamp, and redeploy when it detects a change.

The currently deployed version is stored in `/root/.autodeploy.last`, so redeployment can be forced by removing that file and letting the cron job run again.

**NOTE:** The volumes container **will not be** redeployed, since it's existence is designed to support persistent storage while allowing the server container code to update.

## Demo Instance

The demo instance of AProx (in place to separate new feature development from the version required to support the demo of the latest complete release of PNC), is deployed on the same server. The version of this AProx instance is obviously more tightly controlled, and updates happen as required to support PNC releases (not automatically).

The demo instance uses two Docker containers, one for volumes (storage) and the other for the deployed code (logic). They are called `aprox-volumes-old` and `aprox-old` respectively, and have `systemd` services that correspond to each container as follows:

* `aprox-server-old.service` - Controls `aprox-old` container
* `aprox-volumes-old.service` - Controls 'aprox-volumes-old` container

### Installing from Scratch

For obvious reasons, the demo instance of AProx is not automatically updated. Since it was added to the VM after the development instance, it has also required some customization to the files in the `aprox-docker-utils` tarball in order for the two instances to coexist peacefully. Installation is likewise a bit more hackish for now (at least until I can streamline multi-instance deployment a bit more in the scripts). Therefore, the steps below don't really fit nicely into a copy-paste bash script format, but require a bit more thought on the part of the admin.

**NOTE:** The reference in aprox-server.service to aprox-volumes will need to be adjusted appropriately in step 2.

1. Download aprox-docker-utils and unpack to /root/aprox-docker-utils.old:

    $ curl http://repo.maven.apache.org/maven2/org/commonjava/aprox/docker/aprox-docker-utils/0.19.1-3/aprox-docker-utils-0.19.1-3.tar.gz | tar -zxvf -C /tmp
    $ mv /tmp/aprox-docker-utils /root/aprox-docker-utils.old

2. Edit the systemd scripts to reference `aprox-volumes-old` in place of `aprox-volumes`, and `aprox-old` in place of `aprox`

    $ vi /root/aprox-docker-utils/systemd/aprox-server.service
    $ vi /root/aprox-docker-utils/systemd/aprox-volumes.service

3. Copy/enable the modified systemd scripts alongside the ones used for the development instance:

    $ cp /root/aprox-docker-utils.old/systemd/aprox-volumes.service /etc/systemd/system/aprox-volumes-old.service
    $ systemctl enable aprox-volumes-old
    $ cp /root/aprox-docker-utils.old/systemd/aprox-server.service /etc/systemd/system/aprox-server-old.service
    $ systemctl enable aprox-server-old

4. Initialize a new volumes container with the alternative name (`aprox-volumes-old`):

    $ /root/aprox-docker-utils.old/init-aprox-volumes -n aprox-volumes-old

5. Initialize a new server container with the specific demo version (`0.18.5`) the alternative port (`8180`), the alternative name (`aprox-old`), and the alternative name of the volume container (`aprox-volumes-old`):

    $ /root/aprox-docker-utils.old/init-aprox-server -V 0.18.5 -p 8180 -n aprox-old -v aprox-volumes-old -q

6. Stop the containers via docker (not sure this is necessary):

    $ docker stop aprox-old aprox-volumes-old

7. Restart via systemctl:

    $ systemctl start aprox-volumes-old
    $ systemctl start aprox-server-old
    $ journalctl -f

### Updating the Demo Instance

Updating is a manual process, so we can have greater control over the version running with our PNC demo. To update, you need to shutdown the `aprox-old` container, remove it, pull any updates to the docker image, and recreate the container using the updated AProx version (`$VERSION` below):

    #!/bin/bash
    systemctl stop aprox-server-old
    docker rm aprox-old
    docker pull buildchimp/aprox
    /root/aprox-docker-utils.old/init-aprox-server -V $VERSION -p 8180 -n aprox-old -v aprox-volumes-old -q
    docker stop aprox-old
    systemctl start aprox-server-old
    journalctl -f

    