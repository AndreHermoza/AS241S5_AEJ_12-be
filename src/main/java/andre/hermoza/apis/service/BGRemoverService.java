package andre.hermoza.apis.service;

import andre.hermoza.apis.model.BGRemover;
import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.repository.BGRemoverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BGRemoverService {

    private final WebClient webClient;
    private final WebClient genericClient;
    private final BGRemoverRepository BGRepo;

    public BGRemoverService(BGRemoverRepository BGRepo, @Qualifier("removerClient") WebClient webClient) {
        this.BGRepo = BGRepo;
        this.webClient = webClient;
        this.genericClient = WebClient.create();
    }

    public Flux<BGRemover> findAll() {
        log.info("Listando los datos");
        return BGRepo.findAll();
    }

    public Mono<BGRemover> findByID(Integer id) {
        log.info("Listando por ID:"  + id);
        return BGRepo.findById(id);
    }

    public Mono<BGRemover> removeBackground(String imageUrl) {
        log.info("Iniciando proceso para URL: {}", imageUrl);

        return genericClient.get()
                .uri(imageUrl)
                .accept(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG)
                .retrieve()
                .bodyToMono(byte[].class)
                .flatMap(imageBytes -> {
                    log.info("Imagen descargada con éxito. Tamaño: {} bytes", imageBytes.length);

                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("image", new ByteArrayResource(imageBytes))
                            .filename("image.png")
                            .contentType(MediaType.IMAGE_PNG);
                    builder.part("model", "falcon");

                    return webClient.post()
                            .uri("/image/matte/v1")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(BodyInserters.fromMultipartData(builder.build()))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response ->
                                    response.bodyToMono(String.class).flatMap(error -> {
                                        log.error("Error de RapidAPI: {}", error);
                                        return Mono.error(new RuntimeException(error));
                                    })
                            )
                            .bodyToMono(byte[].class) // Imagen recibida
                            .flatMap(processedImageBytes -> {
                                log.info("¡Éxito! Recibidos {} bytes de la imagen sin fondo", processedImageBytes.length);


                                String fileName = "resultado_" + System.currentTimeMillis() + ".png";
                                try {
                                    java.nio.file.Path path = java.nio.file.Paths.get(fileName);
                                    java.nio.file.Files.write(path, processedImageBytes);
                                    log.info("Imagen guardada localmente: {}", path.toAbsolutePath());
                                } catch (java.io.IOException e) {
                                    log.error("Error guardando imagen en disco", e);
                                }


                                BGRemover entity = new BGRemover();
                                entity.setSource_image_url(imageUrl);

                                // Se guarda el archivo
                                entity.setProcessed_image_url("Archivo guardado: " + fileName);

                                return BGRepo.save(entity);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Fallo en el flujo: {}", e.getMessage());
                    return Mono.error(new RuntimeException("Error: " + e.getMessage()));
                });
    }

    public Mono<BGRemover> setStatus (Integer id, boolean status) {
        log.info("Estado cambiado a: {}" , status);
        return BGRepo.findById(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(status);
                    return BGRepo.save(textToImage);
                });
    }
}
