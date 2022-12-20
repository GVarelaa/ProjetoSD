package SharedState;

import Exceptions.NoScootersAvailableException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScooterManager {
    private final static int D = 2;
    private final static int N = 20; // dimensão do mapa
    private Set<Scooter> scooters;
    private List<Reward> rewards;
    private Set<Reservation> reservations;
    //private ReentrantReadWriteLock lockScooters;
    private ReentrantLock lockScooters; // Simplificar pra já
    private ReentrantLock lockRewards;
    private ReentrantLock lockReservations;


    /**
     * Instantiates scooters map and collection lock
     */
    public ScooterManager(){
        this.scooters = new HashSet<>();
        this.rewards = new ArrayList<>();
        this.reservations = new HashSet<>();
        this.lockScooters = new ReentrantLock();
        //this.lockScooters = new ReentrantReadWriteLock();
        this.lockRewards = new ReentrantLock();
        this.lockReservations = new ReentrantLock();
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

        try{
            this.lockScooters.lock();

            for(Scooter scooter: scooters){ // Iterate over scooters set
                if(scooter.getIsFree()){
                    Position scooterPosition = scooter.getPosition();
                    if(scooterPosition.inRadius(p, D)){ // If scooterPosition in radius D of p
                        freeScooters.add(scooterPosition.clone());
                    }
                }
            }

            return freeScooters;
        }
        finally {
            this.lockScooters.unlock();
        }
    }

    /**
     * List the rewards in a radius D (pre-configured) of p
     * @param p center of radius where rewards will be checked
     * @return a list of the positions of the rewards
     */
    public List<Position> listRewards(Position p) {
        List<Position> rewards = new ArrayList<>();

        try {
            this.lockRewards.lock();

            for (Reward reward : this.rewards) {
                Position rewardPosition = reward.getInitialPosition(); // inicial ou final ?
                if (rewardPosition.inRadius(p, D)) {
                    rewards.add(rewardPosition.clone());
                }
            }

            return rewards;
        }
        finally {
            this.lockRewards.unlock();
        }
    }

    /**
     * Tries to activate a scooter the closest to a given position, limited by a radius D (pre-configured)
     * @param p center of radius where free scooters will be checked
     * @param username username of the client who activates the scooter
     * @return a reservation containing a reservation code and the position of the scooter found
     * @throws NoScootersAvailableException error if there are no available scooters
     */
    public Reservation activateScooter(Position p, String username) throws NoScootersAvailableException {
        List<Position> freeScooters = new ArrayList<>();
        Scooter nearScooter = null;

        try{
            this.lockScooters.lock();

            for (Scooter scooter: scooters) { // Iterate over scooters set
                if (scooter.getIsFree()) {
                    Position scooterPosition = scooter.getPosition();

                    if (scooterPosition.inRadius(p, D)) { // If scooterPosition in radius D of p
                        if (nearScooter == null) nearScooter = scooter;

                        if (scooterPosition.distanceTo(p) < (nearScooter.getPosition().distanceTo(p))) {
                            nearScooter = scooter;
                        }
                    }
                }
            }
            this.lockReservations.lock();
        }
        finally {
            this.lockScooters.unlock();
        }

        try {
            if (nearScooter == null) {
                throw new NoScootersAvailableException("There are no available scooters in a radius " + D + " of " + p.toString() + "!");
            }

            Reservation r = new Reservation(nearScooter.getPosition(), LocalDateTime.now(), username);
            this.reservations.add(r);
            return r; // clone???
        }
        finally {
            this.lockReservations.unlock();
        }
    }

    /**
     * Daemon that evaluates current scooter distribution and tries to optimize it, generating rewards
     */
    public void generateRewards(){
        while (true){ // TODO alterar para condition
            // await

            char matrix[][] = new char [N][N]; // For testing currently, should be an instance variable
            List<Position> overcrowdedPositions = new ArrayList<Position>();
            List<Position> freePositions = new ArrayList<Position>();

            for(int i=0; i<N; i++){ // Iterate the map looking for overcrowded positions and free positions
                for(int j=0; j<N; j++){
                    int positionState = this.evaluatePosition(i, j, matrix);
                    if (positionState == 0){
                        freePositions.add(new Position(j, i));
                    }
                    else if (positionState > 1){
                        overcrowdedPositions.add(new Position(i, j));
                    }
                }
            }

            Reward newReward = null;
            // Generate one reward, if possible
            if (overcrowdedPositions.size() > 1 && freePositions.size() > 1){
                newReward = new Reward(overcrowdedPositions.get(0), freePositions.get(0), 2); // 2 is hard-coded here
            }

            this.rewards.add(newReward); // Needs locking
        }
    }

    /**
     * Calculate the right or bottom limit of square radius
     * @param num number to evaluate
     * @return the limit
     */
    public static int getBorderLeft(int num){
        int before;

        if (num > D-1){
            before = num-D+1;
        }
        else before = 0;
        return before;
    }

    /**
     * Calculate the left or upper limit of square radius
     * @param num number to evaluate
     * @return the limit
     */
    public static int getBorderRight(int num){
        int after;

        if (num < N-D+1){
            after = num+D-1;
        }
        else after = N-1;
        return after;
    }

    /**
     * Counts the number of scooters in a radius D of (columnNum, lineNum)
     * @param lineNum number of line in matrix
     * @param columnNum number of column in matrix
     * @param matrix matrix with the state
     * @return the number of scooters found
     */
    public int evaluatePosition(int lineNum, int columnNum, char [][] matrix){
        int up, down, left, right;
        up = getBorderLeft(lineNum);
        down = getBorderRight(lineNum);
        left = getBorderLeft(columnNum);
        right = getBorderRight(columnNum);

        int count = 0;

        for(int i=up; i<=down; i++){
            for(int j=left; j<=right; j++){
                if (matrix[i][j] == 'X'){
                    count++;
                }
            }
        }

        return count;
    }
}
