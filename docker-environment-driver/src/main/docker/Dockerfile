FROM fedora:21
MAINTAINER Marek Novotny <mnovotny@redhat.com>

# update or not??
#RUN yum -y update && yum clean all
# why SSH? see http://blog.docker.com/2014/06/why-you-dont-need-to-run-sshd-in-docker/
# install all required packages like git, java and zip
RUN yum -y install openssh-server git tar gzip java java-devel && yum clean all

# set JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-openjdk

# openjdk bug with SSL connection https://bugzilla.redhat.com/show_bug.cgi?id=1167153
RUN sed -i 's/jdk.tls.disabledAlgorithms=SSLv3/jdk.tls.disabledAlgorithms=EC,ECDHE,ECDH,SSLv3/g' $JAVA_HOME/jre/lib/security/java.security

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
RUN curl -L http://mirrors.jenkins-ci.org/war-stable/$JENKINS_VERSION/jenkins.war -o /usr/share/jenkins/jenkins.war
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
#Configure SSHD
RUN mkdir /var/run/sshd
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_key
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_rsa_key 
RUN ssh-keygen -b 1024 -t dsa -f /etc/ssh/ssh_host_dsa_key

#!!!! CHANGE ROOT PASSWORD BEFORE REAL USE !!!!
#
# JBoss, Home of Professional Open Source.
# Copyright 2014 Red Hat, Inc., and individual contributors
# as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

RUN echo 'root:changeme' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

#Install maven
# set installed Maven version you can easily change it later
ENV MAVEN_VERSION 3.2.5
RUN curl -sSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
&& ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
 
RUN echo "export M2_HOME=/usr/share/maven" >> /etc/profile
RUN mkdir /root/.m2

# all below env variables are used in Maven settings.xml to set Maven proxy for all requests
#ENV isHttpActive true # false value deactivates the proxy for maven
#ENV proxyIPAddress 10.40.3.134 # IP address of proxy server or DNS resolvabla hostname
#ENV proxyPort 3128 # port on which proxy server listens

# Could be used for images based on this Docker image to specify default mirrored repository if different from Maven Central below
ADD settings.xml /usr/share/maven/conf/settings.xml
ADD setup-proxy-server.sh /tmp/setup-proxy-server.sh

ADD isolate-with-iptables.sh /tmp/isolate-with-iptables.sh

#Prepare script to start Jenkins, SSH daemon and ip-tables
RUN echo -e "/tmp/setup-proxy-server.sh & \n /tmp/isolate-with-iptables.sh & \n /usr/sbin/sshd -D & \n /usr/local/bin/jenkins.sh" > /usr/bin/startPrograms.sh


#Expose SSH port
EXPOSE 22
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]

