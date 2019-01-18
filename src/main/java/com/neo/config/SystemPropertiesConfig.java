package com.neo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:system.properties")
public class SystemPropertiesConfig {

    @Value("${mcLocation}")
    private String mcLocation;

    @Value("${modLocation}")
    private String modLocation;

    public String getMcLocation() {
        return mcLocation;
    }

    public void setMcLocation(String mcLocation) {
        this.mcLocation = mcLocation;
    }

    public String getModLocation() {
        return modLocation;
    }

    public void setModLocation(String modLocation) {
        this.modLocation = modLocation;
    }
}
