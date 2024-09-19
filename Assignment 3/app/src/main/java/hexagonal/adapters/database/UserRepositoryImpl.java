package hexagonal.adapters.database;

import hexagonal.adapters.IUserSerializer;
import hexagonal.domain.entities.User;
import hexagonal.domain.repositories.IUserRepository;
import io.vertx.core.json.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

public class UserRepositoryImpl implements IUserRepository {
    private static final String USERS_PATH = "users";
    private String dbaseFolder;
    private IUserSerializer userSerializer;

    public UserRepositoryImpl(String dbaseFolder, IUserSerializer userSerializer) {
        this.dbaseFolder = dbaseFolder;
        this.userSerializer = userSerializer;
        makeDir(dbaseFolder);
        makeDir(dbaseFolder + File.separator + USERS_PATH);
    }

    @Override
    public void save(User user) {
        JsonObject userJson = new JsonObject(userSerializer.serialize(user));
        saveObj(USERS_PATH, userJson.getString("id"), userJson);
    }

    @Override
    public Optional<User> findUserById(String id) {
        try {
            String path = dbaseFolder + File.separator + USERS_PATH + File.separator + id + ".json";
            if (!Files.exists(Paths.get(path))) {
                return Optional.empty();
            }
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JsonObject userJson = new JsonObject(content);
            User user = userSerializer.deserialize(userJson.encode());
            return Optional.of(user);
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
