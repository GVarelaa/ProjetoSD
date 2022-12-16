package SharedState;

import java.time.LocalDateTime;

// TODO: Add initial position -> check activateScooter method
public class Reservation {

    private int reservationID;
    private LocalDateTime timestamp;
    private String username;

    public Reservation(int id, LocalDateTime timestamp, String username) {
        this.reservationID = id;
        this.timestamp = timestamp;
        this.username = username;
    }

    public int getReservationID() {
        return this.reservationID;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getUsername() {
        return this.username;
    }

    public void setReservationID(int id) {
        this.reservationID = id;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
