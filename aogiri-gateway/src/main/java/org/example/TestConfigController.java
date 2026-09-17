package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope // Дозволяє оновлювати дані на льоту
public class TestConfigController {

    @Value("${lab8.environment:Дані не знайдено}")
    private String environmentData;

    @GetMapping("/api/config-test")
    public String getConfigData() {
        return "Поточні налаштування: " + environmentData;
    }
}