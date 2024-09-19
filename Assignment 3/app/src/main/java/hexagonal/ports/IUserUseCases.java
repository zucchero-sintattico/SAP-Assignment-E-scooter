package hexagonal.ports;

import hexagonal.ports.exceptions.UserIdAlreadyExistingException;
import hexagonal.ports.exceptions.UserNotFoundException;

public interface IUserUseCases {
    void registerNewUser(String id, String name, String surname) throws UserIdAlreadyExistingException;
    String getUserInfo(String id) throws UserNotFoundException;
}
