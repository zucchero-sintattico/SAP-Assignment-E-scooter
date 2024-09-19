package hexagonal.domain.repositories;

import hexagonal.domain.entities.User;

import java.util.Optional;

public interface IUserRepository {
    void save(User user);
    Optional<User> findUserById(String id);
}
