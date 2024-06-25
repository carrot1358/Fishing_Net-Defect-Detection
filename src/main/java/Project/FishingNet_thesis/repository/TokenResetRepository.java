package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.TokenResetDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenResetRepository extends MongoRepository<TokenResetDocument, String> {

    TokenResetDocument findByToken(String token);

//    TokenResetDocument findByEmail(String email);
//    void deleteByEmail(String email);
//    void deleteByToken(String token);
}
