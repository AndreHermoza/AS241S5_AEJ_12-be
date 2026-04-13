package andre.hermoza.apis.rest;

import andre.hermoza.apis.model.textToImage;
import andre.hermoza.apis.service.TTIService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/tti")

public class TTIRest {

    private final TTIService ttiService;


    public TTIRest( TTIService ttiService) {
        this.ttiService = ttiService;
    }

    @GetMapping
    public Flux<textToImage> findAll() {
        return ttiService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<textToImage> findById(@PathVariable Integer id) {
        return ttiService.findByID(id);
    }

    @PostMapping("/generate")
    public Mono<textToImage> generate(@RequestBody Map<String, String> request) {
        String prompt = request.get("prompt");
        return ttiService.generateImage(prompt);
    }

    @PatchMapping("/deactivate/{id}")
    public Mono<textToImage> deactivate(@PathVariable("id") Integer id){
        return ttiService.findByID(id)
                .flatMap(textToImage -> {
                    return ttiService.setStatus(id, false);
                });
    }

    @PatchMapping("/activate/{id}")
    public Mono<textToImage> activate(@PathVariable("id") Integer id){
        return ttiService.findByID(id)
                .flatMap(textToImage -> {
                    textToImage.setStatus(true);
                    return ttiService.setStatus(id, true);
                });
    }

}
