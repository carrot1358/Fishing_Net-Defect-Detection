package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.FishingDefectDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface FishingDefectRepository extends MongoRepository<FishingDefectDocument, String> {

    List<FishingDefectDocument> findByTimestamp(Date date);
}
