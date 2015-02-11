FROM jenkins
MAINTAINER Jakub Bartecek <jbartece@redhat.com>
USER root

#Install SSHD
RUN apt-get update && apt-get install -y openssh-server
RUN mkdir /var/run/sshd

#!!!! CHANGE ROOT PASSWORD BEFORE REAL USE !!!!
RUN echo 'root:changeme' | chpasswd
RUN sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config

# SSH login fix. Otherwise user is kicked off after login
RUN sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd
ENV NOTVISIBLE "in users profile"
RUN echo "export VISIBLE=now" >> /etc/profile

#RUN chown -R jenkins /etc/ssh
#RUN chown jenkins /usr/sbin/sshd

#Install maven
ADD http://mirror.hosting90.cz/apache/maven/maven-3/3.2.5/binaries/apache-maven-3.2.5-bin.tar.gz /root/maven.tar.gz
RUN mkdir /usr/apache-maven/
RUN tar -xzvf /root/maven.tar.gz --directory=/usr/apache-maven
RUN echo "export M2_HOME=/usr/apache-maven/apache-maven-3.2.5" >> /etc/profile
RUN echo "export PATH=$PATH:/usr/apache-maven/apache-maven-3.2.5/bin" >> /etc/profile
RUN mkdir /root/.m2

#Prepare script to start Jenkins and SSH daemon
RUN echo "/usr/sbin/sshd -D & \n /usr/local/bin/jenkins.sh " > /usr/bin/startPrograms.sh

#Expose SSH port
EXPOSE 22
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]


#USER jenkins

