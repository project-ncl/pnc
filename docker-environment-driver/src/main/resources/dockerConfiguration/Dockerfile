FROM jenkins
MAINTAINER Jakub Bartecek <jbartece@redhat.com>
USER root

#Install SSHD
# why? see http://blog.docker.com/2014/06/why-you-dont-need-to-run-sshd-in-docker/
RUN apt-get update && apt-get install -y openssh-server
RUN mkdir /var/run/sshd

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

#RUN chown -R jenkins /etc/ssh
#RUN chown jenkins /usr/sbin/sshd

#Install maven
# set installed Maven version you can easily change it later
ENV MAVEN_VERSION 3.2.5
RUN curl -sSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
&& mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
&& ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
 
RUN echo "export M2_HOME=/usr/share/maven" >> /etc/profile
# isn't better to set it like? ^^^
# ENV M2_HOME /usr/share/maven 
RUN mkdir /root/.m2

# Could be used for images based on this Docker image to specify default mirrored repository if different from Maven Central below
ENV mavenRepositoryUrl http://repo.maven.apache.org/maven2
ADD settings.xml /usr/share/maven/conf/settings.xml

ADD isolate-with-iptables.sh /tmp/isolate-with-iptables.sh

#Prepare script to start Jenkins and SSH daemon
RUN echo "/usr/sbin/sshd -D & \n /usr/local/bin/jenkins.sh " > /usr/bin/startPrograms.sh

#Expose SSH port
EXPOSE 22
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]


#USER jenkins

