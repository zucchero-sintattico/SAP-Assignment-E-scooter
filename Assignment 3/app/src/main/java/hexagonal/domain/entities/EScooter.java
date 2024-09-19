package hexagonal.domain.entities;

import java.util.Objects;
import java.util.Optional;

public class EScooter {

    private final String id;

    public enum EScooterState {
        AVAILABLE, IN_USE, MAINTENANCE
    }

    private EScooterState state;
    private Optional<Location> loc;

    public EScooter(String id) {
        this.id = id;
        this.state = EScooterState.AVAILABLE;
        this.loc = Optional.empty();
    }

    public String getId() {
        return id;
    }

    public EScooterState getState() {
        return state;
    }

    public boolean isAvailable() {
        return state.equals(EScooterState.AVAILABLE);
    }

    public void setState(EScooterState state) {
        this.state = state;
    }

    public void setLocation(Location newLoc) {
        loc = Optional.of(newLoc);
    }

    public Optional<Location> getCurrentLocation() {
        return loc;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EScooter other = (EScooter) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, state, loc);
    }
}
