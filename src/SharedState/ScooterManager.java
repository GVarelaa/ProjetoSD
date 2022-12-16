package SharedState;

import Exceptions.NoScootersAvailableException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScooterManager {
    private final static int D = 2;
    private Set<Scooter> scooters;
    private ReentrantReadWriteLock lockScooters;

    /**
     * Instantiates scooters map and collection lock
     */
    public ScooterManager(){
        this.scooters = new HashSet<>();
        this.lockScooters = new ReentrantReadWriteLock();
    }

    /**
     * Adds a scooter to the set of scooters
     * @param s reference to the scooter to be added
     */
    public void addScooter(Scooter s){
        this.scooters.add(s.clone());
    }

    /**
     * List the available scooters in a radius D (pre-configured) of p
     * @param p center of radius where scooters will be checked
     * @return a list of the positions of the free scooters
     */
    public List<Position> listFreeScooters(Position p){
        List<Position> freeScooters = new ArrayList<>();
        // TODO: Acquire lock when other operations can cause inconsistency

        for(Scooter scooter: scooters){ // Iterate over scooters set
            Position scooterPosition = scooter.getPosition();
            if(scooterPosition.inRadius(p, D)){ // If scooterPosition in radius D of p
                freeScooters.add(scooterPosition.clone());
            }
        }

        return freeScooters;
    }

    /**
     * Tries to activate a scooter the closest to a given position, limited by a radius D (pre-configured)
     * @param p center of radius where free scooters will be checked
     * @param username username of the client who activates the scooter
     * @return a reservation containing a reservation code and the position of the scooter found
     * @throws NoScootersAvailableException error if there are no available scooters
     */
    public Reservation activateScooter(Position p, String username) throws NoScootersAvailableException {
        List<Position> freeScooters = listFreeScooters(p);

        if(freeScooters.size() > 0){
            Reservation r = new Reservation(freeScooters.get(0), LocalDateTime.now(), username);
            return r;
        }
        else throw new NoScootersAvailableException("There are no available scooters in a radius " + D + " of " + p.toString() + "!");
    }
}
