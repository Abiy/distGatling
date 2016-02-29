package com.walmart.store.location;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {


    public static void main(String[] args) throws Exception {
        //new JHades().overlappingJarsReport();
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public StartupRunner schedulerRunner() {
        return new StartupRunner();
    }

}




