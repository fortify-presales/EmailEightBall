package com.microfocus.app;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailHelper {
    @Value("${email.server}")
    private String emailServer;

    @Value("${email.port}")
    private String emailPort;

    @Value("${email.username}")
    private String emailUsername;

    @Value("${email.password}")
    private String emailPassword;

    @Value("${email.from}")
    private String emailFrom;

    EmailHelper() {

    }

    public void send(String to, String subject, String message) throws EmailException{
        try {

            Email email = new SimpleEmail();
            email.setHostName(emailServer);
            email.setSmtpPort(Integer.parseInt(emailPort));
            email.setAuthentication(emailUsername, emailPassword);
            email.setSSLOnConnect(true);
            email.setDebug(true);
            email.addTo(to);
            email.setFrom(emailFrom);
            email.setSubject(subject);
            email.setMsg(message);
            email.send();

        } catch (EmailException e) {
            e.printStackTrace();
        }
    }
}
