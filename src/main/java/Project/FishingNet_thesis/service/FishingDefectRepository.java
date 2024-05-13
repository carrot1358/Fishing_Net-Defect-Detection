package Project.FishingNet_thesis.service;

import Project.FishingNet_thesis.table.FishingDefect;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FishingDefectRepository extends MongoRepository<FishingDefect, String> {
    
}
