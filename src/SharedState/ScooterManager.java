package SharedState;

import Exceptions.NoScootersAvailableException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScooterManager {
    private final static int D = 2;
    private final static int N = 20; // dimensão do mapa
    private final static int S = 10; // número de scooters fixo,
    private Scooter[] scooters; // coleção estática
    private List<Reward> rewards;
    private Map<Integer, Reservation> reservations;

    private ReentrantLock lockScooters;

    private Condition cond;
    private ReentrantLock lockRewards;
    private ReentrantLock lockReservations;


    /**
     * Instantiates scooters map and collection lock
     */
    public ScooterManager(){
        this.scooters = new Scooter[S];
        this.rewards = new ArrayList<>();
        this.reservations = new HashMap<>();
        this.lockRewards = new ReentrantLock();
        this.lockReservations = new ReentrantLock();

        this.lockScooters = new ReentrantLock();
        this.cond = lockScooters.newCondition();

        this.randomizeScooterPositions();

        new Thread(() -> {
            this.generateRewards();
        }).start();
    }

    /**
     * Randomizes the scooters distribution in the map
     */
    private void randomizeScooterPositions(){
        Random random = new Random();
        for(int i=0; i<S; i++){
            int x = random.nextInt(N);
            int y = random.nextInt(N);
            this.scooters[i] = new Scooter(new Position(x, y));
        }
    }

    public void printScooters(){
        String rep = "";
        int matrix [][] = this.convertToMatrix();

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                rep += matrix[i][j] + " ";
            }
            rep += "\n";
        }

        System.out.println(rep);
    }

    private int [][] convertToMatrix(){
        int [][] mat = new int[N][N];

        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                mat[i][j] = 0;
            }
        }

        for (Scooter s: this.scooters){
            Position p = s.getPosition();
            mat[p.getY()][p.getX()]++;
        }

        return mat;
    }

    /**
     * List the available scooters in a radius D (pre-configured) of p
     * @param p center of radius where scooters will be checked
     * @return a list of the positions of the free scooters
     */
    public List<Position> listFreeScooters(Position p){
        List<Position> freeScooters = new ArrayList<>();

        for(Scooter scooter: scooters){ // Iterate over scooters set
            scooter.lockScooter.lock();

            if(scooter.getIsFree()){
                Position scooterPosition = scooter.getPosition();
                if(scooterPosition.inRadius(p, D)){ // If scooterPosition in radius D of p
                    freeScooters.add(scooterPosition.clone());
                }
            }
        }

        for(Scooter scooter : scooters){
            scooter.lockScooter.unlock();
        }

        return freeScooters;
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
        Scooter nearScooter = null;

        for (Scooter scooter: this.scooters) { // Iterate over scooters set
            scooter.lockScooter.lock();

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

        for (Scooter scooter : this.scooters){
            if(scooter != nearScooter){ // Mudar para equals
                scooter.lockScooter.unlock();
            }
        }

        try {
            this.lockReservations.lock();
            if (nearScooter == null) {
                throw new NoScootersAvailableException("There are no available scooters in a radius " + D + " of " + p.toString() + "!");
            }
            //this.cond.signal();
            nearScooter.setIsFree(false);
            Reservation r = new Reservation(nearScooter, username);
            this.reservations.put(r.getReservationID(), r);
            return r; // clone???
        }
        finally {
            nearScooter.lockScooter.unlock();
            this.lockReservations.unlock();
        }
    }


    /**
     * Calculates the cost of a reservation given the distance and duration
     * @param distance covered distance
     * @param duration duration of the reservation
     * @return the cost of the reservation
     */
    public static double calculateCost(double distance, double duration){
        double cost = 0;

        cost = 0.15 * duration;// 15 centimos por minuto

        return Math.round(cost * 100) / 100.0;  // Arredondar a 2 casas decimais
    }

    /**
     * Parks a scooter given the reservation code and the final position of the ride
     * (A ride can be a reward)
     * @param reservationID reservation code
     * @param parkingPosition final position of the scooter
     * @return the cost of the ride or the reward (if applicable)
     */
    public double parkScooter(int reservationID, Position parkingPosition){
        Scooter scooter = null;
        Reservation reservation = null;
        LocalDateTime parkTimestamp = LocalDateTime.now();
        try{
            this.lockReservations.lock();

            reservation = this.reservations.get(reservationID);
            scooter = reservation.getScooter();
            scooter.lockScooter.lock();

            this.reservations.remove(reservationID); // removemos do mapa?
        }
        finally {
            this.lockReservations.unlock();;
        }

        try{
            Position initialPosition = reservation.getInitialPosition();
            double distance = initialPosition.distanceTo(parkingPosition);
            double duration = ChronoUnit.MINUTES.between(parkTimestamp, reservation.getTimestamp()); // Segundos

            double cost = ScooterManager.calculateCost(distance, duration);

            scooter.setPosition(parkingPosition);
            scooter.setIsFree(true);

            return cost;
        }
        finally {
            scooter.lockScooter.unlock();
        }
    }



    /**
     * Daemon that evaluates current scooter distribution and tries to optimize it, generating rewards
     */
    public void generateRewards(){
        List<Position> overcrowdedPositions = new ArrayList<Position>();
        List<Position> freePositions = new ArrayList<Position>();

        while (true){
            System.out.println("Olá");
            lockScooters.lock();

            while (overcrowdedPositions.size() == 0 || freePositions.size() == 0){
                try {
                    cond.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                checkForRewards(overcrowdedPositions, freePositions);
            }

            Reward newReward = null;
            // Generate one reward, if possible
            if (overcrowdedPositions.size() > 1 && freePositions.size() > 1){
                newReward = new Reward(overcrowdedPositions.get(0), freePositions.get(0), 2); // 2 is hard-coded here
            }

            this.rewards.add(newReward); // Needs locking
        }
    }

    private void checkForRewards(List<Position> overcrowdedPositions, List<Position> freePositions){
        char matrix[][] = new char [N][N]; // For testing currently, should be an instance variable


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
    }

    /**
     * Calculate the right or bottom limit of square radius
     * @param num number to evaluate
     * @return the limit
     */
    private static int getBorderLeft(int num){
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
