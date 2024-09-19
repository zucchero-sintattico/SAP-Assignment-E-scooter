package hexagonal.ports;

import io.vertx.core.Future;
import hexagonal.ports.exceptions.RideAlreadyEndedException;
import hexagonal.ports.exceptions.RideNotFoundException;
import hexagonal.ports.exceptions.RideNotPossibleException;

public interface IRideUseCases {
    String startNewRide(String userId, String escooterId) throws RideNotPossibleException;
    Future<String> getRideInfo(String id) throws RideNotFoundException;
    Future<Void> endRide(String rideId) throws RideNotFoundException, RideAlreadyEndedException;
    Future<Integer> getNumberOfOngoingRides();
}
