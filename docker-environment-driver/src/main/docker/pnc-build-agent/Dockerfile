FROM project-ncl/pnc-common:0.6.2
MAINTAINER Project NCL Team

USER root

# Add PNC Build Agent.
# Currently using Maven to download the altest snapshot.
RUN mv /usr/share/maven/conf/settings.xml /usr/share/maven/conf/settings.xml.backup
WORKDIR /tmp
RUN /usr/share/maven/bin/mvn -q org.apache.maven.plugins:maven-dependency-plugin:2.10:get -DrepoUrl=https://repository.jboss.org/nexus/content/repositories/snapshots -Dtransitive=false -Dartifact=org.jboss.pnc:build-agent:0.1-SNAPSHOT:jar:jar-with-dependencies -Ddest=/usr/local/build-agent-0.1-SNAPSHOT-jar-with-dependencies.jar
RUN mv /usr/share/maven/conf/settings.xml.backup /usr/share/maven/conf/settings.xml

EXPOSE 8080

# Prepare script to start BuildAgent, SSH daemon and ip-tables
RUN echo -e "/usr/bin/setup-proxy-server.sh & \n /usr/bin/isolate-with-iptables.sh & \n /usr/bin/addSSHServer.sh & \n java -jar /usr/local/build-agent-0.1-SNAPSHOT-jar-with-dependencies.jar -b 0.0.0.0 -l /tmp" > /usr/bin/startPrograms.sh

# Start
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]
