package ride_service.repositories


import org.springframework.data.mongodb.repository.MongoRepository
import ride_service.entities.Ride

trait RideRepo extends MongoRepository[Ride, String]