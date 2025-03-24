package com.cicdaas.mockemailservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import com.dumbster.smtp.SimpleSmtpServer;

import ch.qos.logback.classic.Logger;

public class MockEmailServer {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MockEmailServer.class);

    private static final int TEST_STMP_SERVER_PORT = 2025;
    private static MockEmailServer INSTANCE = null;
    private SimpleSmtpServer smtpServer = null;
    private Map<String, List<SimpleSmtpMessage>> webEmails = null; 

    private MockEmailServer() {
        LOG.info("Initializing Dumpster!");
        smtpServer = SimpleSmtpServer.start(TEST_STMP_SERVER_PORT);
        webEmails = new HashMap<>();
        LOG.info("Dumpster Initalization Completed!");
    }

    public SimpleSmtpServer getSimpleSmtpServer() {
        return smtpServer;
    }

    public Map<String, List<SimpleSmtpMessage>> getWebEmails() {
        return webEmails;
    }

    public static MockEmailServer getInstance() {
        LOG.info("MOckEmailServer - getInstance method invoked!");
        if (INSTANCE == null) {
            LOG.info("Initantiating MockEmailServer class");
            INSTANCE = new MockEmailServer();
        }
        return INSTANCE;
    }

}