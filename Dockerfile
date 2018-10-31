FROM openjdk:8-jre


EXPOSE 8080
EXPOSE 61984

CMD ["mkdir", "/deployments"]

COPY build/libs/*.jar /deployments/gcloud-backup-photo-syncer-1.0-SNAPSHOT.jar
COPY credentials.json /deployments/credentials.json

CMD ["java", "-cp", ".", "-jar", "/deployments/gcloud-backup-photo-syncer-1.0-SNAPSHOT.jar"]