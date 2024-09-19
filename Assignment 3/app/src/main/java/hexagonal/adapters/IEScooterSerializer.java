package hexagonal.adapters;

import hexagonal.domain.entities.EScooter;

public interface IEScooterSerializer {
    String serialize(EScooter escooter);
    EScooter deserialize(String escooterData);
}
