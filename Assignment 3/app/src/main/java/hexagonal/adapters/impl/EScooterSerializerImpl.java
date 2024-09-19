package hexagonal.adapters.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hexagonal.adapters.IEScooterSerializer;
import hexagonal.domain.entities.EScooter;
import hexagonal.domain.entities.EScooter.EScooterState;
import hexagonal.domain.entities.Location;

public class EScooterSerializerImpl implements IEScooterSerializer {
    private final Gson gson = new Gson();

    @Override
    public String serialize(EScooter escooter) {
        JsonObject scooterObj = new JsonObject();
        scooterObj.addProperty("id", escooter.getId());
        scooterObj.addProperty("state", escooter.getState().toString());
        escooter.getCurrentLocation().ifPresent(location -> {
            JsonObject locObj = new JsonObject();
            locObj.addProperty("latitude", location.getLatitude());
            locObj.addProperty("longitude", location.getLongitude());
            scooterObj.add("location", locObj);
        });
        return scooterObj.toString();
    }

    @Override
    public EScooter deserialize(String escooterData) {
        JsonObject escooterJson = gson.fromJson(escooterData, JsonObject.class);
        String id = escooterJson.get("id").getAsString();
        EScooter eScooter = new EScooter(id);
        eScooter.setState(EScooterState.valueOf(escooterJson.get("state").getAsString()));
        JsonObject locObj = null;
        if (escooterJson.has("location") && !escooterJson.get("location").isJsonNull()) {
            locObj = escooterJson.get("location").getAsJsonObject();
        }
        if (locObj != null) {
            eScooter.setLocation(
                    new Location(locObj.get("latitude").getAsDouble(), locObj.get("longitude").getAsDouble()));
        }
        return eScooter;
    }
}
