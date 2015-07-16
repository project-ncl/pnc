FROM fedora:21
MAINTAINER Project NCL Team

# update or not??
#RUN yum -y update && yum clean all
# why SSH? see http://blog.docker.com/2014/06/why-you-dont-need-to-run-sshd-in-docker/
# install all required packages like git, java and zip
RUN yum -y install openssh-server git tar gzip java java-devel && yum clean all

# set JAVA_HOME
ENV JAVA_HOME /usr/lib/jvm/java-openjdk

# openjdk bug with SSL connection https://bugzilla.redhat.com/show_bug.cgi?id=1167153
RUN sed -i 's/jdk.tls.disabledAlgorithms=SSLv3/jdk.tls.disabledAlgorithms=EC,ECDHE,ECDH,SSLv3/g' $JAVA_HOME/jre/lib/security/java.security

USER root
#Configure SSHD
RUN mkdir /var/run/sshd
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_key
RUN ssh-keygen -b 1024 -t rsa -f /etc/ssh/ssh_host_rsa_key
RUN ssh-keygen -b 1024 -t dsa -f /etc/ssh/ssh_host_dsa_key

#!!!! CHANGE ROOT PASSWORD BEFORE REAL USE !!!!
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
#ENV proxyServer 10.40.3.134 # IP address of proxy server or DNS resolvabla hostname
#ENV proxyPort 3128 # port on which proxy server listens

# Could be used for images based on this Docker image to specify default mirrored repository if different from Maven Central below
ADD settings.xml /usr/share/maven/conf/settings.xml

ADD setup-proxy-server.sh /usr/bin/setup-proxy-server.sh

ADD isolate-with-iptables.sh /usr/bin/isolate-with-iptables.sh

ADD addSSHServer.sh /usr/bin/addSSHServer.sh

# Expose Agent port
EXPOSE 22
