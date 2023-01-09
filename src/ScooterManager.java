import SharedState.Position;
import SharedState.Reservation;

import java.io.IOException;
import java.util.List;

public interface ScooterManager {

    /**
     * Closes the connection
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * Sends to the multiplexer a register request
     * Waits for the server response
     * @param username username
     * @param password password
     * @return confirmation
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean register(String username, String password);

    /**
     * Sends to the multiplexer a login request
     * Waits for the server response
     * @param username username
     * @param password password
     * @return confirmation
     * @throws IOException
     * @throws InterruptedException
     */
    public boolean login(String username, String password);

    /**
     * Sends to the multiplexer a request for free scooters near p
     * Waits for the response of the server
     * @param p the position sent
     * @return a list of nearby scooter positions
     * @throws IOException
     * @throws InterruptedException
     */
    public List<Position> listFreeScooters(Position p);

    /**
     * Sends to the multiplexer a request for listing rewards near p
     * Waits for the response of the server
     * @param p the position sent
     * @return a list of pairs (origin - destination)
     * @throws IOException
     * @throws InterruptedException
     */
    public List<List<Position>> listRewards(Position p);

    /**
     * Sends to the multiplexer a request for activating a scooter the closest to p
     * Waits for the response of the server
     * @param p the position sent
     * @return a reservation (id and initial position)
     * @throws IOException
     * @throws InterruptedException
     */
    public Reservation activateScooter(Position p);

    /**
     * Sends to the multiplexer a request for parking a scooter at p
     * Waits for the response of the server
     * @param p the position sent
     * @param codReservation the code of the reservation
     * @return an int (if > 0, the reward, if < 0, the cost)
     * @throws IOException
     * @throws InterruptedException
     */
    public double parkScooter(Position p, int codReservation);

    /**
     * Turns on or off notifications for rewards near a given position
     * @param onOff false - Off, true - On
     * @param p given position or null if onOff == false
     */
    public void turnOnOffNotifications(boolean onOff, Position p);

    /**
     * Method that waits for notifications from the server regarding rewards near a position specified before
     */
    public void waitForNotifications();
}
