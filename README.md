# Introduction

This mock is an extension of Dumpster SMTP Email Server used for Unit Testing. The new features helps to read & clear emails via REST API. Your Application Under Test can send SMTP messages to Dumpster running on port - 2025.

# Technologies

- Java 8
- Dumpster (http://quintanasoft.com/dumbster/)
- Spring MVC
- Jetty Embedded Server

# Running the Service

- clone the repo
- mvn clean package
- mvn jetty:run

The mock service will be up & running on port: 8080 & STMP Server will be running on port: 2025 (configurable)

# Verify the Service

- Open a web browser
- curl http://localhost:8080/index.jsp
- curl http://localhost:8080/email/healthcheck

# API

## Send sample SMTP message to dumpster server (GET or POST)
- curl http://localhost:8080/email/send/abcd@abc.com
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


