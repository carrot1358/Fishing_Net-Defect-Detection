package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.ERole;
import Project.FishingNet_thesis.models.RoleDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<RoleDocument, String>{
    Optional<RoleDocument> findByName(ERole name);
}
