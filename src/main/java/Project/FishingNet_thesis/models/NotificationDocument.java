package Project.FishingNet_thesis.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("notification_document")
public class NotificationDocument {
    @Id
    private String id;
    private String name;
    private String token;
    private Boolean isSendLink;
    private Boolean isSendImage;
    @DBRef
    private UserDocument userDocument;

    public NotificationDocument WithOutUserDocument(){
        this.userDocument = null;
        return this;
    }
}
