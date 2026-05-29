package andre.hermoza.apis.rest;

import andre.hermoza.apis.model.BGRemover;
import andre.hermoza.apis.service.BGRemoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1/bgremover")
@CrossOrigin(origins = "*")
@Tag(name = "Background Remover", description = "Eliminación de fondos en imágenes mediante IA")
public class BGRemoverRest {

    private final BGRemoverService BGService;

    public BGRemoverRest(BGRemoverService bgService) {
        BGService = bgService;
    }

    @GetMapping
    @Operation(summary = "Listar procesamientos", description = "Obtiene el historial de imágenes procesadas")
    public Flux<BGRemover> findAll() {
        return BGService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID", description = "Busca un procesamiento por su identificador")
    public Mono<BGRemover> findById(
            @Parameter(description = "Identificador del procesamiento") @PathVariable Integer id) {
        return BGService.findByID(id);
    }

    @GetMapping(value = "/images/{filename:.+}", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Descargar imagen procesada", description = "Sirve una imagen PNG almacenada localmente")
    @ApiResponse(responseCode = "200", description = "Imagen encontrada", content = @Content(mediaType = "image/png"))
    public Mono<Resource> getImage(
            @Parameter(description = "Nombre del archivo de imagen") @PathVariable String filename) {
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
    @Operation(summary = "Procesar imagen", description = "Recibe un archivo de imagen, elimina el fondo y persiste el resultado")
    @ApiResponse(responseCode = "200", description = "Imagen procesada correctamente")
    public Mono<BGRemover> process(
            @RequestBody(
                    description = "Archivo de imagen a procesar",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(BGService::removeBackgroundFromFile);
    }

    @PatchMapping("/deactivate/{id}")
    @Operation(summary = "Desactivar registro", description = "Cambia el estado del registro a inactivo")
    public Mono<BGRemover> deactivate(
            @Parameter(description = "Identificador del procesamiento") @PathVariable("id") Integer id) {
        return BGService.findByID(id)
                .flatMap(textToImage -> BGService.setStatus(id, false));
    }

    @PatchMapping("/activate/{id}")
    @Operation(summary = "Activar registro", description = "Cambia el estado del registro a activo")
    public Mono<BGRemover> activate(
            @Parameter(description = "Identificador del procesamiento") @PathVariable("id") Integer id) {
        return BGService.findByID(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(true);
                    return BGService.setStatus(id, true);
                });
    }
}
