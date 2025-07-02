FROM jenkins/inbound-agent:3309.v27b_9314fd1a_4-6-jdk21

COPY ./jdk8u452-b09 /opt/java/openjdk-8
COPY ./jdk-11.0.27+6 /opt/java/openjdk-11
COPY ./jdk-17.0.15+6 /opt/java/openjdk-17
COPY ./jdk-21.0.7+6 /opt/java/openjdk-21

USER root

# RUN apt-get update && \
#     apt-get install ca-certificates curl -y && \
#     install -m 0755 -d /etc/apt/keyrings && \
#     curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
#     chmod a+r /etc/apt/keyrings/docker.asc && \
#     echo \
#     "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian \
#     $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
#     tee /etc/apt/sources.list.d/docker.list > /dev/null && \
#     apt-get update

# RUN apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# RUN usermod -aG docker jenkins && \
#     newgrp docker

RUN apt-get update && \
    apt-get -y install wget

RUN wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq && \
    chmod +x /usr/local/bin/yq

RUN apt-get remove -y wget && \
    apt-get autoremove -y && \
    apt-get clean

USER jenkins