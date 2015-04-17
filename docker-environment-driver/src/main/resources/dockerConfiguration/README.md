Project Newcastle jenkins docker image
======================================

Dockerfile for jenkins

Get Docker version

```
# docker version
```

To build:

Copy the sources down and do the build

```
# docker build --rm -t <username>/pnc_jenkins .
```

Start and make volume persistent:
```
# docker run --name myjenkins -p 8080:8080 -v /var/jenkins_home jenkins
```

Copy data out of the container:

```
# docker cp myjenkins:/var/jenkins_home /tmp/jenkins
```

Start created container:

```
# docker start myjenkins
```

Stop running container:

```
# docker stop myjenkins
```

Start daemon with listening on TCP socket:

```
# docker -d -H tcp://127.0.0.1:2375 -H unix:///var/run/docker.sock
```

Verify:

```
# docker -H tcp://127.0.0.1:2375 version
```

Enable SSHD using Dockerfile copy docker file

Create new docker image:

```
# docker build -t pnc-jenkins <PATH to Dockerfile>
```

Run it:

```
# CID=$(docker run --privileged -it --env firewallAllowedPorts=80,443 --env firewallAllowedDestinations=10.16.36.64 --rm -p 22222:44555 -p 8080:8080 --name ssh-jenkins -v /var/jenkins_home pnc-jenkins)
# 
# docker exec $CID /tmp/isolate-with-iptables.sh
# docker exec $CID /usr/sbin/iptables -L OUTPUT
```

Running Docker image on VM 
--------------------

Start as usual, but with TCP socket enabled

```
# docker -d -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock
```

Content of prepared socket config _/etc/sysconfig/docker_:

```
OPTIONS='--selinux-enabled -H tcp://0.0.0.0:2375 -H unix:///var/run/docker.sock -g /mnt/docker/data'
INSECURE_REGISTRY='--insecure-registry <your-internal-docker-registry>'```

Enable docker:

```
# sudo systemctl enable docker
```

Start docker:

```
 sudo systemctl start docker
```

Verify:

```
# docker -H tcp://127.0.0.1:2375 version
```

Pull image:

```
# sudo docker pull <username>/isshd-jenkins
```

Start container from image as usual

To run (if port 8080 is open on your host):

```
docker run -p 8080:8080 -d <username>/fedora_jenkins
```

or to assign a random port that maps to port 80 on the container:

```
# docker run -d -p 8080 pnc-jenkins
```

To the port that the container is listening on:

```
# docker ps
```

To test:

* open _http://localhost:<port\>_ in a web browser

 _<port\>_ is The port of docker host to which container's port is mapped


