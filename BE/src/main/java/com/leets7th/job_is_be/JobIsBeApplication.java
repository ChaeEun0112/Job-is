package com.leets7th.job_is_be;

import com.leets7th.job_is_be.global.ai.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@EnableConfigurationProperties(OpenAiProperties.class)
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class JobIsBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobIsBeApplication.class, args);
	}

}
