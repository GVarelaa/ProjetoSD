package SharedState;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.NotificationsDisabledException;
import Exceptions.UsernameAlreadyExistsException;

import java.util.List;

public interface IScooterManager {
    /**
     * Register a user in the server
     * @param username username of the user
     * @param password password of the user
     * @throws UsernameAlreadyExistsException error if the username is already registered
     */
    void register(String username, String password) throws UsernameAlreadyExistsException;

    /**
     * Login a user in the server
     * @param username username of the user
     * @param password password of the user
     * @return if the user is logged in or not
     * @throws NonExistentUsernameException error if the username doesn't exist in the users collection
     */
    boolean login(String username, String password) throws NonExistentUsernameException;

    /**
     * List the available scooters in a radius D (pre-configured) of p
     * @param p center of radius where free scooters will be checked
     * @return a list of the positions of the free scooters
     */
    List<Position> listFreeScooters(Position p);

    /**
     * List the available rewards in a radius D (pre-configured) of p
     * @param p center of radius where rewards will be checked
     * @return a list of the positions of the rewards available
     */
    List<List<Position>> listRewards(Position p);

    /**
     * Tries to activate a scooter the closest to a given position, limited by a radius D (pre-configured)
     * @param p center of radius where free scooters will be checked
     * @param username username of the client who activates the scooter
     * @return a reservation containing a reservation code and the position of the scooter found
     * @throws NoScootersAvailableException error if there are no available scooters
     */
    Reservation activateScooter(Position p, String username) throws NoScootersAvailableException;

    /**
     * Parks a scooter given the reservation code and the final position of the ride
     * (A ride can be a reward)
     * @param reservationId reservation code
     * @param parkingPosition final position of the scooter
     * @return the cost of the ride or the reward (if applicable)
     */
    double parkScooter(int reservationId, Position parkingPosition);

    /**
     * Check if there are rewards on the radius of a given position and waits if there are no rewards
     * @param p Position
     * @return List of rewards on the radius of a given position
     */
    List<Reward> userNotifications(String username, Position p, List<Reward> oldRewards) throws NotificationsDisabledException;

    /**
     * Changes the notifications state on a user
     * @param username Username
     * @param notificationsState New notifications state
     */
    void changeNotificationsState(String username, boolean notificationsState);
}
