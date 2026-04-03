package andre.hermoza.apis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(name = "flux_image_generations")
public class textToImage {
    @Id
    private Integer id;
    private String prompt;

    @Column("generated_image_url")
    private String generated_image_url;

    private boolean status = true;
    private LocalDateTime created_at;
}
