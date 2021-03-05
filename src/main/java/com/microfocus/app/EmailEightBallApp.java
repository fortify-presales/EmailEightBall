package com.microfocus.app;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;

@SpringBootApplication
public class EmailEightBallApp implements ApplicationRunner {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmailEightBallApp.class);

    @Autowired
    EmailEightBallConfig config;

    public static void main(String[] args) {
        SpringApplication.run(EmailEightBallApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String filename = null;
        String operation = "test";
        String body = null;
        char[] buffer = new char[1024];

        if (!args.getNonOptionArgs().isEmpty()) {
            log.debug("Using command line args: " + args.toString());
            filename = args.getNonOptionArgs().get(0);
            operation = "send";
        } else {
            log.debug("Using Spring arguments");
            filename = config.getInputFile();
            operation = config.getAppOperation();
        }
        try {
            filename = "" + (Integer.parseInt(filename) % 3);
            log.debug("Reading from file: " + filename);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }
        new FileReader(filename).read(buffer);
        body = String.valueOf(buffer);
        log.debug("Email body: {}", body);

        if (operation.equals("send")) {
            try {

                String subject = "This is the first line\r\n" +
                        "From: Someone <someone@mycompany.com>";

                Email server = new SimpleEmail();
                server.setHostName(config.getServer());
                server.setSmtpPort(config.getPort());
                server.setAuthentication(config.getUsername(), config.getPassword());
                server.setSSLOnConnect(true);
                server.setDebug(true);
                server.addTo(config.getAppEmail());
                server.setFrom(config.getFrom());
                server.setSubject(subject);
                server.setMsg(body);
                server.send();

            } catch (EmailException e) {
                e.printStackTrace();
            }
        }
    }

}
