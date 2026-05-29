package andre.hermoza.apis.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${server.url}") String serverUrl) {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Services API")
                        .version("1.0.0")
                        .description("API reactiva para generación de imágenes (Text-to-Image) y eliminación de fondos (Background Remover).")
                        .contact(new Contact()
                                .name("Andre Hermoza")
                                .email("andre.hermoza@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url(serverUrl).description("Servidor configurado")
                ));
    }
}
