package ru.itmo.pastbin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PastbinApplication {

    public static void main(String[] args) {
        SpringApplication.run(PastbinApplication.class, args);
    }

}
