package com.cicdaas.mockemailservice;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

public class SimpleSmtpMessageTest {

    @Test
    public void testDefaultConstructorInitializesBody() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();

        assertNotNull(message.getBody());
        assertEquals("", message.getBody());
        assertNull(message.getId());
        assertNull(message.getFrom());
        assertNull(message.getTo());
        assertNull(message.getSubject());
        assertNull(message.getReceivedDate());
    }

    @Test
    public void testGettersAndSetters() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();

        message.setId(1L);
        assertEquals(1L, message.getId());

        message.setFrom("sender@test.com");
        assertEquals("sender@test.com", message.getFrom());

        message.setTo("recipient@test.com");
        assertEquals("recipient@test.com", message.getTo());

        message.setSubject("Test Subject");
        assertEquals("Test Subject", message.getSubject());

        message.setReceivedDate("1234567890");
        assertEquals("1234567890", message.getReceivedDate());

        message.setBody("Test Body Content");
        assertEquals("Test Body Content", message.getBody());
    }

    @Test
    public void testIdGeneratedAutomatically() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();
        assertNull(message.getId());
    }

    @Test
    public void testNullableFieldsAllowNull() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();

        message.setFrom(null);
        assertNull(message.getFrom());

        message.setSubject(null);
        assertNull(message.getSubject());

        message.setReceivedDate(null);
        assertNull(message.getReceivedDate());
    }

    @Test
    public void testToFieldCanBeSet() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();
        message.setTo("test@example.com");

        assertEquals("test@example.com", message.getTo());
    }

    @Test
    public void testBodyCanContainHtmlContent() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();
        String htmlContent = "<html><body><h1>Test Email</h1></body></html>";

        message.setBody(htmlContent);

        assertEquals(htmlContent, message.getBody());
    }

    @Test
    public void testSerialVersionUID() {
        assertEquals(1L, SimpleSmtpMessage.serialVersionUID);
    }
}
