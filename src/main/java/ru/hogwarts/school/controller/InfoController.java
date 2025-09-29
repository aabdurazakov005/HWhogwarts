package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InfoController {

    @Value("${server.port}")
    private Integer serverPort;

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.profile}")
    private String appProfile;

    @Value("${app.description}")
    private String appDescription;

    @GetMapping("/port")
    public Integer getServerPort() {
        return serverPort;
    }

    @GetMapping("/info")
    public Map<String, Object> getAppInfo() {
        return Map.of(
                "name", appName,
                "version", appVersion,
                "profile", appProfile,
                "description", appDescription,
                "port", serverPort
        );
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return appProfile;
    }
}