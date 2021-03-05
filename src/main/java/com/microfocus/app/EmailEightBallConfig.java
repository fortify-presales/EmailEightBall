package com.microfocus.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
@EnableAutoConfiguration
public class EmailEightBallConfig {

    // the input file to read - defaults to file name "0"
    @Value("${input:0}")
    private String inputFile;

    // the operation to perform, i.e. send/test
    @Value("${app.operation}")
    private String appOperation;

    // the user to send email to
    @Value("${app.email}")
    private String appEmail;

    @Autowired
    EmailProperties emailProperties;

    public String getServer() {
        return emailProperties.getServer();
    }

    public int getPort() {
        return emailProperties.getPort();
    }

    public String getUsername() {
        return emailProperties.getUsername();
    }

    public String getPassword() {
        return emailProperties.getPassword();
    }

    public String getFrom() {
        return emailProperties.getFrom();
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getAppOperation() {
        return appOperation;
    }

    public String getAppEmail() {
        return appEmail;
    }

}
