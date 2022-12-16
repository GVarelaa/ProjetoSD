package SharedState;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistantUsernameException;
import Exceptions.UsernameAlreadyExistsException;

import java.util.List;
import java.util.Map;

/**
 * Shared state of the application
 * Supports concurrency
 */
public class SharedState implements ISharedState {
    private UserManager um;
    private ScooterManager sm;

    /**
     * Register a user in the server
     * @param username username of the user
     * @param password password of the user
     * @throws UsernameAlreadyExistsException error if the username is already registered
     */
    @Override
    public void register(String username, String password) throws UsernameAlreadyExistsException {
        um.register(username, password);
    }

    /**
     * Login a user in the server
     * @param username username of the user
     * @param password password of the user
     * @return if the user is logged in or not
     * @throws NonExistantUsernameException error if the username doesn't exist in the users collection
     */
    @Override
    public boolean login(String username, String password) throws NonExistantUsernameException {
        return um.login(username, password);
    }

    /**
     * List the available scooters in a radius D (pre-configured) of p
     * @param p center of radius where scooters will be checked
     * @return a list of the positions of the free scooters
     */
    @Override
    public List<Position> listFreeScooters(Position p){
        return sm.listFreeScooters(p);
    }

    /**
     * List the available rewards in a radius D (pre-configured) of p
     * @param p center of radius where rewards will be checked
     * @return a list of the rewards available
     */
    @Override
    public List<Reward> listRewards(Position p){
        return null;
    }

    /**
     * Tries to activate a scooter the closest to a given position, limited by a radius D (pre-configured)
     * @param p center of radius where free scooters will be checked
     * @param username username of the client who activates the scooter
     * @return a reservation containing a reservation code and the position of the scooter found
     * @throws NoScootersAvailableException error if there are no available scooters
     */
    @Override
    public Reservation activateScooter(Position p, String username) throws NoScootersAvailableException {
        return sm.activateScooter(p, username);
    }

    /**
     * Parks a scooter given the reservation code and the final position of the ride
     * (A ride can be a reward)
     * @param reservationId reservation code
     * @param parkingPosition final position of the scooter
     * @return the cost of the ride or the reward (if applicable)
     */
    @Override
    public int parkScooter(int reservationId, Position parkingPosition) {
        return 0;
    }


    /**
     * Sets a client (identified by username) available to receive notifications for when rewards appear in a radius D (pre-configured) of a given position
     * Rewards will be sent later
     * @param username username of the client
     * @param p center of radius where notifications of rewards may be applicable
     */
    @Override
    public void askForNotifications(String username, Position p) {

    }

    /**
     * Cancel notifications for a given client
     * @param username username of the client
     */
    @Override
    public void cancelNotifications(String username) {

    }




}
