package andre.hermoza.apis.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${api.rapidapi.key}")
    private String apiKey;

    @Bean
    @Qualifier("fluxClient")
    public WebClient fluxWebClient() {
        return WebClient.builder()
                .baseUrl("https://ai-text-to-image-generator-flux-free-api.p.rapidapi.com")
                .defaultHeader("x-rapidapi-host", "ai-text-to-image-generator-flux-free-api.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean
    @Qualifier("removerClient")
    public WebClient removerWebClient() {
        return WebClient.builder()
                .baseUrl("https://ai-background-remover.p.rapidapi.com")
                .defaultHeader("x-rapidapi-host", "ai-background-remover.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
}