FROM eclipse-temurin:21.0.7_6-jre

ENV LANG=fr_FR.UTF-8 LANGUAGE=fr_FR:fr LC_ALL=fr_FR.UTF-8

ENV PATH_TO_JAR=/opt/pe/pe-bo.jar
WORKDIR /opt/pe/
ADD ./target/*.jar $PATH_TO_JAR

ENV JAVA_TOOL_OPTIONS_DEFAULT \
    -XX:MaxRAMPercentage=75 \
    -XX:+UseZGC

ENV JAVA_USER_ID=10001
ENV JAVA_USER=java
RUN groupadd -g "$JAVA_USER_ID" "$JAVA_USER" && \
    useradd -r -u "$JAVA_USER_ID" -g "$JAVA_USER" "$JAVA_USER"

USER $JAVA_USER_ID

ENTRYPOINT [ "/bin/sh", "-c", \
    "export JAVA_TOOL_OPTIONS=\"$JAVA_TOOL_OPTIONS_DEFAULT $JAVA_TOOL_OPTIONS\"; \
    exec java -jar $PATH_TO_JAR" ]