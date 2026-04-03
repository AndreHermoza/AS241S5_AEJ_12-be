package andre.hermoza.apis.rest;

import andre.hermoza.apis.model.BGRemover;
import andre.hermoza.apis.service.BGRemoverService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @PostMapping("/process")
    public Mono<BGRemover> process(@RequestBody Map<String, String> request) {
        String img_url = request.get("source_image_url");
        return BGService.removeBackground(img_url);
    }
}
