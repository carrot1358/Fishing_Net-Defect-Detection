package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.NotificationDocument;
import Project.FishingNet_thesis.models.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationDocument, String> {
    //    which notification its own by the user
    List<NotificationDocument> findByUserDocumentId(String id);
    NotificationDocument findByToken(String token);
    Boolean existsByToken(String token);
    List<NotificationDocument> findByUserDocument (UserDocument userDocument);
}
