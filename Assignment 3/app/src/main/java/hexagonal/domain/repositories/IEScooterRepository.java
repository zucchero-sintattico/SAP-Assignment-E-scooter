package hexagonal.domain.repositories;

import hexagonal.domain.entities.EScooter;

import java.util.Optional;

public interface IEScooterRepository {
    void save(EScooter escooter);
    Optional<EScooter> findEScooterById(String id);
}
