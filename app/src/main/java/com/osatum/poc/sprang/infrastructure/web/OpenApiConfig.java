package com.osatum.poc.sprang.infrastructure.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Konfiguracja OpenAPI / Swagger UI dla systemu JML – dostęp do danych.
 * Dane kontaktowe: Paweł Koncewicz, pawel.koncewicz@osatum.com
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JML – dostęp do danych (IP / instytucje / zasoby)",
                version = "1.0.0",
                description = "API systemu JML do zarządzania dostępem do zasobów w oparciu o instytucje i adresy IP",
                contact = @Contact(
                        name = "Paweł Koncewicz",
                        email = "pawel.koncewicz@osatum.com"
                )
        )
)
public class OpenApiConfig {
}
