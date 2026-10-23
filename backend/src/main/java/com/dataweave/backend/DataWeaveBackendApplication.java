package com.dataweave.backend;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableCaching
public class DataWeaveBackendApplication {

    public static ConfigurableApplicationContext start(String... args) {
        return new SpringApplicationBuilder(DataWeaveBackendApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off")
                .run(args);
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = start(args)) {
            System.out.println("Backend started (no UI). Beans: " + ctx.getBeanDefinitionCount());
        }
    }
}
