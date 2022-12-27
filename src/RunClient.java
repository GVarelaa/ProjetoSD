import SharedState.Position;
import SharedState.Reservation;
import SharedState.Reward;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();

        Scanner sc = new Scanner(System.in);

        RunClient.loginMenu(sc, client);
        RunClient.showMenu(sc, client);

        client.close();
    }

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
            System.out.println("Registo efetuado com sucesso!");
        }
        else {
            System.out.println("Registo não efetuado!");
        }
        return success;
    }

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

    private static void loginMenu(Scanner sc, Client c) throws IOException, InterruptedException {
        boolean registered = false;

        while (!registered){
            registered = signUpMenu(sc, c);
        }

        boolean loggedIn = false;

        while (!loggedIn){
            loggedIn = signInMenu(sc, c);
        }
    }

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

    private static void parkScooterMenu(Scanner sc, Client c, int codReservation) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        c.parkScooter(new Position(x, y), codReservation);
    }

    private static void turnOnNotificationsMenu(Scanner sc, Client c){
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        c.turnOnNotifications(true, new Position(x, y));
    }

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
