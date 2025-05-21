package com.webkorps.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.webkorps.main.repository")
@EntityScan(basePackages = "com.webkorps.main.entity")
public class FriendBookApplication {

	public static void main(String[] args) {
		SpringApplication.run(FriendBookApplication.class, args);
	}

}
