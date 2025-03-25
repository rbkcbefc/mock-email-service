package com.cicdaas.mockemailservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import ch.qos.logback.classic.Logger;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

@Controller
public class MockEmailService {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MockEmailService.class);

    private Counter emailAddressCounter;
    private Counter webEmailCounter;
    private MeterRegistry meterRegistry;

    public MockEmailService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.webEmailCounter = Counter.builder("webemail.sent.count")
            .tags("status", "sent")
            .description("Total number of web emails sent")
            .register(meterRegistry);
        this.emailAddressCounter = Counter.builder("emailadress.count")
            .tags("email-type", "web")
            .description("Total number of email addresses in memory")
            .register(meterRegistry);
    }

    @RequestMapping(value = "/email/healthcheck", method = RequestMethod.GET, produces="text/plain")
    @ResponseBody
    public String isEmailServiceAlive() {
        return "alive";
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/email/clear/{emailAddress}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public String clearMsgs(@PathVariable("emailAddress") String emailAddress) {
        emailAddress = decodeEmailAddress(emailAddress);
        LOG.debug("Clear Email Messages: " + emailAddress);
        SimpleSmtpServer smtpServer = MockEmailServer.getInstance().getSimpleSmtpServer();
        Iterator<SmtpMessage> emailIter = smtpServer.getReceivedEmail();
        int clearedEmailCount = 0;
        while (emailIter.hasNext()) {
            SmtpMessage msg = emailIter.next();
            Iterator<String> headerItr = msg.getHeaderNames();
            while (headerItr.hasNext()) {
                String headerName = headerItr.next();
                String[] headerValues = msg.getHeaderValues(headerName);
                boolean removeMsg = false;
                for (String val : headerValues) {
                    if (headerName.equalsIgnoreCase("To") && val.equalsIgnoreCase(emailAddress)) {
                        LOG.debug("Email ID matches! To be deleted!");
                        removeMsg = true;
                    }
                }
                if (removeMsg) {
                    emailIter.remove();
                    LOG.debug("Matched Email ID - Deleted!");
                    clearedEmailCount++;
                }
            }
        }
        return "{\"status\":\"success\", \"count\":\""+clearedEmailCount+"\"}";
    }

    @SuppressWarnings({ "unused", "unchecked" })
    @RequestMapping(value = "/email/read/{emailAddress}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public UserSimpleSmtpMessages readMsgs(@PathVariable("emailAddress") String emailAddress) {
        emailAddress = decodeEmailAddress(emailAddress);
        LOG.debug("Read Email Address: " + emailAddress);
        List<SimpleSmtpMessage> smtpMsgs = new ArrayList<SimpleSmtpMessage>();
        SimpleSmtpServer smtpServer = MockEmailServer.getInstance().getSimpleSmtpServer();
        Iterator<SmtpMessage> emailIter = smtpServer.getReceivedEmail();
        LOG.debug("Total SMTP Email Size: " + smtpServer.getReceivedEmailSize());
        UserSimpleSmtpMessages msgs = new UserSimpleSmtpMessages();
        msgs.setEmailAddress(emailAddress);
        List<SimpleSmtpMessage> listOfMsg = new ArrayList<SimpleSmtpMessage>();
        while (emailIter.hasNext()) {
            SmtpMessage msg = emailIter.next();
            Iterator<String> headerItr = msg.getHeaderNames();
            while (headerItr.hasNext()) {
                String headerName = headerItr.next();
                LOG.debug("Header Name: " + headerName + " , Value: " + msg.getHeaderValue(headerName));
                String[] headerValues = msg.getHeaderValues(headerName);
                for (String val : headerValues) {
                    if (headerName.equalsIgnoreCase("To") && val.equalsIgnoreCase(emailAddress)) {
                        SimpleSmtpMessage smtpMsg = new SimpleSmtpMessage();
                        smtpMsg.setBody(getHtmlContent(msg.getBody()));
                        smtpMsg.setFrom(msg.getHeaderValue("From"));
                        smtpMsg.setSubject(msg.getHeaderValue("Subject"));
                        smtpMsg.setTo(emailAddress);
                        smtpMsg.setReceivedDate(msg.getHeaderValue("Date"));
                        listOfMsg.add(smtpMsg);
                    }
                }
            }
        }
        LOG.debug("Total User SMTP Emails: " + listOfMsg.size());
        Map<String, List<SimpleSmtpMessage>> webEmails = MockEmailServer.getInstance().getWebEmails();
        LOG.debug("Total Web User(s): " + webEmails.size());
        if (webEmails.containsKey(emailAddress)) {
            listOfMsg.addAll(webEmails.get(emailAddress));
        }
        LOG.debug("Total User Emails  (smtp & web): " + listOfMsg.size());
        msgs.setMsgs(listOfMsg);
        return msgs;
    }

    @SuppressWarnings("unchecked")
    private String getHtmlContent(String body) {
        try {
            Session s = Session.getDefaultInstance(new Properties());
            InputStream is = new ByteArrayInputStream(body.getBytes());
            MimeMessage message = new MimeMessage(s, is);
            Enumeration<String> enu = message.getAllHeaderLines();
            enu.nextElement();
            String tmp = enu.nextElement();
            int endIndex = tmp.indexOf("------=_Part_");
            return endIndex > 0 ? tmp.substring(0, endIndex) : tmp;
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract html content from SMTP message!", e);
        }
    }

    private String decodeEmailAddress(String emailAddress) {
        // when Email Id is NOT encoded, safely return the original Email ID with domain as .com
        return emailAddress.contains("@") ? emailAddress : emailAddress + ".com";
    }

    @RequestMapping(value = "/email/send/{emailAddress}", method = {RequestMethod.GET , RequestMethod.POST}, 
            produces="application/json")
    @ResponseBody
    public String sendEmail(@PathVariable("emailAddress") String emailAddress) {
        try {
            // compose message
            emailAddress = decodeEmailAddress(emailAddress);
            LOG.debug("Send Email Address: " + emailAddress);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("admin@mockemailservice.com");
            message.setTo(emailAddress);
            message.setSubject("Subject: Test Email");
            message.setText("<html><body><h1>Test Email - \" + System.currentTimeMillis() + \"</h1></body></html>");
            Properties javaMailProperties = new Properties();
            javaMailProperties.setProperty("mail.transport.protocol", "smtp");
            javaMailProperties.setProperty("mail.smtp.auth", "false");
            javaMailProperties.setProperty("mail.smtp.starttls.enable", "false");
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
            mailSender.setHost("localhost");
            mailSender.setPort(2025);
            mailSender.setJavaMailProperties(javaMailProperties);
            mailSender.send(message);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            String errMsg = "Unable to send email!" + e.getMessage();
            LOG.error(errMsg); 
            return "{\"status\":\"failed\",\"message\":\""+errMsg+"\"}";
        }
    }

    @RequestMapping(value = "/webemail/send/{emailAddress}", method = {RequestMethod.GET , RequestMethod.POST}, 
    produces="application/json")
    @ResponseBody
    public String sendWebEmail(@PathVariable("emailAddress") String emailAddress) {
        try {
            // compose message
            emailAddress = decodeEmailAddress(emailAddress);
            LOG.debug("Send Web Email Address: " + emailAddress);
            SimpleSmtpMessage smtpMessage = new SimpleSmtpMessage();
            smtpMessage.setBody("<html><body><h1>Test Email - \" + System.currentTimeMillis() + \"</h1></body></html>");
            smtpMessage.setSubject("Subject: Test Email");
            smtpMessage.setFrom("admin@mockemailservice.com");
            smtpMessage.setTo(emailAddress);
            smtpMessage.setReceivedDate("" + System.currentTimeMillis());
            Map<String, List<SimpleSmtpMessage>> webEmails = MockEmailServer.getInstance().getWebEmails();
            if (webEmails.containsKey(emailAddress)) {
                List<SimpleSmtpMessage> emails = webEmails.get(emailAddress);
                emails.add(smtpMessage);
            } else {
                List<SimpleSmtpMessage> emails = new ArrayList<>();
                emails.add(smtpMessage);
                webEmails.put(emailAddress, emails);
                incrementEmailAddressCounter();
            }
            incrementWebEmailSent();
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            String errMsg = "Unable to send email!" + e.getMessage();
            LOG.error(errMsg); 
            return "{\"status\":\"failed\",\"message\":\""+errMsg+"\"}";
        }
    }

    @Scheduled(fixedRate=300000)
    public void emailHealthCheck()  {
        // every 5 minutes log the status
        int msgCount = MockEmailServer.getInstance().getSimpleSmtpServer().getReceivedEmailSize();
        LOG.info("Email Health Check - Every 5 mins - Total Email Msg: " + msgCount + 
                " , Total memory: " + Runtime.getRuntime().totalMemory() + " , Free memory: " + Runtime.getRuntime().freeMemory());
    }

    @Scheduled(fixedRate = 300000)
    public void updateWebEmailMetrics() {
        // every 5 mins
        Map<String, List<SimpleSmtpMessage>> webEmails = MockEmailServer.getInstance().getWebEmails();
        long webEmailInMemoryCount = 0;
        for (String emailAddress : webEmails.keySet()) {
            webEmailInMemoryCount += webEmails.get(emailAddress).size();
        }
        Tags tags = Tags.of("email-type", "web");
        meterRegistry.gauge("webmail.inmemory.count", tags, webEmailInMemoryCount);
    }

    private void incrementEmailAddressCounter() {
        emailAddressCounter.increment();
    }

    private void incrementWebEmailSent() {
        webEmailCounter.increment();
    }

}
