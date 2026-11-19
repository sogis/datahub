FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24@sha256:7f2eaba33484d04e5331135e8d3e621c5a4740b6b03bc99d25e4d4dc043f6b8c AS ubi
# The vendor image declares 8443. Start with its unmodified filesystem and fresh OCI metadata.
FROM scratch
COPY --from=ubi / /
ENV PATH=/usr/local/bin:/usr/bin:/bin JAVA_HOME=/usr/lib/jvm/jre LANG=C.utf8
RUN install -d -o 0 -g 0 -m 0555 /application \
    && install -d -o 0 -g 0 -m 2775 /work /datahub
WORKDIR /application
ENV SPRING_PROFILES_ACTIVE=prod WORK_DIRECTORY=/work TARGET_DIRECTORY=/datahub HOME=/work TZ=Europe/Zurich
ARG JAR_FILE
COPY --chown=0:0 --chmod=0444 ${JAR_FILE} /application/application.jar
USER 1001:0
EXPOSE 8080
ENTRYPOINT ["/bin/sh", "-c", "umask 0002; exec java -XX:-UseBASE64Intrinsics -XX:MaxRAMPercentage=90.0 -jar /application/application.jar \"$@\"", "--"]
