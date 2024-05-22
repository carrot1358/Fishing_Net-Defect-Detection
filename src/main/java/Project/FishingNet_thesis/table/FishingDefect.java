package Project.FishingNet_thesis.table;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document("fishing_defects")
public class FishingDefect {
    @Id
    private String id;
    private String filename;
    private String image;
    private Date timestamp;
    private boolean ismanaged;
}