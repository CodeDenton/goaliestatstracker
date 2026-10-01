package com.creasevision.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cache.annotation.EnableCaching;

import com.creasevision.api.ingestion.NhlImportProperties;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableConfigurationProperties(NhlImportProperties.class)
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
