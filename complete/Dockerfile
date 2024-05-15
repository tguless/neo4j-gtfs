FROM amazoncorretto:17
RUN mkdir /apps
RUN mkdir /apps/config
COPY ./target/gs-accessing-data-neo4j-0.1.0.jar /apps/app.jar
#COPY ./config /apps/config
#COPY ./profiles /apps/profiles
EXPOSE 8080
USER root
WORKDIR /apps/
ENTRYPOINT ["sh", "-c", "java \
            -Dlog4j.appender.console=org.apache.log4j.ConsoleAppender \
            -Dspring.profiles.active=${APP_ENVIRONMENT} \
            -Dserver.port=8080 \
            -jar \
            /apps/app.jar"]
