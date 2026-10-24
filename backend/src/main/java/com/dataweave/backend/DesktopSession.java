package com.dataweave.backend;

import org.springframework.context.ConfigurableApplicationContext;

public final class DesktopSession implements AutoCloseable {

    private final ConfigurableApplicationContext context;
    private final ApplicationFacade facade;

    private DesktopSession(ConfigurableApplicationContext context) {
        this.context = context;
        this.facade = context.getBean(ApplicationFacade.class);
    }

    public static DesktopSession start(String... args) {
        return new DesktopSession(DataWeaveBackendApplication.start(args));
    }

    public static DesktopSession startForGui(String... args) {
        return new DesktopSession(DataWeaveBackendApplication.startForGui(args));
    }

    public ApplicationFacade facade() {
        return facade;
    }

    @Override
    public void close() {
        facade.shutdown();
        context.close();
    }
}
