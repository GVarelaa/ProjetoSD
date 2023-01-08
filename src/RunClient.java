import SharedState.Position;
import SharedState.Reservation;
import SharedState.Reward;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class RunClient {

    /**
     * Client's Main
     * @param args System's args
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();

        Scanner sc = new Scanner(System.in);

        RunClient.loginMenu(sc, client);
        RunClient.showMenu(sc, client);

        client.close();
    }

    /**
     * Show main's menu
     * @param sc scanner
     * @param c client
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static void showMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Sign up Menu
     * @param sc scanner
     * @param c client
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static boolean signUpMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Sign in Menu
     * @param sc scanner
     * @param c client
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static boolean signInMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Login Menu
     * @param sc scanner
     * @param c client
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static void loginMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Show Scooter Menu
     * @param sc scanner
     * @param c client
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static void showScootersMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Show activation Scooter menu
     * @param sc scanner
     * @param c client
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static int activateScooterMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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

    /**
     * Show park scooter menu
     * @param sc scanner
     * @param c client
     * @param codReservation code Reservation
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static void parkScooterMenu(Scanner sc, Client c, int codReservation) throws IOException, InterruptedException {
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

    /**
     * Show Turn on notifications' menu
     * @param sc scanner
     * @param c client
     */
    private static void turnOnNotificationsMenu(Scanner sc, Client c){
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

    /**
     * Show Rewards' menu
     * @param sc scanner
     * @param c client
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    private static void showRewardsMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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
