FROM jenkins/jnlp-agent-docker:latest

COPY ./jdk8u452-b09 /opt/java/openjdk-8
COPY ./jdk-11.0.27+6 /opt/java/openjdk-11
COPY ./jdk-17.0.15+6 /opt/java/openjdk-17
COPY ./jdk-21.0.7+6 /opt/java/openjdk-21

USER root

RUN apt-get update && \
    apt-get -y install podman

USER jenkins