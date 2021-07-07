FROM postgres:9.5

MAINTAINER e-thuaud

ENV POSTGRES_USER publicenemy
ENV POSTGRES_PASSWORD publicenemy
ENV POSTGRES_DB publicenemy

COPY db/data.sql /docker-entrypoint-initdb.d/

CMD ["postgres"]