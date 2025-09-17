package com.shopQ.MainShopQ;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.shopQ.MainShopQ.entity")
@EnableJpaRepositories("com.shopQ.MainShopQ")
public class MainShopQApplication {

	public static void main(String[] args) {
		SpringApplication.run(MainShopQApplication.class, args);
	}

}
