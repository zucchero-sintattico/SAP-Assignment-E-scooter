package hexagonal.adapters;

import hexagonal.domain.entities.User;

public interface IUserSerializer {
    String serialize(User user);
    User deserialize(String userData);
}
