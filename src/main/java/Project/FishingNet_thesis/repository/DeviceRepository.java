package Project.FishingNet_thesis.repository;

import Project.FishingNet_thesis.models.DeviceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<DeviceDocument, String> {
    DeviceDocument findByDeviceName (String deviceName);
}
