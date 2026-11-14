package com.pedronunesdev.MenteFinanceira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MenteFinanceiraApplication {

	public static void main(String[] args) {
		SpringApplication.run(MenteFinanceiraApplication.class, args);
	}

}
