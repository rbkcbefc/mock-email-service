package com.cicdaas.mockemailservice;

public class SimpleSmtpMessage {

    public static final long serialVersionUID = 1L;

    public String from;
    public String to;
    public String subject;
    public String receivedDate;
    public String body;

    public SimpleSmtpMessage() {
        body = new String();
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(String receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
