package com.microfocus.app;

import org.slf4j.LoggerFactory;

import java.io.*;

public class AdminHelper {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AdminHelper.class);

    AdminHelper() {

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
