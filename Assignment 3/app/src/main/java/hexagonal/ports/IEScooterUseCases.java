package hexagonal.ports;

import hexagonal.ports.exceptions.EScooterNotFoundException;

public interface IEScooterUseCases {
    void registerNewEScooter(String id);
    String getEScooterInfo(String id) throws EScooterNotFoundException;
}
