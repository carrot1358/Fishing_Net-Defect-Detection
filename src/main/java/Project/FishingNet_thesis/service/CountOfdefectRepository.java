package Project.FishingNet_thesis.service;

import Project.FishingNet_thesis.table.CountOfdefect;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

public interface CountOfdefectRepository extends MongoRepository<CountOfdefect, String>{
    CountOfdefect findByDate(Date date);
    List<CountOfdefect> findByDefectCount(int defectCount);
    List<CountOfdefect> findByActivateCount(int activateCount);
    List<CountOfdefect> findByDeactivateCount(int deactivateCount);
}
