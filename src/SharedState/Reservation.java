package SharedState;

import java.time.LocalDateTime;

// TODO: Add initial position -> check activateScooter method
public class Reservation {

    private int reservationID;
    private Scooter scooter;
    private Position initialPosition;
    private LocalDateTime timestamp;
    private String username;
    private static int idCount = 0;

    public Reservation(Scooter scooter, String username) {
        this.reservationID = idCount++;
        this.scooter = scooter;
        this.initialPosition = scooter.getPosition();
        this.timestamp = LocalDateTime.now();
        this.username = username;
    }

    public Reservation(int codReservation, Position p) { // Para o cliente poder receber reservas (talvez mude depois)
        this.reservationID = codReservation;
        this.initialPosition = p;
    }

    public int getReservationID() {
        return this.reservationID;
    }

    public Scooter getScooter(){
        return this.scooter;
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

    /**
     * Get the string representation of a reservation
     * @return a reservation in the form of a string
     */
    public String toString(){
        return this.reservationID + " , " + this.initialPosition + " , " + this.username + " , " + this.timestamp + "\n";
    }
}
