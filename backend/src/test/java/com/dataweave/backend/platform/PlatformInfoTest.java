package com.dataweave.backend.platform;

import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlatformInfoTest {

    @Test
    void dataWeaveHomeIsUnderUserHome() {
        Path home = PlatformInfo.dataWeaveHome();
        Assertions.assertTrue(home.toString().contains(".dataweave"));
        Assertions.assertNotNull(PlatformInfo.os());
        Assertions.assertNotNull(PlatformInfo.cpuArchitecture());
    }
}
