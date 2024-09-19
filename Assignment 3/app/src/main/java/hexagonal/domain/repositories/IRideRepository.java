package hexagonal.domain.repositories;

import hexagonal.domain.entities.Ride;

import java.util.Optional;
import io.vertx.core.Future;

public interface IRideRepository {
    Future<Void> save(Ride ride);
    Future<Optional<Ride>> findRideById(String id);
    Future<Integer> getNumberOfOnGoingRides();
}
