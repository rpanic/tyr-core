FROM gradle:jdk11-alpine as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build

FROM openjdk:11-jre
COPY --from=builder /home/gradle/src/build/libs/Tyr-1.0-all.jar /app/tyr.jar
WORKDIR /app
CMD ["java", "-jar", "tyr.jar"]