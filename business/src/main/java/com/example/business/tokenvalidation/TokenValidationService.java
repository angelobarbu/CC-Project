package com.example.business.tokenvalidation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@PropertySource("classpath:application.properties")
public class TokenValidationService {
    private final WebClient webClient;

    public TokenValidationService(WebClient.Builder webClientBuilder, @Value("${spring.auth.service.url}") String url) {
        this.webClient = webClientBuilder.baseUrl(url).build();
    }

    public boolean validateToken(String token) {
        ValidationResponse response = webClient.get()
                .uri("/validate?token=" + token)
                .retrieve()
                .bodyToMono(ValidationResponse.class)
                .block();

        return response != null && response.isValid();
    }
}