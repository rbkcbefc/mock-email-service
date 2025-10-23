package com.cicdaas.mockemailservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MockEmailServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmailRepository emailRepository;

    @Test
    void testHealthCheckReturnsAlive() throws Exception {
        mockMvc.perform(get("/email/healthcheck"))
            .andExpect(status().isOk())
            .andExpect(content().string("alive"))
            .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void testReadMsgsReturnsEmptyListWhenNoEmails() throws Exception {
        mockMvc.perform(get("/email/read/nonexistent@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emailAddress").value("nonexistent@example.com"))
            .andExpect(jsonPath("$.msgs").isArray());
    }

    @Test
    void testReadMsgsDecodesEmailAddressWithoutAtSign() throws Exception {
        mockMvc.perform(get("/email/read/testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emailAddress").value("testuser.com"));
    }

    @Test
    void testReadMsgsKeepsEmailAddressWithAtSign() throws Exception {
        mockMvc.perform(get("/email/read/test@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emailAddress").value("test@example.com"));
    }

    @Test
    void testReadMsgsReturnsDbEmailsOnly() throws Exception {
        SimpleSmtpMessage dbEmail = new SimpleSmtpMessage();
        dbEmail.setTo("dbtest@example.com");
        dbEmail.setFrom("sender@test.com");
        dbEmail.setSubject("DB Email");
        dbEmail.setBody("DB email body");
        dbEmail.setReceivedDate("1234567890");
        emailRepository.save(dbEmail);

        mockMvc.perform(get("/email/read/dbtest@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emailAddress").value("dbtest@example.com"))
            .andExpect(jsonPath("$.msgs").isArray())
            .andExpect(jsonPath("$.msgs[0].subject").value("DB Email"));
    }

    @Test
    void testClearMsgsDecodesEmailAddress() throws Exception {
        mockMvc.perform(get("/email/clear/testuser"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testClearMsgsReturnsSuccessJson() throws Exception {
        mockMvc.perform(get("/email/clear/test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.count").exists());
    }

    @Test
    void testSendEmailReturnsJson() throws Exception {
        mockMvc.perform(get("/email/send/test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testSendEmailDecodesEmailAddress() throws Exception {
        mockMvc.perform(post("/email/send/testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testSendWebEmailSavesToDatabase() throws Exception {
        long beforeCount = emailRepository.count();

        mockMvc.perform(get("/webemail/send/webtestsave@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

        long afterCount = emailRepository.count();
        assertEquals(beforeCount + 1, afterCount);
    }

    @Test
    void testSendWebEmailDecodesAddress() throws Exception {
        mockMvc.perform(post("/webemail/send/testuser"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"));

        long count = emailRepository.countByTo("testuser.com");
        assertTrue(count > 0);
    }

    @Test
    void testSendWebEmailReturnsSuccessJson() throws Exception {
        mockMvc.perform(post("/webemail/send/test@example.com"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value("success"));
    }
}
