package com.song.lad.sfeature;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(exclude =
		{DataSourceAutoConfiguration.class,
				DataSourceTransactionManagerAutoConfiguration.class,
				SecurityAutoConfiguration.class})
@EnableAsync(proxyTargetClass = true)
//filter不属于spring自动管理的组件,需要手动扫描
@ServletComponentScan(basePackages = {"core.validate.filter"})
public class SfeatureApplication {

	public static void main(String[] args) {
		SpringApplication.run(SfeatureApplication.class, args);
	}

}
