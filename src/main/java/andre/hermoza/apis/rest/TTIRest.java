package andre.hermoza.apis.rest;

import andre.hermoza.apis.model.GenerateImageRequest;
import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.service.TTIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/tti")
@Tag(name = "Text to Image", description = "Generación de imágenes con IA a partir de prompts de texto")
public class TTIRest {

    private final TTIService ttiService;

    public TTIRest(TTIService ttiService) {
        this.ttiService = ttiService;
    }

    @GetMapping
    @Operation(summary = "Listar generaciones", description = "Obtiene el historial de imágenes generadas")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public Flux<textToImage> findAll() {
        return ttiService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID", description = "Busca una generación por su identificador")
    @ApiResponse(responseCode = "200", description = "Generación encontrada")
    public Mono<textToImage> findById(
            @Parameter(description = "Identificador de la generación") @PathVariable Integer id) {
        return ttiService.findByID(id);
    }

    @PostMapping("/generate")
    @Operation(summary = "Generar imagen", description = "Envía un prompt a la IA y persiste el resultado")
    @ApiResponse(responseCode = "200", description = "Imagen generada correctamente")
    public Mono<textToImage> generate(@RequestBody GenerateImageRequest request) {
        return ttiService.generateImage(request.prompt());
    }

    @PatchMapping("/deactivate/{id}")
    @Operation(summary = "Desactivar registro", description = "Cambia el estado del registro a inactivo")
    public Mono<textToImage> deactivate(
            @Parameter(description = "Identificador de la generación") @PathVariable("id") Integer id) {
        return ttiService.findByID(id)
                .flatMap(textToImage -> ttiService.setStatus(id, false));
    }

    @PatchMapping("/activate/{id}")
    @Operation(summary = "Activar registro", description = "Cambia el estado del registro a activo")
    public Mono<textToImage> activate(
            @Parameter(description = "Identificador de la generación") @PathVariable("id") Integer id) {
        return ttiService.findByID(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(true);
                    return ttiService.setStatus(id, true);
                });
    }
}
