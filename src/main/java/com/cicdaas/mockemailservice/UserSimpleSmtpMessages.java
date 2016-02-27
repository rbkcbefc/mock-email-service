package com.cicdaas.mockemailservice;

import java.util.ArrayList;
import java.util.List;

public class UserSimpleSmtpMessages {

    public String emailAddress;
    public List<SimpleSmtpMessage> msgs;

    public UserSimpleSmtpMessages() {
        emailAddress = new String();
        msgs = new ArrayList<SimpleSmtpMessage>();
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<SimpleSmtpMessage> getMsgs() {
        return msgs;
    }

    public void setMsgs(List<SimpleSmtpMessage> msgs) {
        this.msgs = msgs;
    }

}
