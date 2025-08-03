package com.iroom.sensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.iroom.sensor", "com.iroom.modulecommon"})
public class SensorApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorApplication.class, args);
	}
}
