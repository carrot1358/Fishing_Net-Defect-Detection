package Project.FishingNet_thesis.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("token_reset_document")
public class TokenResetDocument {
    @Id
    private String token;
    private Date expirationDate;

    @DBRef
    private UserDocument user;

}
