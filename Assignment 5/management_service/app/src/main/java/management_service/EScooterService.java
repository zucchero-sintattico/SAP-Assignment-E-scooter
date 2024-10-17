package management_service;

import management_service.entities.EScooter;
import management_service.repositories.EScooterRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EScooterService {

    private final EScooterRepo eScooterRepo;

    public EScooterService(EScooterRepo eScooterRepo) {
        this.eScooterRepo = eScooterRepo;
    }

    public EScooter createEScooter(EScooter eScooter) {
        return eScooterRepo.save(eScooter);
    }

    public List<EScooter> getEScooters() {
        return eScooterRepo.findAll();
    }

    public EScooter getScooterState(String id) {
        return eScooterRepo.findById(id).orElse(null);
    }

    public List<EScooter> getAvailableScooters() {
        return eScooterRepo.findAll().stream()
                .filter(escooter -> "ready".equals(escooter.getState()))
                .collect(Collectors.toList());
    }

    public EScooter setScooterState(String scooterId, EScooter updatedScooter) {
        Optional<EScooter> scooterOptional = eScooterRepo.findById(scooterId);
        if (scooterOptional.isPresent()) {
            EScooter existingScooter = scooterOptional.get();
            existingScooter.setState(updatedScooter.getState());
            return eScooterRepo.save(existingScooter);
        } else {
            return null;
        }
    }

    public EScooter useScooter(String scooterId) {
        Optional<EScooter> scooterOptional = eScooterRepo.findById(scooterId);
        if (scooterOptional.isPresent()) {
            EScooter scooter = scooterOptional.get();
            if ("ready".equals(scooter.getState())) {
                scooter.setState("in use");
                return eScooterRepo.save(scooter);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
