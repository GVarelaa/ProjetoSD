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
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScooterManager {
    private final static int D = 2;
    private final static int N = 10; // dimensão do mapa
    private final static int S = 15; // número de scooters fixo,
    private Scooter[] scooters; // coleção estática
    private Map<String, User> users;
    private ReentrantReadWriteLock usersLock;
    private ReentrantReadWriteLock.ReadLock usersReadLock;
    private ReentrantReadWriteLock.WriteLock usersWriteLock;
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
        this.usersLock = new ReentrantReadWriteLock();
        this.usersReadLock = usersLock.readLock();
        this.usersWriteLock = usersLock.writeLock();
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
    }

    public void register(String username, String password) throws UsernameAlreadyExistsException {
        try {
            this.usersWriteLock.lock();

            if (this.users.containsKey(username)) {
                throw new UsernameAlreadyExistsException("Username " + username + " already exists!");
            }

            User newUser = new User(username, password);
            this.users.put(username, newUser);
        }
        finally {
            this.usersWriteLock.unlock();
        }
    }

    public boolean login(String username, String password) throws NonExistentUsernameException {
        try {
            this.usersReadLock.lock();

            if (!this.users.containsKey(username)) {
                throw new NonExistentUsernameException("Username " + username + " doesn't exist!");
            }

            User user = this.users.get(username);

            return user.getUsername().equals(username) && user.getPassword().equals(password);
        }
        finally {
            this.usersReadLock.unlock();
        }
    }

    public void changeNotificationsState(String username, boolean notificationsState) {
        User user = null;

        try {
            this.usersReadLock.lock();

            user = this.users.get(username);

            user.lock.lock();
        }
        finally {
            this.usersReadLock.unlock();
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
            Thread.sleep(10000);

            Reservation reservation = this.reservations.get(reservationID); // lançar exceção se for null
            this.reservations.remove(reservationID); // removemos do mapa?

            scooter = reservation.getScooter();
            this.reservationID = reservation.getReservationID(); // para a condição

            Position initialPosition = reservation.getInitialPosition();
            double distance = initialPosition.distanceTo(parkingPosition);
            double duration = ChronoUnit.SECONDS.between(parkTimestamp, reservation.getTimestamp()); // Segundos
            double cost = ScooterManager.calculateCost(distance, duration);

            this.lockRewards.lock();

            for(Reward r: this.rewards){ // Verificar se é uma recompensa
                if (r.getInitialPosition().equals(reservation.getInitialPosition()) && r.getFinalPosition().equals(parkingPosition)){
                    cost = r.getValue();
                    this.rewards.remove(r);
                }
            }

            scooter.lockScooter.lock();
            this.lockReservations.unlock();

            scooter.setPosition(parkingPosition);
            scooter.setIsFree(true);

            this.rewardsCond.signal(); // acordar a thread
            this.lockRewards.unlock();

            return cost;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {

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
                        this.usersReadLock.lock();

                        u = this.users.get(username);

                        u.lock.lock();
                    }
                    finally {
                        this.usersReadLock.unlock();
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
