package hexagonal.domain.services;

import hexagonal.adapters.IUserSerializer;
import hexagonal.domain.entities.User;
import hexagonal.domain.repositories.IUserRepository;
import hexagonal.ports.IUserUseCases;
import hexagonal.ports.exceptions.UserIdAlreadyExistingException;
import hexagonal.ports.exceptions.UserNotFoundException;

import java.util.Optional;

public class UserService implements IUserUseCases {
    private final IUserRepository userRepository;
    private final IUserSerializer userSerializer;

    public UserService(IUserRepository userRepository, IUserSerializer userSerializer) {
        this.userRepository = userRepository;
        this.userSerializer = userSerializer;
    }

    @Override
    public void registerNewUser(String id, String name, String surname) throws UserIdAlreadyExistingException {
        Optional<User> user = userRepository.findUserById(id);
        if (user.isEmpty()) {
            User newUser = new User(id, name, surname);
            userRepository.save(newUser); // Save the user using the userRepository
        } else {
            throw new UserIdAlreadyExistingException();
        }
    }

    @Override
    public String getUserInfo(String id) throws UserNotFoundException {
        Optional<User> user = userRepository.findUserById(id);
        if (user.isPresent()) {
            return userSerializer.serialize(user.get());
        } else {
            throw new UserNotFoundException();
        }
    }

    public boolean userExists(String userId) {
        return userRepository.findUserById(userId).isPresent();
    }
}
