package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.DefectStatisticsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface DefectStatisticsRepository extends MongoRepository<DefectStatisticsDocument, String>{
    DefectStatisticsDocument findByDate(Date date);
    List<DefectStatisticsDocument> findByDefectCount(int defectCount);
    List<DefectStatisticsDocument> findByActivateCount(int activateCount);
    List<DefectStatisticsDocument> findByDeactivateCount(int deactivateCount);
}
