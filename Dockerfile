FROM eclipse-temurin:17-jre-focal

ENV LANG=fr_FR.UTF-8 LANGUAGE=fr_FR:fr LC_ALL=fr_FR.UTF-8
RUN apt-get update -y
RUN apt-get install fontconfig libretls musl-locales musl-locales-lang ttf-dejavu tzdata zlib -y

WORKDIR /opt/pe/
COPY ./target/*.jar /opt/pe/pe.jar
ENTRYPOINT ["java", "-jar",  "/opt/pe/pe.jar"]