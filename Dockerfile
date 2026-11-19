FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24@sha256:7f2eaba33484d04e5331135e8d3e621c5a4740b6b03bc99d25e4d4dc043f6b8c

USER 0
WORKDIR /work
RUN chmod 0775 /work
VOLUME ["/work"]

WORKDIR /datahub
RUN chmod 0775 /datahub
VOLUME ["/datahub"]

WORKDIR /opt/app
ARG JAR_FILE
COPY --chown=0:0 --chmod=0444 ${JAR_FILE} ./application.jar

USER 1001:0
EXPOSE 8080
ENV HOME=/work SPRING_PROFILES_ACTIVE=prod WORK_DIRECTORY=/work TARGET_DIRECTORY=/datahub TZ=Europe/Zurich LOG4J_FORMAT_MSG_NO_LOOKUPS=true
# Keep this mitigation until the Java 25 runtime contains the fix for CVE-2026-114938.
CMD ["java", "-XX:MaxRAMPercentage=80.0", "-XX:-UseBASE64Intrinsics", "-jar", "application.jar"]
