package hexagonal.adapters.impl;

import com.google.gson.Gson;
import hexagonal.adapters.IUserSerializer;
import hexagonal.domain.entities.User;

public class UserSerializerImpl implements IUserSerializer {
    private final Gson gson = new Gson();

    @Override
    public String serialize(User user) {
        return gson.toJson(user);
    }

    @Override
    public User deserialize(String userData) {
        return gson.fromJson(userData, User.class);
    }
}
