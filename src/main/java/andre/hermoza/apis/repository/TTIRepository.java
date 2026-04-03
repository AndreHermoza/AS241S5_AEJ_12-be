package andre.hermoza.apis.repository;

import andre.hermoza.apis.model.textToImage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TTIRepository extends ReactiveCrudRepository <textToImage, Integer> {
}
