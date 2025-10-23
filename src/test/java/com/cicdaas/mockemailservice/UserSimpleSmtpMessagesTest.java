package com.cicdaas.mockemailservice;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class UserSimpleSmtpMessagesTest {

    @Test
    void testDefaultConstructorInitializesFields() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();

        assertNotNull(messages.getEmailAddress());
        assertEquals("", messages.getEmailAddress());
        assertNotNull(messages.getMsgs());
        assertTrue(messages.getMsgs().isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();

        messages.setEmailAddress("test@example.com");
        assertEquals("test@example.com", messages.getEmailAddress());

        List<SimpleSmtpMessage> msgList = new ArrayList<>();
        SimpleSmtpMessage msg = new SimpleSmtpMessage();
        msg.setTo("test@example.com");
        msg.setSubject("Test");
        msgList.add(msg);

        messages.setMsgs(msgList);
        assertEquals(1, messages.getMsgs().size());
        assertEquals("Test", messages.getMsgs().get(0).getSubject());
    }

    @Test
    void testMessagesListMutable() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();
        List<SimpleSmtpMessage> msgList = messages.getMsgs();

        SimpleSmtpMessage msg1 = new SimpleSmtpMessage();
        msg1.setSubject("Message 1");
        msgList.add(msg1);

        assertEquals(1, messages.getMsgs().size());

        SimpleSmtpMessage msg2 = new SimpleSmtpMessage();
        msg2.setSubject("Message 2");
        msgList.add(msg2);

        assertEquals(2, messages.getMsgs().size());

        msgList.remove(0);
        assertEquals(1, messages.getMsgs().size());
        assertEquals("Message 2", messages.getMsgs().get(0).getSubject());
    }

    @Test
    void testSetEmailAddressWithDifferentFormats() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();

        messages.setEmailAddress("simple@test.com");
        assertEquals("simple@test.com", messages.getEmailAddress());

        messages.setEmailAddress("user+tag@example.com");
        assertEquals("user+tag@example.com", messages.getEmailAddress());

        messages.setEmailAddress("test.user@subdomain.example.com");
        assertEquals("test.user@subdomain.example.com", messages.getEmailAddress());
    }

    @Test
    void testEmptyMessagesList() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();
        List<SimpleSmtpMessage> emptyList = new ArrayList<>();

        messages.setMsgs(emptyList);

        assertNotNull(messages.getMsgs());
        assertTrue(messages.getMsgs().isEmpty());
    }

    @Test
    void testMultipleMessages() {
        UserSimpleSmtpMessages messages = new UserSimpleSmtpMessages();
        List<SimpleSmtpMessage> msgList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SimpleSmtpMessage msg = new SimpleSmtpMessage();
            msg.setSubject("Message " + i);
            msg.setTo("test@example.com");
            msgList.add(msg);
        }

        messages.setMsgs(msgList);

        assertEquals(5, messages.getMsgs().size());
        assertEquals("Message 0", messages.getMsgs().get(0).getSubject());
        assertEquals("Message 4", messages.getMsgs().get(4).getSubject());
    }
}
