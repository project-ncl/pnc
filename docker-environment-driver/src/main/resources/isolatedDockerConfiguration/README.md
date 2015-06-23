Isolation is done on running Docker container with iptables script
and environment variable `$firewallAllowedDestinations`

You can effectively disable network isolation by setting environment variable to
`firewallAllowedDestinations=all`

If you will omit the setting of environment variable, every network traffic will be dropped and docker container will be fully isolated from outside.


Example of container launching to allow only http and https connections to 10.16.36.64:

```
# CID=$(docker run --cap-add=NET_ADMIN -d --env firewallAllowedDestinations=10.16.36.64:80,10.16.36.64:443 -P -p 8080:8080 --name ssh-jenkins -v /var/jenkins_home pnc-builder-v0.5)

# docker exec $CID /tmp/isolate-with-iptables.sh
```

Check the non-permitted link to https://repo1.maven.org which translates to https://23.235.43.209

```
# docker exec $CID curl https://repo1.maven.org/maven2/ant/ant-junit/maven-metadata.xml
```

it should resulted in _Failed to connect to repo1.maven.org port 443: Connection refused_
while on

```
# docker exec $CID curl http://10.16.36.64/brewroot/repos/jb-wfk-2-rhel-6-build/latest/maven/ant/ant-junit/maven-metadata.xml
```

should pass with downloading and showing maven-metadata.xml file content on the console.
