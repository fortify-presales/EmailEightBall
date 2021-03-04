package com.microfocus.app;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.apache.commons.mail.EmailException;

import java.io.FileReader;

@SpringBootApplication
public class EmailEightBall implements ApplicationRunner {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(EmailEightBall.class);

	// the input file to read - defaults to file name "0"
	@Value("${input:0}")
	private String inputFile;

	@Value("${email.to}")
	private String emailTo;

	public static void main(String[] args) {
		SpringApplication.run(EmailEightBall.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String filename = null;
		char[] buffer = new char[1024];

		if (!args.getNonOptionArgs().isEmpty()) {
			log.debug("Using command line args: " + args.toString());
			filename = args.getNonOptionArgs().get(0);
		} else {
			log.debug("Using Spring arguments");
			filename = inputFile;
		}
		try {
			filename = "" + (Integer.parseInt(filename) % 3);
			log.debug("Reading from file:" + filename);
		} catch (Exception e) {
			System.out.println("Invalid input.");
		}
		new FileReader(filename).read(buffer);
		log.debug(buffer.toString());

		try {

			String subject = "This is the first line\r\n" +
					"From: MyCompany <welcome@mycompany.com>";

			EmailHelper email = new EmailHelper();
			log.debug("Sending email to: " + emailTo);
			email.send(emailTo, subject, buffer.toString());

		} catch (EmailException e) {
			e.printStackTrace();
		}
	}

}
