package hexagonal.launcher;

import hexagonal.adapters.IEScooterSerializer;
import hexagonal.adapters.IRideSerializer;
import hexagonal.adapters.IUserSerializer;
import hexagonal.adapters.database.EScooterRepositoryImpl;
import hexagonal.adapters.database.MongoRepository;
import hexagonal.adapters.database.UserRepositoryImpl;
import hexagonal.adapters.impl.EScooterSerializerImpl;
import hexagonal.adapters.impl.RideSerializerImpl;
import hexagonal.adapters.impl.UserSerializerImpl;
import hexagonal.domain.repositories.IEScooterRepository;
import hexagonal.domain.repositories.IUserRepository;
import hexagonal.domain.services.EScooterService;
import hexagonal.domain.services.RideService;
import hexagonal.domain.services.UserService;
import hexagonal.ports.IPresentationPort;
import hexagonal.adapters.ui.PresentationAdapter;

public class EScooterManagementSystem {
    public static void main(String[] args) {
        String dbaseFolder = "dbase";

        // Initialize serializers
        IUserSerializer userSerializer = new UserSerializerImpl();
        IUserRepository userRepository = new UserRepositoryImpl(dbaseFolder, userSerializer);
        IEScooterSerializer escooterSerializer = new EScooterSerializerImpl();
        IEScooterRepository escooterRepository = new EScooterRepositoryImpl(dbaseFolder, escooterSerializer);
        IRideSerializer rideSerializer = new RideSerializerImpl(userRepository, escooterRepository);

        // Set up database folder
        MongoRepository db = new MongoRepository("mongodb://localhost:27017", "assignment_3", "rides", (RideSerializerImpl) rideSerializer);

        //Initialize services
        UserService userService = new UserService(userRepository, userSerializer);
        EScooterService escooterService = new EScooterService(escooterRepository, escooterSerializer);
        RideService rideService = new RideService(userRepository, escooterRepository, db, rideSerializer, escooterService);

        //Start presentation port
        IPresentationPort presentationPort = new PresentationAdapter( userService, escooterService, rideService, 8080);
        presentationPort.init();
    }
}
