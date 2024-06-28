package Project.FishingNet_thesis.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "roles_document")
public class RoleDocument {
    @Id
    private String id;
    private ERole name;



}
