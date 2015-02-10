FROM jenkins
MAINTAINER Jakub Bartecek <jbartece@redhat.com>
USER root

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

#Start jenkins instance and SSH daemon
RUN echo "/usr/sbin/sshd -D & \n /usr/local/bin/jenkins.sh " > /usr/bin/startPrograms.sh

EXPOSE 22
ENTRYPOINT ["sh", "/usr/bin/startPrograms.sh"]


#USER jenkins

