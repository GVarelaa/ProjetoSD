package SharedState;

import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.NotificationsDisabledException;
import Exceptions.UsernameAlreadyExistsException;

import java.security.spec.PSSParameterSpec;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ScooterManager {
    private int D;
    private int N; // dimensão do mapa
    private int S; // número de scooters fixo,
    private Scooter[] scooters; // coleção estática
    private Map<String, User> users;
    private ReentrantReadWriteLock.ReadLock usersReadLock;
    private ReentrantReadWriteLock.WriteLock usersWriteLock;
    private Map<Integer, Reservation> reservations;
    private ReentrantLock reservationsLock;
    private List<Reward> rewards;
    public ReentrantLock rewardsLock;
    private Condition rewardsCond;
    public Condition notificationsCond;
    private int reservationID; // Para a condição da variável de condição


    /**
     * Instantiates a scooter manager shared state
     * @param D radius of search
     * @param N size of the map
     * @param S number of scooters
     */
    public ScooterManager(int D, int N, int S){
        this.D = D;
        this.N = N;
        this.S = S;
        this.scooters = new Scooter[S];

        this.users = new HashMap<>();
        ReentrantReadWriteLock usersLock = new ReentrantReadWriteLock();
        this.usersReadLock = usersLock.readLock();
        this.usersWriteLock = usersLock.writeLock();

        this.rewards = new ArrayList<>();
        this.rewardsLock = new ReentrantLock();

        this.reservations = new HashMap<>();
        this.reservationsLock = new ReentrantLock();

        this.rewardsCond = this.rewardsLock.newCondition();
        this.notificationsCond = this.rewardsLock.newCondition();

        this.reservationID = -1;

        this.randomizeScooterPositions();

        new Thread(() -> this.generateRewards()).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for(int i = 0; i < this.rewards.size(); i++) System.out.println(this.rewards.get(i));
    }

    public void register(String username, String password) throws UsernameAlreadyExistsException {
        try {
            this.usersWriteLock.lock(); // Total control of users collection

            if (this.users.containsKey(username)) { // Must be inside critial section, otherwise there can be two registers at the same time
                throw new UsernameAlreadyExistsException("Username " + username + " already exists!");
            }

            User newUser = new User(username, password);
            this.users.put(username, newUser);
            System.out.println(this.users.size());
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
            System.out.println(user.getNotificationsState());
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
            this.rewardsLock.lock();

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
            this.rewardsLock.unlock();
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

        this.reservationsLock.lock();

        try {
            nearScooter.lockScooter.unlock();

            Reservation r = new Reservation(nearScooter, username);
            this.reservationID = r.getReservationID(); // para a condição

            this.rewardsLock.lock();
            this.rewardsCond.signal();
            this.rewardsLock.unlock();

            this.reservations.put(r.getReservationID(), r);
            System.out.println(this.reservations.size());

            return r; // clone???
        } finally {
            this.reservationsLock.unlock();
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

        cost = 0.15 * distance;//* duration;// 15 centimos por minuto

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

        try {
            this.reservationsLock.lock();

            reservation = this.reservations.get(reservationID); // lançar exceção se for null
            this.reservations.remove(reservationID); // removemos do mapa?

            this.rewardsLock.lock();
        }
        finally {
            this.reservationsLock.unlock();
        }

        try {
            this.reservationID = reservation.getReservationID(); // para a condição

            scooter = reservation.getScooter();

            try {
                scooter.lockScooter.lock();
                scooter.setPosition(parkingPosition);
                scooter.setIsFree(true);
            }
            finally {
                scooter.lockScooter.unlock();
            }

            Position initialPosition = reservation.getInitialPosition();
            double distance = initialPosition.distanceTo(parkingPosition);
            double duration = ChronoUnit.SECONDS.between(parkTimestamp, reservation.getTimestamp()); // Segundos
            double cost = ScooterManager.calculateCost(distance, duration);

            for(Reward r: this.rewards){ // Verificar se é uma recompensa
                if (r.getInitialPosition().equals(reservation.getInitialPosition()) && r.getFinalPosition().equals(parkingPosition)){
                    cost = r.getValue();
                    this.rewards.remove(r);
                }
            }

            this.rewardsCond.signal(); // acordar a thread

            return cost;
        }
        finally {
            this.rewardsLock.unlock();
        }
    }

    public void generateRewards(){
        // Percorrer as trotis para tirar as posições das livres
        // Para trotis em posições iguais, gerar recompensas
        int lastReservationID = -2;
        while(true){ // Quando for estacionada
            try{
                this.rewardsLock.lock();

                while(this.reservationID == lastReservationID){ // igualar a -1 para calcular recompensas quando arranca
                    try{
                        this.rewardsCond.await();
                    }
                    catch (Exception ignored){ // mudar

                    }
                }

                List<Position> positions = new ArrayList<>();
                List<Scooter> toUnlock = new ArrayList<>();
                for(Scooter s : this.scooters){
                    s.lockScooter.lock();
                    if (s.getIsFree()) {
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

                if (lastReservationID == -2) lastReservationID = -1; // Para executar uma primeira vez quando inicia
                lastReservationID = this.reservationID;
            }
            finally {
                this.rewardsLock.unlock();
            }
        }
    }

    /**
     * Gets the rewards on the radius of a given position
     * @param p Position
     * @return List of rewards
     */
    public List<Reward> getRewardsFromPosition(Position p){
        List<Reward> rewards = new ArrayList<>();

        for(Reward r : this.rewards){
            Position pos = r.getInitialPosition();

            if(pos.inRadius(p, D)){
                rewards.add(r);
            }
        }

        return rewards;
    }

    /**
     * Calculates the elements that are in the first list but aren't in the second one
     * @param newRewards newly generated rewards
     * @param oldRewards old rewards (calculated before)
     * @return the difference between the two lists
     */
    private List<Reward> getDiff(List<Reward> newRewards, List<Reward> oldRewards){
        List<Reward> diff = new ArrayList<>();
        for (Reward r: newRewards){
            if (!oldRewards.contains(r)){
                diff.add(r);
            }
        }
        return diff;
    }

    /**
     * Check if there are rewards on the radius of a given position and waits if there are no rewards
     * @param p Position
     * @return List of new rewards on the radius of a given position
     */
    public List<Reward> userNotifications(String username, Position p, List<Reward> oldRewards) throws NotificationsDisabledException{
        System.out.println(username);
        List<Reward> diff;
        try{
            this.rewardsLock.lock();

            while((diff = this.getDiff(this.getRewardsFromPosition(p), oldRewards)).size() == 0){ // Condição : enquanto não houver recompensas no seu raio, adormece
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
            for(Reward r: diff){
                oldRewards.add(r);
            }

            return diff;
        }
        finally {
            this.rewardsLock.unlock();
        }
    }
}
