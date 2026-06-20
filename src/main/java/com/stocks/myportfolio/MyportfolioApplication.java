package com.stocks.myportfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MyportfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyportfolioApplication.class, args);
	}

}
