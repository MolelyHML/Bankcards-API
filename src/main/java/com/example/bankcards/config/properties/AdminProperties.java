package com.example.bankcards.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("bankcards")
public class AdminProperties {
    private String adminUsername;
    private String adminPassword;
}
