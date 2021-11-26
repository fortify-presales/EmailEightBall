package com.microfocus.app;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileReader;
import java.util.Random;

@SpringBootApplication
public class EmailEightBallApp implements ApplicationRunner {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmailEightBallApp.class);
    private static final int MAX_ANSWERS = 19;

    @Autowired
    EmailEightBallConfig config;

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    public static void main(String[] args) {
        SpringApplication.run(EmailEightBallApp.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String jarPath = EmailEightBallApp.class
                .getProtectionDomain().getCodeSource()
                .getLocation().toURI().getPath();
        String USAGE = "Usage: java -jar " + jarPath + " [question] [emailTo address] [1-6]";

        String question = null;
        String filename = null;
        String operation = "test";
        String response = null;
        String emailTo = null;
        char[] buffer = new char[1024];

        if (!args.getNonOptionArgs().isEmpty()) {
            if (args.getNonOptionArgs().size() < 2) {
                log.warn(USAGE);
                return;
            }
            log.info("Using command line args: " + args.toString());
            question = args.getNonOptionArgs().get(0);
            emailTo = args.getNonOptionArgs().get(1);
            if (args.getNonOptionArgs().size() > 2) {
                filename = args.getNonOptionArgs().get(2);
            } else {
                int randomInt =  new Random().nextInt(MAX_ANSWERS);
                filename = String.valueOf(randomInt);
            }
            operation = "send";
        } else {
            log.info("Using default Spring arguments");
            question = config.getQuestion();
            filename = config.getInputFile();
            emailTo = config.getAppEmail();
            operation = config.getAppOperation();
        }

        try {
            filename = "." + File.separatorChar + "data" +
                    File.separatorChar + filename;
            log.info("Reading from file: " + filename);
        } catch (Exception e) {
            System.out.println("Invalid input.");
        }

        new FileReader(filename).read(buffer);
        response = String.valueOf(buffer);

        // send a emailTo address with defined or random content
        if (operation.equals("send")) {
            if (activeProfile.equals("dev")) {
                log.debug("Email server: {}", config.getServer());
                log.debug("Server port: {}", String.valueOf(config.getPort()));
                log.debug("Server username: {}", config.getUsername());
                log.debug("Server password: {}", config.getPassword());
                log.debug("Email to: {}", emailTo);
                log.debug("Email from: {}", config.getFrom());
                log.debug("Email subject: {}", question);
                log.debug("Email response: {}", response);
            }

            try {

                EmailRequest request = new EmailRequest(
                        config.getServer(),
                        config.getPort(),
                        emailTo,
                        config.getFrom(),
                        question,
                        response
                );
                request.setBounce("eightball-bounce@localhost.com");
                if (activeProfile.equals("dev")) {
                    request.setDebug(true);
                }
                AppHelper.sendEmail(request);

            } catch (EmailException ex) {
                ex.printStackTrace();
            }

            log.info("Email sent successfully, exiting...");
        }

        // dump the contents of one or more filenames
        if (operation.equals("dump")) {
            String command[] = {"more", filename};
            AppHelper.runCommand(command);
        }
    }

}
