package com.pedronunesdev.MenteFinanceira.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_FORMAT = "Bearer Authentication";

    @Bean
    public OpenAPI openAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Mente financeira API Documentation")
                        .description("""
                                API REST para a API do Mente Financeira |
                                
                                Endpoints marcados com o cadeado exigem token JWT no header \
                                `Authorization: Bearer {token}`, obtido em `POST /api/auth/login`.
                                """)
                        .version("v1.0")
                )
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Ambiente local")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_FORMAT))
                .components(new Components().addSecuritySchemes(SECURITY_FORMAT, createApiKeyOpenApi()));
    }

    private SecurityScheme createApiKeyOpenApi(){
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("Bearer")
                .description("Informe o token JWT obtido em `POST /api/auth/login`, no formato: `Bearer {token}`");
    }
}
