package management_service.repositories;

import management_service.entities.EScooter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EScooterRepo extends MongoRepository<EScooter, String> {
}
