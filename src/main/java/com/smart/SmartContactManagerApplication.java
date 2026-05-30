package com.smart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.smart")
public class SmartContactManagerApplication {

	public static void main(String[] args) {
	
		SpringApplication.run(SmartContactManagerApplication.class, args);
	
	System.out.println("****************************************");
	}

}
