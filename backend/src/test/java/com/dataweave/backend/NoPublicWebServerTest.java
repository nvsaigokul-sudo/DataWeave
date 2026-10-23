package com.dataweave.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import com.dataweave.backend.support.IntegrationTestBase;

class NoPublicWebServerTest extends IntegrationTestBase {

    @Autowired Environment environment;

    @Test
    void webApplicationTypeIsNone() {
        Assertions.assertEquals("none", environment.getProperty("spring.main.web-application-type"));
    }
}
