import java.io.IOException;

public class RunClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        client.start();

        String username = "guilherme";
        String password = "lol";

        int flag = client.register(username, password);

        System.out.println(flag);

        client.close();
    }
}
