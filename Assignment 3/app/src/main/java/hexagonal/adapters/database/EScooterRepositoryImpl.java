package hexagonal.adapters.database;

import hexagonal.domain.repositories.IEScooterRepository;
import io.vertx.core.json.JsonObject;
import hexagonal.adapters.IEScooterSerializer;
import hexagonal.domain.entities.EScooter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Optional;

import java.nio.file.Files;
import java.nio.file.Paths;

public class EScooterRepositoryImpl implements IEScooterRepository {
    private static final String ESCOOTERS_PATH = "escooters";
    private String dbaseFolder = "resources";
    private IEScooterSerializer escooterSerializer;

    public EScooterRepositoryImpl(String dbaseFolder, IEScooterSerializer escooterSerializer) {
        this.dbaseFolder = dbaseFolder;
        this.escooterSerializer = escooterSerializer;
        makeDir(dbaseFolder);
        makeDir(dbaseFolder + File.separator + ESCOOTERS_PATH);
    }

    @Override
    public void save(EScooter escooter) {
        JsonObject escooterJson = new JsonObject(escooterSerializer.serialize(escooter));
        // Print the json that is going to be saved
        System.out.println("EScoterRepositoryImpl: ");
        System.out.println("Saving the following json: ");
        System.out.println(escooterJson);
        saveObj(ESCOOTERS_PATH, escooterJson.getString("id"), escooterJson);
    }

    @Override
    public Optional<EScooter> findEScooterById(String id) {
        try {
            String path = dbaseFolder + File.separator + ESCOOTERS_PATH + File.separator + id + ".json";
            if (!Files.exists(Paths.get(path))) {
                return Optional.empty();
            }
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JsonObject escooterJson = new JsonObject(content);
            EScooter escooter = escooterSerializer.deserialize(escooterJson.encode());
            return Optional.of(escooter);
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    private void saveObj(String db, String id, JsonObject obj) {
        try {
            FileWriter fw = new FileWriter(dbaseFolder + File.separator + db + File.separator + id + ".json");
            BufferedWriter wr = new BufferedWriter(fw);
            wr.write(obj.encodePrettily());
            wr.flush();
            fw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void makeDir(String name) {
        try {
            File dir = new File(name);
            if (!dir.exists()) {
                dir.mkdir();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
