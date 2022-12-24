import SharedState.Position;

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
        client.turnOnNotifications(true, new Position(0,0));
        //System.out.println(flag);

        client.close();
    }
}
