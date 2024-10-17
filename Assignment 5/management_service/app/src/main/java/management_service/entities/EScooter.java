package management_service.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Document(collection = "e-scooters")
public class EScooter {

    @Id
    private final String id;
    private String name;
    private String location;
    private String state;

    public EScooter(String location, String state, String name, String id) {
        this.location = location;
        this.state = state;
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    public String getState() {
        return this.state;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EScooter eScooter = (EScooter) o;
        return Objects.equals(this.id, eScooter.id) && Objects.equals(this.name, eScooter.name) && Objects.equals(this.location, eScooter.location) && Objects.equals(this.state, eScooter.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.name, this.location, this.state);
    }
}
