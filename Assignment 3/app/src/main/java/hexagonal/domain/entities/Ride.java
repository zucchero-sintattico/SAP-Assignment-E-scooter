package hexagonal.domain.entities;

import java.util.Date;
import java.util.Optional;

public class Ride {
    private Date startedDate;
    private Optional<Date> endDate;
    private User user;
    private EScooter scooter;
    private boolean ongoing;
    private String id;
    public Ride(String id, User user, EScooter scooter) {
        this.id = id;
        this.startedDate = new Date();
        this.endDate = Optional.empty();
        this.user = user;
        this.scooter = scooter;
        ongoing = true;
    }

    public String getId() {
        return id;
    }

    public void end() {
        endDate = Optional.of(new Date());
        ongoing = false;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public boolean isOngoing() {
        return this.ongoing;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public Optional<Date> getEndDate() {
        return endDate;
    }

    public void setEndDate(Optional<Date> endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public EScooter getEScooter() {
        return scooter;
    }
}