package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByPhone(String phone);
    Optional<UserDocument> findByEmail(String email);
    Boolean existsByUsernameAndPassword(String username, String password);


}
