package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.TokenPassResetDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TokenPassResetRepository extends MongoRepository<TokenPassResetDocument, String> {

    TokenPassResetDocument findByToken(String token);

//    TokenPassResetDocument findByEmail(String email);
//    void deleteByEmail(String email);
//    void deleteByToken(String token);
}
