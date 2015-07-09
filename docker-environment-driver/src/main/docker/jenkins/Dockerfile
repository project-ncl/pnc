FROM project-ncl/pnc-common:latest
MAINTAINER Project NCL Team

## jenkins instalation
ENV JENKINS_HOME /var/jenkins_home

# Jenkins is ran with user `jenkins`, uid = 1000
# If you bind mount a volume from host/vloume from a data container,
# ensure you use same uid
RUN useradd -d "$JENKINS_HOME" -u 1000 -m -s /bin/bash jenkins
# Jenkins home directoy is a volume, so configuration and build history
# can be persisted and survive image upgrades
VOLUME /var/jenkins_home
# `/usr/share/jenkins/ref/` contains all reference configuration we want
# to set on a fresh new installation. Use it to bundle additional plugins
# or config file with your custom jenkins Docker image.
RUN mkdir -p /usr/share/jenkins/ref/init.groovy.d
COPY init.groovy /usr/share/jenkins/ref/init.groovy.d/tcp-slave-angent-port.groovy
COPY plugins.sh /usr/local/bin/plugins.sh
ENV JENKINS_VERSION 1.554.1
# could use ADD but this one does not check Last-Modified header
# see https://github.com/docker/docker/issues/8331
RUN curl -sL http://mirrors.jenkins-ci.org/war-stable/$JENKINS_VERSION/jenkins.war -o /usr/share/jenkins/jenkins.war
ENV JENKINS_UC https://updates.jenkins-ci.org
RUN chown -R jenkins "$JENKINS_HOME" /usr/share/jenkins/ref
# for main web interface:
EXPOSE 8080
# will be used by attached slave agents:
EXPOSE 50000
USER jenkins
COPY jenkins.sh /usr/local/bin/jenkins.sh
# we use script for launching SSHD, isolation and jenkins at the end
#ENTRYPOINT ["/usr/local/bin/jenkins.sh"]
# from a derived Dockerfile, can use `RUN plugin.sh active.txt` to setup /usr/share/jenkins/ref/plugins from a support bundle
COPY plugins.txt /usr/share/jenkins/plugins.txt
COPY jenkins-config/* /var/jenkins_home/
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/plugins.txt
## end of jenkins installation


USER root

#Prepare script to start Jenkins, SSH daemon and ip-tables
RUN echo -e "/usr/bin/setup-proxy-server.sh & \n /usr/bin/isolate-with-iptables.sh & \n /usr/sbin/sshd -D & \n /usr/local/bin/jenkins.sh" > /usr/bin/startPrograms.sh

# Expose SSH port
EXPOSE 22

# Entry
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]
