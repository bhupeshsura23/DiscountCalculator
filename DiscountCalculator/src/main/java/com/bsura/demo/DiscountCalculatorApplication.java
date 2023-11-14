package com.bsura.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class DiscountCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscountCalculatorApplication.class, args);
	}

}
