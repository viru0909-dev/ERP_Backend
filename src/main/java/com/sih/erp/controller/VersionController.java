package com.sih.erp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class VersionController {

    @GetMapping("/api/public/version")
    public Map<String, String> getVersion() {
        // This is our test. If we see this message, we know the new code is live.
        return Map.of("version", "v3.0-FINAL-SECURITY-FIX");
    }
}