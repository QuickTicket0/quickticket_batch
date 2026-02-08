package com.quickticket.quickticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class QuickticketApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuickticketApplication.class, args);
	}

}
