package hexagonal.adapters.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hexagonal.adapters.IRideSerializer;
import hexagonal.domain.entities.EScooter;
import hexagonal.domain.entities.Ride;
import hexagonal.domain.entities.User;
import hexagonal.domain.repositories.IEScooterRepository;
import hexagonal.domain.repositories.IUserRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Locale;

public class RideSerializerImpl implements IRideSerializer {
    private final IUserRepository userRepository;
    private final IEScooterRepository eScooterRepository;
    private final Gson gson;

    public RideSerializerImpl(IUserRepository userRepository, IEScooterRepository eScooterRepository) {
        this.userRepository = userRepository;
        this.eScooterRepository = eScooterRepository;
        this.gson = new Gson();
    }

    @Override
    public String serialize(Ride ride) {
        JsonObject rideObj = new JsonObject();
        rideObj.addProperty("id", ride.getId());
        rideObj.addProperty("userId", ride.getUser().getId());
        rideObj.addProperty("onGoing", ride.isOngoing());
        rideObj.addProperty("escooterId", ride.getEScooter().getId());
        rideObj.addProperty("startDate", ride.getStartedDate().toString());
        Optional<Date> endDate = ride.getEndDate();

        if (endDate.isPresent()) {
            rideObj.addProperty("endDate", endDate.get().toString());
        } else {
            rideObj.add("location", null);
        }
        return rideObj.toString();
    }

    @Override
    public Ride deserialize(String rideData) {
        JsonObject rideJson = gson.fromJson(rideData, JsonObject.class);
        String id = rideJson.get("id").getAsString();
        String userId = rideJson.get("userId").getAsString();
        String escooterId = rideJson.get("escooterId").getAsString();

        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Date startDate = null;
        try {
            startDate = formatter.parse(rideJson.get("startDate").getAsString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Optional<Date> endDate = Optional.empty();
        if (rideJson.has("endDate")) {
            try {
                endDate = Optional.of(formatter.parse(rideJson.get("endDate").getAsString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // Assuming you have a User and EScooter repository to fetch the respective objects
        User user = userRepository.findUserById(userId).get();
        EScooter escooter = eScooterRepository.findEScooterById(escooterId).get();

        Ride ride = new Ride(id,user, escooter);
        ride.setStartedDate(startDate);
        ride.setEndDate(endDate);
        ride.setOngoing(rideJson.get("onGoing").getAsBoolean());

        return ride;
    }
}
