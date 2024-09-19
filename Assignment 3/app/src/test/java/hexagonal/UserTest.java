package hexagonal;

import hexagonal.adapters.IUserSerializer;
import hexagonal.adapters.database.UserRepositoryImpl;
import hexagonal.adapters.impl.UserSerializerImpl;
import hexagonal.domain.entities.User;
import hexagonal.domain.repositories.IUserRepository;
import hexagonal.domain.services.UserService;
import hexagonal.ports.exceptions.UserIdAlreadyExistingException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    @Test
    void createNewUser() throws UserIdAlreadyExistingException {
        String id = "1";
        String name = "John";
        String surname = "Doe";
        IUserRepository userRepository = mock(IUserRepository.class);
        IUserSerializer userSerializer = mock(IUserSerializer.class);
        UserService userService = new UserService(userRepository, userSerializer);

        when(userRepository.findUserById(id)).thenReturn(Optional.of(new User(id, name, surname)));

        // Act
        assertThrows(UserIdAlreadyExistingException.class, () -> {
            userService.registerNewUser(id, name, surname);
        });

        // Assert
        assertTrue(userService.userExists(id));

    }

    @Test
    void saveValidUser() {
        // Arrange
        IUserRepository userRepository = new UserRepositoryImpl("dbase", new UserSerializerImpl());
        User user = new User("1", "John", "Doe");

        userRepository.save(user);
        Optional<User> savedUser = userRepository.findUserById("1");

        // Print
        System.out.println("Saved User: " + savedUser);

        // Assert
        assertTrue(savedUser.isPresent());
        assertEquals(user, savedUser.get());
    }
}
