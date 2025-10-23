package com.cicdaas.mockemailservice;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.dumbster.smtp.SimpleSmtpServer;

public class MockEmailServerTest {

    @Test
    public void testGetInstanceReturnsSameInstance() {
        MockEmailServer instance1 = MockEmailServer.getInstance();
        MockEmailServer instance2 = MockEmailServer.getInstance();

        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "getInstance should return the same instance (singleton)");
    }

    @Test
    public void testGetInstanceInitializesSmtpServer() {
        MockEmailServer instance = MockEmailServer.getInstance();
        SimpleSmtpServer smtpServer = instance.getSimpleSmtpServer();

        assertNotNull(smtpServer, "SMTP server should be initialized");
    }

    @Test
    public void testGetSimpleSmtpServerReturnsServer() {
        MockEmailServer instance = MockEmailServer.getInstance();
        SimpleSmtpServer server = instance.getSimpleSmtpServer();

        assertNotNull(server, "Should return a valid SimpleSmtpServer instance");
    }

    @Test
    public void testSmtpServerIsNotStopped() {
        MockEmailServer instance = MockEmailServer.getInstance();
        SimpleSmtpServer server = instance.getSimpleSmtpServer();

        assertNotNull(server);
        assertFalse(server.isStopped(), "SMTP server should be running");
    }
}
