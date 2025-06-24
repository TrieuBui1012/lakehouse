FROM apache/spark:v3.3.2

COPY ./target/Scala-1.0-SNAPSHOT.jar /opt/spark/work-dir/myjob.jar
COPY ./hudi-defaults.conf /opt/spark/work-dir/hudi-defaults.conf
USER root
ARG spark_uid=185

RUN groupadd --system --gid=${spark_uid} spark && \
    useradd --system --uid=${spark_uid} --gid=spark spark


RUN chown -R spark:spark /opt/spark && chmod 777 /opt/spark/work-dir/*
RUN chown -R spark:spark .
USER spark
ENTRYPOINT [ "/opt/entrypoint.sh" ]
# RUN pip3 install pyspark aws-hadoop