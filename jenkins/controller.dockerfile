FROM jenkins/jenkins:2.517-jdk21

USER root

COPY plugins.txt /var/jenkins_home/plugins.txt
RUN chown jenkins:jenkins /var/jenkins_home/plugins.txt
RUN jenkins-plugin-cli -f /var/jenkins_home/plugins.txt

USER jenkins