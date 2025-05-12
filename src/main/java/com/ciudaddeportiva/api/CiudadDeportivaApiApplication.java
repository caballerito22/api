package com.ciudaddeportiva.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//activamos el scheduler!
@EnableScheduling

public class CiudadDeportivaApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CiudadDeportivaApiApplication.class, args);
	}

}
