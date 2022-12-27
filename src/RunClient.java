import SharedState.Position;
import SharedState.Reservation;

import java.io.IOException;
import java.util.Scanner;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();

        Scanner sc = new Scanner(System.in);

        System.out.println("============");
        System.out.println("1... Sign up");
        System.out.println("2... Sign in");
        System.out.println("============");

        int opt = sc.nextInt();

        if (opt == 1 || opt == 2) {
            System.out.println("Introduza o username: ");
            String username = sc.next();
            System.out.println("Introduza a password: ");
            String password = sc.next();

            if (opt == 1) client.register(username, password);
            else client.login(username, password);
        }


        //String username = "guilherme";
        //String password = "lol";

        //boolean flag = client.register(username, password);
        //client.turnOnNotifications(true, new Position(0,0));
        //System.out.println(flag);

        client.close();
    }

    private static void showMenu(Scanner sc, Client c) throws IOException, InterruptedException {
        int codReservation = 0;
        while (true){
            System.out.println("============");
            System.out.println("1... Sign up");
            System.out.println("2... Sign in");
            System.out.println("3... Show available scooters");
            System.out.println("4... Activate scooter");
            System.out.println("5... Park scooter");
            System.out.println("6... Turn on notifications");
            System.out.println("7... Turn off notifications");
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
            }
        }
    }

    private static void signUpMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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
    }

    private static void signInMenu(Scanner sc, Client c) throws IOException, InterruptedException {
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
    }

    private static void showScootersMenu(Scanner sc, Client c) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        c.listFreeScooters(new Position(x, y));
    }

    private static int activateScooterMenu(Scanner sc, Client c) throws IOException, InterruptedException {
        System.out.println("Indique a posição: ");
        System.out.println("x: ");
        int x = sc.nextInt();
        System.out.println("y: ");
        int y = sc.nextInt();

        Reservation r = c.activateScooter(new Position(x, y));

        System.out.println("Scooter na posição: " + r.getScooter().getPosition().toString());
        System.out.println("Reservation ID: " + r.getReservationID());
        return r.getReservationID();
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


}
