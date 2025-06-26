FROM redhat/ubi9-minimal


ARG NEXUS_VERSION=3.81.1-01
ARG NEXUS_DOWNLOAD_URL=https://download.sonatype.com/nexus/3/sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip
ARG NEXUS_DOWNLOAD_SHA256_HASH=88e0b313af161c8d5817aa6399e5a995755cde7e956a0c6468807b073d6e99c6

# configure nexus runtime
ENV SONATYPE_DIR=/opt/sonatype
ENV NEXUS_HOME=${SONATYPE_DIR}/nexus \
    NEXUS_DATA=/nexus-data \
    NEXUS_CONTEXT='' \
    SONATYPE_WORK=${SONATYPE_DIR}/sonatype-work \
    DOCKER_TYPE='rh-docker'

# Install Java, tar, and unzip
RUN microdnf update -y \
    && microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y \
          java-17-openjdk-headless tar procps shadow-utils gzip unzip \
    && microdnf clean all \
    && groupadd --gid 200 -r nexus \
    && useradd --uid 200 -r nexus -g nexus -s /bin/false -d /opt/sonatype/nexus -c 'Nexus Repository Manager user'

WORKDIR ${SONATYPE_DIR}

# Download nexus & setup directories
RUN curl -L ${NEXUS_DOWNLOAD_URL} --output sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip \
    && echo "${NEXUS_DOWNLOAD_SHA256_HASH} sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip" > sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip.sha256 \
    && sha256sum -c sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip.sha256 \
    && unzip sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip \
    && rm -f sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip sonatype-nexus-repository-${NEXUS_VERSION}-assembly.zip.sha256 \
    && mv nexus-${NEXUS_VERSION} $NEXUS_HOME \
    && chown -R nexus:nexus ${SONATYPE_WORK} \
    && mv ${SONATYPE_WORK}/nexus3 ${NEXUS_DATA} \
    && ln -s ${NEXUS_DATA} ${SONATYPE_WORK}/nexus3

RUN echo "#!/bin/bash" >> ${SONATYPE_DIR}/start-nexus-repository-manager.sh \
   && echo "cd /opt/sonatype/nexus" >> ${SONATYPE_DIR}/start-nexus-repository-manager.sh \
   && echo "exec ./bin/nexus run" >> ${SONATYPE_DIR}/start-nexus-repository-manager.sh \
   && chmod a+x ${SONATYPE_DIR}/start-nexus-repository-manager.sh \
   && chmod a+x ${SONATYPE_DIR}/nexus/bin/nexus \
   && sed -e '/^nexus-context/ s:$:${NEXUS_CONTEXT}:' -i ${NEXUS_HOME}/etc/nexus-default.properties

RUN microdnf remove -y gzip shadow-utils

VOLUME ${NEXUS_DATA}

EXPOSE 8081
USER nexus

ENV NEXUS_DATASTORE_NEXUS_JDBCURL="jdbc:postgresql://postgresql-nexus.nexus.svc.cluster.local:5432/nexus?gssEncMode=disable&tcpKeepAlive=true&loginTimeout=5&connectionTimeout=5&socketTimeout=30&cancelSignalTimeout=5&targetServerType=primary"
ENV NEXUS_DATASTORE_NEXUS_USERNAME="nexus"

ENV NEXUS_DATASTORE_NEXUS_ADVANCED="maximumPoolSize=200"
ENV NEXUS_DATASTORE_NEXUS_ADVANCED="maxLifetime=840000"

CMD ["/opt/sonatype/nexus/bin/nexus", "run"]