# Introduction

This mock is an extension of Dumpster SMTP Email Server used for Unit Testing. The new features helps to read & clear emails via REST API. Your Application Under Test can send SMTP messages to Dumpster running on port - 2025.

# Technologies

- Java 23
- Dumpster (http://quintanasoft.com/dumbster/)
- Spring Boot Web 3.3.4

# Running the Service

- clone the repo
- mvn clean package
- mvn spring-boot:run (or) java -jar target/mock-email-service.war

The mock service will be up & running on port: 8080 & STMP Server will be running on port: 2025 (configurable)

# Verify the Service

- Open a web browser - Welcome Page
- curl http://localhost:8080/
- Health Check
- curl http://localhost:8080/healthcheck
- curl http://localhost:8080/email/healthcheck

# API

## Send sample message to dumpster server (GET or POST)
- curl http://localhost:8080/webemail/send/abcd@abc.com
````
{"status":"success"}
````

## Read email messages - GET
- curl http://localhost:8080/email/read/abcd@abc.com

````
{"emailAddress":"abcd@abc.com","msgs":[{"from":"admin@mockemailservice.com","to":"abcd@abc.com","subject":"Subject: Test Email","receivedDate":"Fri, 26 Feb 2016 21:02:24 -0800 (PST)","body":"<html><body><h1>Test Email - 1456549344106</h1></body></html>"}]}
````

## Clear email messages - GET
- curl http://localhost:8080/email/clear/abcd@abc.com
````
{"status":"success", "count":"1"}
````

# Docker

## Build Image
- docker build -t mock-email-service .

## Run Image
- docker run -it -p 8080:8080   mock-email-service

