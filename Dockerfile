FROM eclipse-temurin:21.0.4_7-jre

ENV LANG=fr_FR.UTF-8 LANGUAGE=fr_FR:fr LC_ALL=fr_FR.UTF-8

ENV PATH_TO_JAR=/application/pe-bo.jar
WORKDIR application
RUN rm -rf /application
ADD ./target/*.jar $PATH_TO_JAR

ENV JAVA_TOOL_OPTIONS_DEFAULT \
    -XX:MaxRAMPercentage=75 \
    -XX:+UseParallelGC

ENTRYPOINT [ "/bin/sh", "-c", \
    "export JAVA_TOOL_OPTIONS=\"$JAVA_TOOL_OPTIONS_DEFAULT $JAVA_TOOL_OPTIONS\"; \
    exec java -jar $PATH_TO_JAR" ]