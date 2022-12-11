import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


class ServerWorker implements Runnable{
    private Socket socket;
    private UserManager userManager;

    public ServerWorker(Socket socket, UserManager userManager){
        this.socket = socket;
        this.userManager = userManager;
    }

    @Override
    public void run(){

    }
}

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        UserManager userManager = new UserManager();

        while(true){
            Socket socket = serverSocket.accept();

            Thread worker = new Thread(new ServerWorker(socket, userManager));
            worker.start();
        }
    }
}
