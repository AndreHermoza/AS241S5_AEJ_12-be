package andre.hermoza.apis.rest;

import andre.hermoza.apis.model.BGRemover;
import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.service.BGRemoverService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bgremover")
@CrossOrigin(origins = "*")
public class BGRemoverRest {

    private final BGRemoverService BGService;

    public BGRemoverRest(BGRemoverService bgService) {
        BGService = bgService;
    }

    @GetMapping
    public Flux<BGRemover> findAll() {
        return BGService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<BGRemover> findById(@PathVariable Integer id) {
        return BGService.findByID(id);
    }

    @GetMapping(value = "/images/{filename:.+}", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<Resource> getImage(@PathVariable String filename) {
        try {
            Path path = Paths.get("uploads").resolve(filename);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return Mono.just(resource);
            } else {
                return Mono.error(new RuntimeException("No se pudo leer la imagen"));
            }
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error al cargar la imagen: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<BGRemover> process(
            @RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(BGService::removeBackgroundFromFile);
    }

    @PatchMapping("/deactivate/{id}")
    public Mono<BGRemover> deactivate(@PathVariable("id") Integer id){
        return BGService.findByID(id)
                .flatMap(textToImage -> {
                    return BGService.setStatus(id, false);
                });
    }

    @PatchMapping("/activate/{id}")
    public Mono<BGRemover> activate(@PathVariable("id") Integer id){
        return BGService.findByID(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(true);
                    return BGService.setStatus(id, true);
                });
    }
}
