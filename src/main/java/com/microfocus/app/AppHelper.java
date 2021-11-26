package com.microfocus.app;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.LoggerFactory;

import java.io.*;

public class AppHelper {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AppHelper.class);

    AppHelper() {
    }

    public static void sendEmail(EmailRequest request) throws Exception {
        String ENDL = System.getProperty("line.separator");

        Email server = new SimpleEmail();
        server.setHostName(request.getServer());
        server.setSmtpPort(request.getPort());
        //server.setAuthenticator(new DefaultAuthenticator(request.getUsername(), request.getPassword()));
        //server.setSSLOnConnect(true);
        if (request.getDebug()) {
            server.setDebug(true);
        }
        server.addTo(request.getTo());
        server.setFrom(request.getFrom());
        server.setSubject(request.getSubject());
        server.setMsg(request.getBody());
        server.setBounceAddress(request.getBounce());
        server.send();
    }

    public static String runCommand(String[] command) throws IOException {
        String output = null;
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process p = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output += line + System.getProperty("line.separator");;
        }
        return output;
    }
}
