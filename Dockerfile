FROM bellsoft/liberica-openjdk-alpine:17-cds AS builder
WORKDIR /application
COPY . .
RUN --mount=type=cache,target=/root/.gradle chmod +x gradlew && ./gradlew clean build -x test

FROM bellsoft/liberica-openjre-alpine:17-cds
VOLUME /tmp

WORKDIR /application

COPY --from=builder /application/build/libs/*.jar app.jar
RUN java -XX:ArchiveClassesAtExit=app.jsa -Dspring.context.exit=onRefresh -jar app.jar & exit 0

ENV JAVA_XMX="512M"
ENV JAVA_XMS="256M"

ENV JAVA_CDS_OPTS="-XX:SharedArchiveFile=app.jsa -Xlog:class+load:file=/tmp/classload.log"
ENV JAVA_ERROR_FILE_OPTS="-XX:ErrorFile=/tmp/java_error.log"

ENTRYPOINT exec java \
    -Xmx$JAVA_XMX \
    -Xms$JAVA_XMX \l
    $JAVA_ERROR_FILE_OPTS \
    $JAVA_CDS_OPTS \
    -jar app.jar
