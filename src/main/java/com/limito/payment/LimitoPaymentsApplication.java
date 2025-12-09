package com.limito.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class LimitoPaymentsApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimitoPaymentsApplication.class, args);
	}

}
