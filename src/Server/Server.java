package Server;

import Connections.TaggedConnection;
import SharedState.User;
import SharedState.UserManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


class ServerWorker implements Runnable{
    private TaggedConnection c;
    private UserManager userManager;

    public ServerWorker(TaggedConnection c, UserManager userManager){
        this.c = c;
        this.userManager = userManager;
    }

    @Override
    public void run(){
        try{
            TaggedConnection.Frame frame = this.c.receive();

            if(frame.tag == 1){   // é um pedido de registo
                User user = User.deserialize(frame.data);
            }
            else if(frame.tag == 2){   // é um pedido de autenticação
                User user = User.deserialize(frame.data);
            }
        }
        catch (Exception ignored){
            //mudar
        }

    }
}

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        UserManager userManager = new UserManager();

        while(true){
            Socket socket = serverSocket.accept();
            TaggedConnection c = new TaggedConnection(socket);

            Thread worker = new Thread(new ServerWorker(c, userManager));
            worker.start();
        }
    }
}
