package andre.hermoza.apis.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Solicitud para generar una imagen a partir de un prompt de texto")
public record GenerateImageRequest(
        @Schema(description = "Descripción textual de la imagen a generar", example = "a futuristic city at sunset")
        String prompt
) {
}
