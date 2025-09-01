package com.griotold.prompthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PrompthubApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrompthubApplication.class, args);
	}

}
