package SharedState;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistantUsernameException;
import Exceptions.UsernameAlreadyExistsException;

import java.util.List;

public interface ISharedState {
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
     * @throws NonExistantUsernameException error if the username doesn't exist in the users collection
     */
    boolean login(String username, String password) throws NonExistantUsernameException;

    /**
     * List the available scooters in a radius D (pre-configured) of p
     * @param p center of radius where free scooters will be checked
     * @return a list of the positions of the free scooters
     */
    List<Position> listFreeScooters(Position p);

    /**
     * List the available rewards in a radius D (pre-configured) of p
     * @param p center of radius where rewards will be checked
     * @return a list of the rewards available
     */
    List<Reward> listRewards(Position p);

    /**
     * Tries to activate a scooter the closest to a given position, limited by a radius D (pre-configured)
     * @param p center of radius where free scooters will be checked
     * @return a reservation containing a reservation code and the position of the scooter found
     * @throws NoScootersAvailableException error if there are no available scooters
     */
    Reservation activateScooter(Position p) throws NoScootersAvailableException;

    /**
     * Parks a scooter given the reservation code and the final position of the ride
     * (A ride can be a reward)
     * @param reservationId reservation code
     * @param parkingPosition final position of the scooter
     * @return the cost of the ride or the reward (if applicable)
     */
    int parkScooter(int reservationId, Position parkingPosition);

    /**
     * Sets a client (identified by username) available to receive notifications for when rewards appear in a radius D (pre-configured) of a given position
     * Rewards will be sent later
     * @param username username of the client
     * @param p center of radius where notifications of rewards may be applicable
     */
    void askForNotifications(String username, Position p);

    /**
     * Cancel notifications for a given client
     * @param username username of the client
     */
    void cancelNotifications(String username);

}
