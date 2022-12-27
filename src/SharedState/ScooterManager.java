package SharedState;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.NotificationsDisabledException;
import Exceptions.UsernameAlreadyExistsException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScooterManager {
    private final static int D = 2;
    private final static int N = 10; // dimensão do mapa
    private final static int S = 15; // número de scooters fixo,
    private Scooter[] scooters; // coleção estática
    private Map<String, User> users;
    private ReentrantLock lockUsers;
    private Map<Integer, Reservation> reservations;
    private ReentrantLock lockReservations;
    private List<Reward> rewards;
    public ReentrantLock lockRewards;
    private Condition rewardsCond;
    public Condition notificationsCond;
    private int reservationID; // Para a condição da variável de condição


    /**
     * Instantiates scooters map and collection lock
     */
    public ScooterManager(){
        this.scooters = new Scooter[S];
        this.users = new HashMap<>();
        this.lockUsers = new ReentrantLock();
        this.rewards = new ArrayList<>();
        this.lockRewards = new ReentrantLock();
        this.reservations = new HashMap<>();
        this.lockReservations = new ReentrantLock();
        this.rewardsCond = this.lockRewards.newCondition();
        this.notificationsCond = this.lockReservations.newCondition();

        this.reservationID = -1;

        this.randomizeScooterPositions();

        new Thread(() -> {
            this.generateRewards();
        }).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(Reward r: this.rewards){
            System.out.println(r.toString());
        }
    }

    public void register(String username, String password) throws UsernameAlreadyExistsException {
        try{
            this.lockUsers.lock();

            if(this.users.containsKey(username)){
                throw new UsernameAlreadyExistsException("Username " + username + " already exists!");
            }

            User newUser = new User(username, password);
            this.users.put(username, newUser);
        }
        finally {
            this.lockUsers.unlock();
        }
    }

    public boolean login(String username, String password) throws NonExistentUsernameException {
        try{
            this.lockUsers.lock();

            if(!this.users.containsKey(username)){
                throw new NonExistentUsernameException("Username " + username + " doesn't exist!");
            }

            User user = this.users.get(username);

            return user.getUsername().equals(username) && user.getPassword().equals(password);
        }
        finally {
            this.lockUsers.unlock();
        }
    }

    public void changeNotificationsState(String username, boolean notificationsState) {
        User user = null;

        try {
            this.lockUsers.lock();

            user = this.users.get(username);

            user.lock.lock();
        }
        finally {
            this.lockUsers.unlock();
        }

        try {
            user.setNotificationsState(notificationsState);
        }
        finally {
            user.lock.unlock();
        }
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

    public void printMatrix(int [][] matrix){
        String rep = "";
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

        try{
            for(Scooter scooter: this.scooters){ // Iterate over scooters set
                scooter.lockScooter.lock();

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
            for(Scooter scooter : this.scooters){
                scooter.lockScooter.unlock();
            }
        }
    }

    /**
     * List the rewards in a radius D (pre-configured) of p
     * @param p center of radius where rewards will be checked
     * @return a list of the positions of the rewards
     */
    public List<List<Position>> listRewards(Position p) {
        List<List<Position>> rewards = new ArrayList<>();

        try {
            this.lockRewards.lock();

            for (Reward reward : this.rewards) {
                Position rewardPosition = reward.getInitialPosition(); // inicial ou final ?
                if (rewardPosition.inRadius(p, D)) {
                    List<Position> rewardPair = new ArrayList<>();
                    rewardPair.add(0, rewardPosition);
                    rewardPair.add(1, reward.getFinalPosition());
                    rewards.add(rewardPair);
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
        System.out.println("Thread " + Thread.currentThread().getName() + " started activating scooter at position " + p.toString());

        //this.lockScooters.lock();

        try{
            for (Scooter scooter: this.scooters) { // Iterate over scooters set
                scooter.lockScooter.lock();
                Position scooterPosition = scooter.getPosition();
                if (scooter.getIsFree() && scooterPosition.inRadius(p, D)) { // If scooterPosition in radius D of p
                    if (nearScooter == null) {
                        nearScooter = scooter;
                    }
                    else {
                        if (scooterPosition.distanceTo(p) < (nearScooter.getPosition().distanceTo(p))) {
                            nearScooter.lockScooter.unlock();
                            nearScooter = scooter;
                        }
                        else scooter.lockScooter.unlock();
                    }
                }
                else scooter.lockScooter.unlock(); // ir libertando à medida
            }

            if (nearScooter == null) {
                throw new NoScootersAvailableException("There are no available scooters in a radius " + D + " of " + p.toString() + "!");
            }

            nearScooter.setIsFree(false);

            this.lockReservations.lock();
            nearScooter.lockScooter.unlock();

            Reservation r = new Reservation(nearScooter, username);
            this.reservationID = r.getReservationID(); // para a condição
            this.lockRewards.lock();
            this.rewardsCond.signal();
            this.lockRewards.unlock();
            this.reservations.put(r.getReservationID(), r);

            return r; // clone???
        }
        finally {
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
        LocalDateTime parkTimestamp = LocalDateTime.now();
        try{
            this.lockReservations.lock();

            Reservation reservation = this.reservations.get(reservationID); // lançar exceção se for null
            this.reservations.remove(reservationID); // removemos do mapa?

            scooter = reservation.getScooter();
            this.reservationID = reservation.getReservationID(); // para a condição

            scooter.lockScooter.lock();
            this.lockReservations.unlock();

            scooter.setPosition(parkingPosition);
            scooter.setIsFree(true);

            Position initialPosition = reservation.getInitialPosition();
            double distance = initialPosition.distanceTo(parkingPosition);
            double duration = ChronoUnit.MINUTES.between(parkTimestamp, reservation.getTimestamp()); // Segundos
            double cost = ScooterManager.calculateCost(distance, duration);


            return cost;
        }
        finally {
            this.rewardsCond.signal(); // acordar a thread
            scooter.lockScooter.unlock();
        }
    }

    public void generateRewards(){
        // Percorrer as trotis para tirar as posições das livres
        // Para trotis em posições iguais, gerar recompensas
        int lastReservationID = -1;
        while(true){ // Quando for estacionada
            try{
                this.lockRewards.lock();

                while(this.reservationID == lastReservationID){
                    try{
                        this.rewardsCond.await();
                    }
                    catch (Exception ignored){ // mudar

                    }
                }

                List<Position> positions = new ArrayList<Position>();
                List<Scooter> toUnlock = new ArrayList<Scooter>();
                for(Scooter s : this.scooters){
                    s.lockScooter.lock();
                    if(s.getIsFree()){
                        toUnlock.add(s);
                        positions.add(s.getPosition());
                    }
                    else s.lockScooter.unlock();
                }

                this.rewards.clear(); // Apagar as rewards que lá estavam
                for(Position p1 : positions){
                    if(positions.contains(p1)){ // Pelo menos 2 trotis no msm sítio
                        for (int i = 0; i < N; i++){
                            for (int j = 0; j < N; j++){
                                boolean bool = true;

                                for (Position p2 : positions){
                                    if(p2.inRadius(i, j, D)){
                                        bool = false;
                                        break;
                                    }
                                }

                                if(bool){
                                    this.rewards.add(new Reward(p1, new Position(i, j)));
                                }
                            }
                        }
                    }
                }


                for (Scooter s : toUnlock){
                    s.lockScooter.unlock();
                }

                lastReservationID = this.reservationID;
            }
            finally {
                this.lockRewards.unlock();
            }
        }
    }


    /**
     * Daemon that evaluates current scooter distribution and tries to optimize it, generating rewards
     */
    /*public void generateRewards(){
        System.out.println("Matrix inicial");
        int[][] matrix = this.convertToMatrix(); // Será uma variável partilhada depois
        this.printMatrix(matrix);
        List<Position> overcrowdedPositions = new ArrayList<Position>();
        List<Position> freePositions = new ArrayList<Position>();
        int count = 0;

        while (true){
            lockScooters.lock();
            checkForRewards(overcrowdedPositions, freePositions, matrix);
            while (count == 30 || overcrowdedPositions.size() == 0 || freePositions.size() == 0){
                try {
                    System.out.println("Matrix final");
                    this.printMatrix(matrix);
                    System.out.println("Olá");
                    cond.await();
                    count = 0;
                    System.out.println("Olé");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            Reward newReward = null;
            // Generate one reward, if possible
            if (overcrowdedPositions.size() >= 1 && freePositions.size() >= 1){
                newReward = new Reward(overcrowdedPositions.remove(0), freePositions.remove(0), 2); // 2 is hard-coded here
                updateMatrix(newReward.getInitialPosition(), newReward.getFinalPosition(), matrix);
                this.rewards.add(newReward); // Needs locking
            }

            overcrowdedPositions.clear();
            freePositions.clear();
            count++;

        }
    }*/

    /**
     * Updates the count in each position after a scooter is moved
     * @param initialPosition where it was
     * @param finalPosition where it is now
     * @param matrix the matrix with the counts
     */
    private void updateMatrix(Position initialPosition, Position finalPosition, int[][] matrix){
        matrix[initialPosition.getY()][initialPosition.getX()]--;
        matrix[finalPosition.getY()][finalPosition.getX()]++;
    }

    /**
     * Checks if there are ways to create rewards in the map
     * @param overcrowdedPositions a list of positions with more than one scooter in a radius of D
     * @param freePositions a list of positions with no scooters in a radius of D
     */
    private void checkForRewards(List<Position> overcrowdedPositions, List<Position> freePositions, int [][] matrix){
        for(int i=0; i<N; i++){ // Iterate the map looking for overcrowded positions and free positions
            for(int j=0; j<N; j++){
                int positionState = this.evaluatePosition(i, j, matrix);
                if (positionState == 0){ // 0 livres
                    Position p = new Position(j, i);
                    if (!freePositions.contains(p)){
                        freePositions.add(p);
                    }
                }
                else if (positionState > 1){ // Mais do que 1 livre
                    Position p = new Position(j, i);
                    if (!overcrowdedPositions.contains(p) && matrix[i][j] >= 1){
                        overcrowdedPositions.add(p);
                    }
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
    public int evaluatePosition(int lineNum, int columnNum, int [][] matrix){
        int up, down, left, right;
        up = getBorderLeft(lineNum);
        down = getBorderRight(lineNum);
        left = getBorderLeft(columnNum);
        right = getBorderRight(columnNum);

        int count = 0;

        for(int i=up; i<=down; i++){
            for(int j=left; j<=right; j++){
                count += matrix[i][j];
            }
        }

        return count;
    }

    /**
     * Gets the rewards on the radius of a given position
     * @param p Position
     * @return List of rewards
     */
    public List<Reward> getRewardsFromPosition(Position p){
        List<Reward> rewards = new ArrayList<Reward>();

        for(Reward r : this.rewards){
            Position pos = r.getInitialPosition();

            if(pos.inRadius(p, D)){
                rewards.add(r);
            }
        }

        return rewards;
    }

    /**
     * Check if there are rewards on the radius of a given position and waits if there are no rewards
     * @param p Position
     * @return List of rewards on the radius of a given position
     */
    public List<Reward> userNotifications(String username, Position p){
        try{
            this.lockRewards.lock();

            List<Reward> rewards = null;

            while((rewards = this.getRewardsFromPosition(p)) == null){ // Condição : enquanto não houver recompensas no seu raio, adormece
                User u = null;

                try{
                    this.notificationsCond.await();

                    //verificar se as notificações estão desativadas
                    try {
                        this.lockUsers.lock();

                        u = this.users.get(username);

                        u.lock.lock();
                    }
                    finally {
                        this.lockUsers.unlock();
                    }

                    try {
                        if (!u.getNotificationsState()) {
                            throw new NotificationsDisabledException("Notifications are disabled!");
                        }
                    }
                    finally {
                        u.lock.unlock();
                    }
                }
                catch (Exception ignored){

                }
            }

            return rewards;
        }
        finally {
            this.lockRewards.unlock();
        }
    }
}
