package com.limito.payment.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class PortOneWebClientConfig {

	@Value("${portone.base-url}")
	private String baseUrl;

	@Value("${portone.api-secret}")
	private String apiSecret;

	@Bean
	public WebClient portOneWebClient() {
		log.info("PortOne apiSecret={}", apiSecret);
		return WebClient.builder()
			.baseUrl(baseUrl)
			.defaultHeader("Authorization", "PortOne " + apiSecret)
			.exchangeStrategies(
				ExchangeStrategies.builder()
					.codecs(config -> config
						.defaultCodecs()
						.maxInMemorySize(10 * 1024 * 1024))
					.build()
			)
			.build();
	}
}
