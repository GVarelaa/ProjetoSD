package SharedState;

import java.time.LocalDateTime;

// TODO: Add initial position -> check activateScooter method
public class Reservation {

    private int reservationID;
    private Position initialPosition;
    private LocalDateTime timestamp;
    private String username;
    private static int idCount = 0;

    public Reservation(Position initialPosition, LocalDateTime timestamp, String username) {
        this.reservationID = idCount++;
        this.timestamp = timestamp;
        this.username = username;
    }

    public int getReservationID() {
        return this.reservationID;
    }

    public Position getInitialPosition(){
        return this.initialPosition.clone();
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getUsername() {
        return this.username;
    }

    public void setInitialPosition(Position initialPosition){
        this.initialPosition = initialPosition.clone();
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
