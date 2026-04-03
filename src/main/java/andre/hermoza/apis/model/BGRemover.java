package andre.hermoza.apis.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Table(name = "background_removals")
public class BGRemover {

    @Id
    private Integer id;

    @Column("source_image_url")
    private String source_image_url;

    @Column("processed_image_url")
    private String processed_image_url;

    private boolean status = true;

    @Column("created_at")
    private LocalDateTime created_at = LocalDateTime.now();
}
