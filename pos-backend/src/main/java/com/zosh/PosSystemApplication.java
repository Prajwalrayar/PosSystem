package com.zosh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PosSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PosSystemApplication.class, args);
	}

}
