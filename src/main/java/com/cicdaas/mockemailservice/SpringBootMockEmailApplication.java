package com.cicdaas.mockemailservice;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import ch.qos.logback.classic.Logger;

@SpringBootApplication
public class SpringBootMockEmailApplication {

	private static final Logger LOG = (Logger) LoggerFactory.getLogger(SpringBootMockEmailApplication.class);

	public static void main(String[] args) {
		LOG.info("Initializing Spring Boot App - Start");
		SpringApplication.run(SpringBootMockEmailApplication.class, args);
		MockEmailServer.getInstance();
		LOG.info("Initializing Spring Boot App - Complete");
	}

}
