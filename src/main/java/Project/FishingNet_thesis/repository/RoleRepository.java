package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.ERole;
import Project.FishingNet_thesis.models.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String>{
    Optional<Role> findByName(ERole name);
}
