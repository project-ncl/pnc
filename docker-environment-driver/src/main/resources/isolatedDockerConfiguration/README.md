Isolation is done on running Docker container with iptables script

Run

# CID=$(docker run --privileged -it --env firewallAllowedPorts=80,443 --env firewallAllowedDestinations=10.16.36.64 --rm -p 22222:44555 -p 8080:8080 --name ssh-jenkins -v /var/jenkins_home mareknovotny/pnc-jenkins)

# docker exec $CID /tmp/isolate-with-iptables.sh


Check the non-permitted link

```
# docker exec $CID curl https://repo1.maven.org/maven2/ant/ant-junit/maven-metadata.xml
```

it should resulted in _Failed to connect to repo1.maven.org port 443: Connection refused_
while on 

```
# docker exec cdc8eede2e6c curl http://download.eng.bos.redhat.com/brewroot/repos/jb-wfk-2-rhel-6-build/latest/maven/ant/ant-junit/maven-metadata.xml
```

should pass with content of the file.


