FROM openjdk:23
COPY target/mock-email-service.war mock-email-service.war
ENTRYPOINT ["java","-jar","/mock-email-service.war"]