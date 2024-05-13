package Project.FishingNet_thesis.table;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("fishing_defects")
public class FishingDefect {
    @Id
    private String id;
    private String filename;
    private String image;
    private String timestamp;
}