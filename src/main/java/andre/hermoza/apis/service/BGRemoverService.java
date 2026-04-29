package andre.hermoza.apis.service;

import andre.hermoza.apis.model.BGRemover;
import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.repository.BGRemoverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${server.url}")
    private String serverUrl;

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

    public Mono<BGRemover> removeBackgroundFromFile(FilePart filePart) {
        log.info("Recibido archivo: {}, tamaño: {}", filePart.filename(), filePart.headers().getContentLength());

        // 1. Leer el contenido del FilePart como byte[]
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .reduce((a, b) -> {
                    // En caso de que llegara en varios trozos, se concatenan
                    byte[] merged = new byte[a.length + b.length];
                    System.arraycopy(a, 0, merged, 0, a.length);
                    System.arraycopy(b, 0, merged, a.length, b.length);
                    return merged;
                })
                .defaultIfEmpty(new byte[0])
                .flatMap(fileBytes -> {
                    // 2. Construir el multipart para RapidAPI
                    MultipartBodyBuilder builder = new MultipartBodyBuilder();
                    builder.part("image", new ByteArrayResource(fileBytes))
                            .filename(filePart.filename())
                            .contentType(MediaType.IMAGE_PNG);
                    builder.part("model", "falcon");

                    // 3. Enviar a RapidAPI (igual que antes)
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
                            .bodyToMono(byte[].class)
                            .flatMap(processedImageBytes -> {
                                // 4. Guardar localmente (opcional)
                                String fileName = "resultado_" + System.currentTimeMillis() + ".png";
                                try {
                                    java.nio.file.Path dirPath = java.nio.file.Paths.get("uploads");
                                    if (!java.nio.file.Files.exists(dirPath)) {
                                        java.nio.file.Files.createDirectories(dirPath);
                                    }
                                    java.nio.file.Path path = dirPath.resolve(fileName);
                                    java.nio.file.Files.write(path, processedImageBytes);
                                    log.info("Imagen guardada localmente: {}", path.toAbsolutePath());
                                } catch (java.io.IOException e) {
                                    log.error("Error guardando imagen en disco", e);
                                }

                                // 5. Crear entidad y guardar en BD
                                BGRemover entity = new BGRemover();
                                entity.setSource_image_url("File: " + filePart.filename());
                                entity.setProcessed_image_url(serverUrl + "/api/v1/bgremover/images/" + fileName);
                                return BGRepo.save(entity);
                            });
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
