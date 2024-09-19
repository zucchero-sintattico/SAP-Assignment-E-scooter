package hexagonal;

import hexagonal.domain.entities.EScooter;
import hexagonal.domain.services.EScooterService;
import org.junit.jupiter.api.Test;
import hexagonal.adapters.impl.EScooterSerializerImpl;
import hexagonal.adapters.IEScooterSerializer;
import hexagonal.adapters.database.EScooterRepositoryImpl;
import java.util.Optional;
import hexagonal.domain.repositories.IEScooterRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EScooterTest {

    @Test
    void createNewEScooter() {
        // Arrange
        String id = "escooter-20";
        IEScooterRepository escooterRepository = mock(IEScooterRepository.class);
        IEScooterSerializer escooterSerializer = mock(IEScooterSerializer.class);
        EScooterService eScooterService = new EScooterService(escooterRepository, escooterSerializer);

        when(escooterRepository.findEScooterById(id)).thenReturn(Optional.of(new EScooter(id)));

        // Act
        eScooterService.registerNewEScooter(id);

        // Assert
        assertTrue(eScooterService.escooterExists(id));
    }

    // saves a valid EScooter object
    @Test
    void saveValidEScooter() {
        // Arrange
        IEScooterRepository repository = new EScooterRepositoryImpl("dbase", new EScooterSerializerImpl());
        EScooter escooter = new EScooter("escooter-20");

        // Act
        repository.save(escooter);
        Optional<EScooter> savedEScooter = repository.findEScooterById("escooter-20");

        // Print
        System.out.println("Saved EScooter: " + savedEScooter);


        // Assert
        assertTrue(savedEScooter.isPresent());
        assertEquals(escooter, savedEScooter.get());
    }

}
