import SharedState.Position;

import java.io.IOException;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();

        //String username = "guilherme";
        //String password = "lol";

        //boolean flag = client.register(username, password);
        client.turnOnNotifications(true, new Position(0,0));
        //System.out.println(flag);

        client.close();
    }
}
