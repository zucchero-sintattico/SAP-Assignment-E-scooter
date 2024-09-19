package hexagonal.domain.services;

import hexagonal.adapters.IEScooterSerializer;
import hexagonal.domain.entities.EScooter;
import hexagonal.domain.entities.Location;
import hexagonal.domain.repositories.IEScooterRepository;
import hexagonal.ports.IEScooterUseCases;
import hexagonal.ports.exceptions.EScooterNotFoundException;

import java.util.Optional;

public class EScooterService implements IEScooterUseCases {
    private final IEScooterRepository escooterRepository;
    private final IEScooterSerializer escooterSerializer;

    public EScooterService(IEScooterRepository escooterRepository, IEScooterSerializer escooterSerializer) {
        this.escooterRepository = escooterRepository;
        this.escooterSerializer = escooterSerializer;
    }

    @Override
    public void registerNewEScooter(String id) {
        EScooter escooter = new EScooter(id);
        escooterRepository.save(escooter);
    }

    public void updateEScooterState(String id, EScooter.EScooterState state) {
        Optional<EScooter> escooter = escooterRepository.findEScooterById(id);
        if (escooter.isPresent()) {
            escooter.get().setState(state);
            escooterRepository.save(escooter.get());
        } else {
            throw new RuntimeException("EScooter not found");
        }
    }

    public void updateEScooterLocation(String id, Location newLoc) {
        Optional<EScooter> escooter = escooterRepository.findEScooterById(id);
        if (escooter.isPresent()) {
            escooter.get().setLocation(newLoc);
            escooterRepository.save(escooter.get());
        } else {
            throw new RuntimeException("EScooter not found");
        }
    }

    public boolean escooterExists(String escooterId) {
        return escooterRepository.findEScooterById(escooterId).isPresent();
    }

    @Override
    public String getEScooterInfo(String id) throws EScooterNotFoundException {
        Optional<EScooter> escooter = escooterRepository.findEScooterById(id);
        if (escooter.isPresent()) {
            return escooterSerializer.serialize(escooter.get());
        } else {
            throw new EScooterNotFoundException();
        }
    }
}
