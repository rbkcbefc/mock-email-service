package com.cicdaas.mockemailservice;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class EmailRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmailRepository emailRepository;

    @BeforeEach
    void setUp() {
        emailRepository.deleteAll();
    }

    @Test
    void testSaveEmailPersistsToDatabase() {
        SimpleSmtpMessage message = new SimpleSmtpMessage();
        message.setTo("test@example.com");
        message.setFrom("sender@test.com");
        message.setSubject("Test Subject");
        message.setBody("Test Body");
        message.setReceivedDate("1234567890");

        SimpleSmtpMessage saved = emailRepository.save(message);
        entityManager.flush();

        assertNotNull(saved.getId());
        assertEquals("test@example.com", saved.getTo());
        assertEquals("sender@test.com", saved.getFrom());
        assertEquals("Test Subject", saved.getSubject());
        assertEquals("Test Body", saved.getBody());
        assertEquals("1234567890", saved.getReceivedDate());
    }

    @Test
    void testFindByToOrderByReceivedDateDesc() {
        SimpleSmtpMessage msg1 = createMessage("test@example.com", "1000000000");
        SimpleSmtpMessage msg2 = createMessage("test@example.com", "3000000000");
        SimpleSmtpMessage msg3 = createMessage("test@example.com", "2000000000");
        SimpleSmtpMessage msg4 = createMessage("other@example.com", "4000000000");

        emailRepository.save(msg1);
        emailRepository.save(msg2);
        emailRepository.save(msg3);
        emailRepository.save(msg4);
        entityManager.flush();

        List<SimpleSmtpMessage> results = emailRepository.findByToOrderByReceivedDateDesc("test@example.com");

        assertEquals(3, results.size());
        assertEquals("3000000000", results.get(0).getReceivedDate());
        assertEquals("2000000000", results.get(1).getReceivedDate());
        assertEquals("1000000000", results.get(2).getReceivedDate());
    }

    @Test
    void testFindByToReturnsEmptyListWhenNoMatch() {
        SimpleSmtpMessage msg = createMessage("test@example.com", "1234567890");
        emailRepository.save(msg);
        entityManager.flush();

        List<SimpleSmtpMessage> results = emailRepository.findByToOrderByReceivedDateDesc("nonexistent@example.com");

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testDeleteByToRemovesAllMatchingEmails() {
        SimpleSmtpMessage msg1 = createMessage("test@example.com", "1000000000");
        SimpleSmtpMessage msg2 = createMessage("test@example.com", "2000000000");
        SimpleSmtpMessage msg3 = createMessage("other@example.com", "3000000000");

        emailRepository.save(msg1);
        emailRepository.save(msg2);
        emailRepository.save(msg3);
        entityManager.flush();

        emailRepository.deleteByTo("test@example.com");
        entityManager.flush();

        List<SimpleSmtpMessage> remaining = emailRepository.findAll();
        assertEquals(1, remaining.size());
        assertEquals("other@example.com", remaining.get(0).getTo());
    }

    @Test
    void testDeleteByToDoesNotAffectOtherEmails() {
        SimpleSmtpMessage msg1 = createMessage("test@example.com", "1000000000");
        SimpleSmtpMessage msg2 = createMessage("other@example.com", "2000000000");
        SimpleSmtpMessage msg3 = createMessage("another@example.com", "3000000000");

        emailRepository.save(msg1);
        emailRepository.save(msg2);
        emailRepository.save(msg3);
        entityManager.flush();

        emailRepository.deleteByTo("test@example.com");
        entityManager.flush();

        assertEquals(2, emailRepository.count());
        assertFalse(emailRepository.findByToOrderByReceivedDateDesc("test@example.com").isEmpty() == false);
        assertEquals(1, emailRepository.findByToOrderByReceivedDateDesc("other@example.com").size());
        assertEquals(1, emailRepository.findByToOrderByReceivedDateDesc("another@example.com").size());
    }

    @Test
    void testCountByToReturnsCorrectCount() {
        SimpleSmtpMessage msg1 = createMessage("test@example.com", "1000000000");
        SimpleSmtpMessage msg2 = createMessage("test@example.com", "2000000000");
        SimpleSmtpMessage msg3 = createMessage("other@example.com", "3000000000");

        emailRepository.save(msg1);
        emailRepository.save(msg2);
        emailRepository.save(msg3);
        entityManager.flush();

        long count = emailRepository.countByTo("test@example.com");
        assertEquals(2, count);

        count = emailRepository.countByTo("other@example.com");
        assertEquals(1, count);

        count = emailRepository.countByTo("nonexistent@example.com");
        assertEquals(0, count);
    }

    private SimpleSmtpMessage createMessage(String to, String receivedDate) {
        SimpleSmtpMessage message = new SimpleSmtpMessage();
        message.setTo(to);
        message.setFrom("sender@test.com");
        message.setSubject("Test Subject");
        message.setBody("Test Body");
        message.setReceivedDate(receivedDate);
        return message;
    }
}
