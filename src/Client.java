import SharedState.Position;
import SharedState.Reservation;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        ScooterManager client = new ScooterManagerStub();
        client.start();

    /**
     * Client's Constructor
     * @throws IOException Exception
     */
    public Client() throws IOException{
        Socket socket = new Socket("localhost", 12345);
        TaggedConnection connection = new TaggedConnection(socket);
        this.multiplexer = new Demultiplexer(connection);
    }

    /**
     * Starts a multiplexer
     * @throws IOException Exception
     */
    public void start() throws IOException{
        this.multiplexer.start();
    }

    /**
     * Closes a multiplexer
     * @throws IOException Exception
     */
    public void close() throws IOException {
        this.multiplexer.close();
    }

    /**
     * Registration of an user
     * @param username user's name
     * @param password user's password
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    public boolean register(String username, String password) throws IOException, InterruptedException {
        // send request
        int size = 4 + username.length() + password.length();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeUTF(username);
        os.writeUTF(password);

        this.multiplexer.send(1, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(1); // bloqueia enquanto nao existirem mensagens / erro

        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        boolean flag = is.readBoolean();

        return flag;
    }

    /**
     * Login of an user
     * @param username user's name
     * @param password user's password
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    public boolean login(String username, String password) throws IOException, InterruptedException {
        int size = 4 + username.length() + password.length();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeUTF(username);
        os.writeUTF(password);

        this.multiplexer.send(2, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(2); // bloqueia enquanto nao existirem mensagens / erro

        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        boolean flag = is.readBoolean();

        return flag;
    }

    /**
     * Sends to the multiplexer a request for free scooters near p
     * Waits for the response of the server
     * TODO test it!
     * @param p the position sent
     * @return a list of nearby scooter positions
     * @throws IOException
     * @throws InterruptedException
     */
    public List<Position> listFreeScooters(Position p) throws IOException, InterruptedException {
        //Thread freeScooters = new Thread(() -> {
            try{
                int size = 8; // (x)4 + (y)4 bytes
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
                DataOutputStream os = new DataOutputStream(byteArray);
                os.writeInt(p.getX());
                os.writeInt(p.getY());

                this.multiplexer.send(3, byteArray.toByteArray());

                // get reply
                byte[] data = this.multiplexer.receive(3);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

                List<Position> positions = new ArrayList<>();
                int length = is.readInt();

                for(int i=0; i<length; i++){
                    int x = is.readInt();
                    int y = is.readInt();
                    positions.add(new Position(x, y));
                }

                return positions;
        Scanner sc = new Scanner(System.in);

        Client.loginMenu(sc, client);
        Client.showMenu(sc, client);

        client.close();
    }

    private static void showMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        int codReservation = -1;
        while (true){
            System.out.println("============");
            System.out.println("3... Show available scooters");
            System.out.println("4... Activate scooter");
            System.out.println("5... Park scooter");
            System.out.println("6... Turn on notifications");
            System.out.println("7... Turn off notifications");
            System.out.println("8... Mostrar recompensas");
            System.out.println("============");

            int opt = sc.nextInt();

            switch (opt){
                case 1:
                    signUpMenu(sc, c);
                    break;
                case 2:
                    signInMenu(sc, c);
                    break;
                case 3:
                    showScootersMenu(sc, c);
                    break;
                case 4:
                    codReservation = activateScooterMenu(sc, c);
                    break;
                case 5:
                    parkScooterMenu(sc, c, codReservation);
                    break;
                case 6:
                    turnOnNotificationsMenu(sc, c);
                    break;
                case 7:
                    c.turnOnNotifications(false, null);
                    break;
                case 8:
                    showRewardsMenu(sc, c);
            }
        }
    }

    private static boolean signUpMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        System.out.println("============");
        System.out.println("Registar utilizador");
        System.out.println("============");
        System.out.println("Introduza o username: ");
        String username = sc.next();
        System.out.println("Introduza a password: ");
        String password = sc.next();

        boolean success = c.register(username, password);
        if (success == true){
            c.login(username, password);
            System.out.println("Registo efetuado com sucesso!");
        }
        else {
            System.out.println("Registo não efetuado!");
        }
        return success;
    }

    private static boolean signInMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        System.out.println("============");
        System.out.println("Autenticar utilizador");
        System.out.println("============");
        System.out.println("Introduza o username: ");
        String username = sc.next();
        System.out.println("Introduza a password: ");
        String password = sc.next();

        boolean success = c.login(username, password);
        if (success == true){
            System.out.println("Login efetuado com sucesso!");
        }
        else {
            System.out.println("Login inválido!");
        }
        return success;
    }

    private static void loginMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        boolean registered = false;
        boolean loggedIn = false;

        while (!registered && !loggedIn){
            System.out.println("============");
            System.out.println("1... Registar");
            System.out.println("2... Autenticar");
            System.out.println("============");

            int opt = sc.nextInt();

            if (opt == 1){
                registered = signUpMenu(sc, c);
            }

            if (opt == 2){
                loggedIn = signInMenu(sc, c);
            }
        }

    }

    private static void showScootersMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        Position p = new Position(x, y);
        List<Position> availScooters = c.listFreeScooters(p);
        if (availScooters.size() == 0){
            System.out.println("Nenhuma scooter livre perto de " + p.toString());
        }
        for(Position pos: availScooters){
            System.out.println("Scooter livre em " + pos.toString());
        }
    }

    private static int activateScooterMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        Position p = new Position(x, y);
        Reservation r = c.activateScooter(p);
        if (r != null){
            System.out.println("Scooter na posição: " + r.getInitialPosition().toString());
            System.out.println("Reservation ID: " + r.getReservationID());
            return r.getReservationID();
        }
        else{
            System.out.println("Não há scooters disponíveis perto de " + p.toString());
            return -1;
        }

    }

    private static void parkScooterMenu(Scanner sc, ScooterManager c, int codReservation) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        double cost = c.parkScooter(new Position(x, y), codReservation);

        if (cost > 0){
            System.out.println("Prémio da recompensa: " + cost);
        }
        else if (cost < 0){
            System.out.println("Custo da viagem: " + cost);
        }
    }

    private static void turnOnNotificationsMenu(Scanner sc, ScooterManager c){
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        c.turnOnNotifications(true, new Position(x, y));
        Thread t = new Thread(() -> {
            c.waitForNotifications();
        });
        t.start();
    }

    private static void showRewardsMenu(Scanner sc, ScooterManager c) throws IOException, InterruptedException {
        System.out.println("Indique a posição");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        Position p = new Position(x, y);
        List<List<Position>> rewards = c.listRewards(p);

        if (rewards.size() == 0){
            System.out.println("Não há recompensas perto de " + p.toString());
        }

        for(List<Position> reward: rewards){
            System.out.println("Recompensa disponível de " + reward.get(0).toString() + " para " + reward.get(1).toString());
        }
    }



}
