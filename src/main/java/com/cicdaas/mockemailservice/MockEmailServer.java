package com.cicdaas.mockemailservice;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dumbster.smtp.SimpleSmtpServer;

import ch.qos.logback.classic.Logger;

@Component
public class MockEmailServer {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MockEmailServer.class);

    private static final int TEST_STMP_SERVER_PORT = 2025;

    private static MockEmailServer INSTANCE = new MockEmailServer();

    private SimpleSmtpServer smtpServer = null;

    private MockEmailServer() {
        LOG.info("Initializing Dumpster!");
        smtpServer = SimpleSmtpServer.start(TEST_STMP_SERVER_PORT);
        LOG.info("Dumpster Initalization Completed!");
    }

    public SimpleSmtpServer getSimpleSmtpServer() {
        return this.smtpServer;
    }

    public static MockEmailServer getInstance() {
        return INSTANCE;
    }

}