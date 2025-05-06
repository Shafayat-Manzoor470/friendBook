package com.webkorps.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.webkorps.main.repository")
public class FriendBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(FriendBookApplication.class, args);
	}

}
