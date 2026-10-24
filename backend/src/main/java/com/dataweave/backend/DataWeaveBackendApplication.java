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
        return startInternal(false, args);
    }

    /** Compose Desktop needs a non-headless AWT toolkit; default Spring headless mode breaks GUI. */
    public static ConfigurableApplicationContext startForGui(String... args) {
        return startInternal(true, args);
    }

    private static ConfigurableApplicationContext startInternal(boolean forGui, String... args) {
        SpringApplicationBuilder builder =
                new SpringApplicationBuilder(DataWeaveBackendApplication.class)
                        .web(WebApplicationType.NONE)
                        .properties("spring.main.banner-mode=off");
        if (forGui) {
            builder.headless(false);
        }
        return builder.run(args);
    }

    public static void main(String[] args) {
        try (ConfigurableApplicationContext ctx = start(args)) {
            System.out.println("Backend started (no UI). Beans: " + ctx.getBeanDefinitionCount());
        }
    }
}
