package hexagonal.adapters;

import hexagonal.domain.entities.Ride;

public interface IRideSerializer {
    String serialize(Ride ride);
    Ride deserialize(String rideData);
}
