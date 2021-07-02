package com.slr.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlrAppApplication {

	public static void main(String[] args) {
		//System.setProperty("entityExpansionLimit", "10000000");
		SpringApplication.run(SlrAppApplication.class, args);
	}
}
