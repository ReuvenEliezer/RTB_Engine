package com.iiq.rtbEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
public class RtbEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RtbEngineApplication.class, args);
	}

}
