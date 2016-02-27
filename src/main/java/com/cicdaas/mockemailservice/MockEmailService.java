package com.cicdaas.mockemailservice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class MockEmailService {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MockEmailService.class);

    @Autowired
    private JavaMailSenderImpl mailSender;

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
        LOG.debug("Received Email Size: " + smtpServer.getReceivedEmailSize());
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
        LOG.debug("List of messages: " + listOfMsg.size());
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
        String decodedEmailId = new String(Base64.decodeBase64(emailAddress.getBytes()));
        // when Email Id is NOT encoded, safely return the original Email ID with domain as .com
        return (decodedEmailId.contains("@") && decodedEmailId.contains(".") && decodedEmailId.length() > 5) ? 
                decodedEmailId : emailAddress + ".com";
    }

    @RequestMapping(value = "/email/send/{emailAddress}", method = {RequestMethod.GET , RequestMethod.POST}, 
            produces="application/json")
    @ResponseBody
    public String sendEmail(@PathVariable("emailAddress") String emailAddress) {
        try {
            // compose message
            emailAddress = decodeEmailAddress(emailAddress);
            LOG.debug("Send Email Address: " + emailAddress);
            MimeMessage mmsg = mailSender.createMimeMessage();
            mmsg.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(emailAddress));
    
            MimeMultipart content = new MimeMultipart("related");
            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent("<html><body><h1>Test Email - " + System.currentTimeMillis() + "</h1></body></html>", "text/html");
            content.addBodyPart(htmlPart);
            mmsg.setContent(content);
            mmsg.setFrom(new InternetAddress("admin@mockemailservice.com"));
            mmsg.setSubject("Subject: Test Email");
            mailSender.send(mmsg);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            String errMsg = "Unable to send email!" + e.getMessage(); 
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


}
