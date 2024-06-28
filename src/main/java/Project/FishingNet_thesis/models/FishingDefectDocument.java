package Project.FishingNet_thesis.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@Document("fishing_defects")
public class FishingDefectDocument {
    @Id
    private String id;
    private String filename;
    private String image;
    private Date timestamp;
    private boolean ismanaged;
    private String status;
    @DBRef
    private UserDocument userDocument;
}