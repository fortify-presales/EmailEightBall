package com.microfocus.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileReader;

@SpringBootApplication
public class SpringEightBallApp implements ApplicationRunner {

	@Value("${input:0}")
	private String inputFile;

	public static void main(String[] args) {

		SpringApplication.run(SpringEightBallApp.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		char[] buffer = new char[1024];
		String filename = inputFile;
		try {
			filename = "" + (Integer.parseInt(filename) % 3);
		} catch (Exception e) {
			System.out.println("Invalid input.");
		}
		new FileReader(filename).read(buffer);
		System.out.println(buffer);
	}

}
