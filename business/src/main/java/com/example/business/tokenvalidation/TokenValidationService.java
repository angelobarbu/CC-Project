package com.example.business.tokenvalidation;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TokenValidationService {

    private final WebClient webClient;

    public TokenValidationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:30201/validateToken").build();
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