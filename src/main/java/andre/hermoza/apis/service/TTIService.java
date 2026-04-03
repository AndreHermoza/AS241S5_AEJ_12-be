package andre.hermoza.apis.service;

import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.repository.TTIRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@Slf4j
public class TTIService {

    private final WebClient webClient;
    private final TTIRepository TTIRepo;

    public TTIService(TTIRepository ttiRepo, @Qualifier("fluxClient") WebClient webClient) {
        this.TTIRepo = ttiRepo;
        this.webClient = webClient;
    }

    public Flux<textToImage> findAll() {
        log.info("Listando los datos");
        return TTIRepo.findAll();
    }

    public Mono<textToImage> findByID(Integer id) {
        log.info("Listando por ID:"  + id);
        return TTIRepo.findById(id);
    }

    public Mono<textToImage> generateImage(String prompt) {
        log.info("Iniciando petición a la IA con el prompt: {}", prompt);

        Map<String, Object> body = Map.of(
                "prompt", prompt,
                "style_id", 4,
                "size", "1-1"
        );

        return webClient.post()
                .uri("/aaaaaaaaaaaaaaaaaiimagegenerator/quick.php")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    textToImage entity = new textToImage();
                    entity.setPrompt(prompt);
                    entity.setStatus(true);

                    try {
                        // 1. Obtenemos la lista "final_result"
                        java.util.List<Map<String, Object>> results = (java.util.List<Map<String, Object>>) response.get("final_result");

                        if (results != null && !results.isEmpty()) {
                            // 2. Tomamos el primer objeto de la lista y el campo "origin"
                            String imageUrl = results.get(0).get("origin").toString();
                            entity.setGenerated_image_url(imageUrl);
                            log.info("URL extraída con éxito: {}", imageUrl);
                        } else {
                            return Mono.error(new RuntimeException("La API no devolvió resultados en final_result"));
                        }
                    } catch (Exception e) {
                        log.error("Error al parsear la respuesta de la IA: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Formato de respuesta inesperado"));
                    }

                    return TTIRepo.save(entity);
                })
                .onErrorResume(e -> {
                    log.error("Error en el flujo: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error: " + e.getMessage()));
                });
    }
    public Mono<textToImage> setStatus (Integer id, boolean status) {
        log.info("Estado cambiado a: {}" , status);
        return TTIRepo.findById(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(status);
                    return TTIRepo.save(textToImage);
                });
    }
}