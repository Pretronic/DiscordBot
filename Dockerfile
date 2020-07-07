FROM java:8-jdk-alpine

COPY ./build/PretronicDiscordBot.jar /usr/app/
COPY ./config.yml /usr/app/
COPY ./messages/ /usr/app/


WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "PretronicDiscordBot.jar"]