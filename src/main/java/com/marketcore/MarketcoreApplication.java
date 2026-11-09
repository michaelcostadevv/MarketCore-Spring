package com.marketcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO) // JSON de paginação estável
public class MarketcoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketcoreApplication.class, args);
    }
}
