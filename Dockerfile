FROM tomcat:9.0
ADD target/mock-email-service.war /usr/local/tomcat/webapps/mock-email-service.war
EXPOSE 8080