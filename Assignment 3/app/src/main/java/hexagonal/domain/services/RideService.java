package hexagonal.domain.services;

import hexagonal.adapters.IRideSerializer;
import hexagonal.domain.entities.EScooter;
import hexagonal.domain.entities.Ride;
import hexagonal.domain.entities.User;
import hexagonal.domain.repositories.IEScooterRepository;
import hexagonal.domain.repositories.IRideRepository;
import hexagonal.domain.repositories.IUserRepository;
import hexagonal.ports.IRideUseCases;
import hexagonal.ports.exceptions.RideAlreadyEndedException;
import hexagonal.ports.exceptions.RideNotFoundException;
import hexagonal.ports.exceptions.RideNotPossibleException;
import io.vertx.core.Future;

import java.util.Optional;
import java.util.logging.Logger;

public class RideService implements IRideUseCases {
    private static final Logger LOGGER = Logger.getLogger(RideService.class.getName());
    private final IUserRepository userRepository;
    private final IEScooterRepository escooterRepository;
    private final IRideRepository rideRepository;
    private final IRideSerializer rideSerializer;
    private final EScooterService escooterService;
    private long rideCounter;

    public RideService(IUserRepository userRepository, IEScooterRepository escooterRepository, IRideRepository rideRepository, IRideSerializer rideSerializer, EScooterService escooterService) {
        this.userRepository = userRepository;
        this.escooterRepository = escooterRepository;
        this.rideRepository = rideRepository;
        this.rideSerializer = rideSerializer;
        this.escooterService = escooterService;
    }

    @Override
    public String startNewRide(String userId, String escooterId) throws RideNotPossibleException {
        Optional<User> user = userRepository.findUserById(userId);
        Optional<EScooter> escooter = escooterRepository.findEScooterById(escooterId);
        if (user.isPresent() && escooter.isPresent()) {
            EScooter sc = escooter.get();
            if (sc.isAvailable()) {
                sc.setState(EScooter.EScooterState.IN_USE); // Set the state of the e-scooter as in use
                escooterService.updateEScooterState(escooterId, EScooter.EScooterState.IN_USE);
                rideCounter++;
                String rideId = "ride-" + rideCounter;
                Ride ride = new Ride(rideId, user.get(), escooter.get());
                rideRepository.save(ride); // Save the ride using the rideRepository
                return ride.getId();
            } else {
                throw new RideNotPossibleException();
            }
        } else {
            throw new RideNotPossibleException();
        }
    }

    @Override
    public Future<String> getRideInfo(String id) {
        System.out.println("Getting ride info for ID: " + id);
        return rideRepository.findRideById(id).compose(rideOpt -> {
            if (rideOpt.isPresent()) {
                return Future.succeededFuture(rideSerializer.serialize(rideOpt.get()));
            } else {
                return Future.failedFuture(new RideNotFoundException());
            }
        });
    }

    @Override
    public Future<Void> endRide(String rideId) {
        LOGGER.info("Attempting to end ride with ID: " + rideId);
        return rideRepository.findRideById(rideId).compose(rideOpt -> {
            if (rideOpt.isPresent()) {
                Ride ri = rideOpt.get();
                System.out.println("Ride ID: " + ri.getId());
                System.out.println("IS going: " + ri.isOngoing());
                EScooter scooter = ri.getEScooter();
                System.out.println("EScooter ID: " + scooter.getId());
                if (ri.isOngoing()) {
                    ri.end();
                    System.out.println("Ride ID ended: " + ri.getId());
                    return rideRepository.save(ri).compose(v -> {
                        LOGGER.info("Successfully ended ride with ID: " + rideId);
                        scooter.setState(EScooter.EScooterState.AVAILABLE);
                        escooterService.updateEScooterState(scooter.getId(), EScooter.EScooterState.AVAILABLE);
                        return Future.succeededFuture();
                    });
                } else {
                    LOGGER.warning("Attempted to end ride with ID: " + rideId + ", but it was already ended");
                    return Future.failedFuture(new RideAlreadyEndedException());
                }
            } else {
                LOGGER.warning("Attempted to end ride with ID: " + rideId + ", but it was not found");
                return Future.failedFuture(new RideNotFoundException());
            }
        });
    }

    @Override
    public Future<Integer> getNumberOfOngoingRides() {
        return rideRepository.getNumberOfOnGoingRides();
    }
}
