import Connections.TaggedConnection;
import Exceptions.NonExistantUsernameException;
import Exceptions.UsernameAlreadyExistsException;
import SharedState.SharedState;
import SharedState.User;
import SharedState.UserManager;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


class ServerWorker implements Runnable{
    private TaggedConnection connection;
    private SharedState sharedState;

    public ServerWorker(TaggedConnection connection, SharedState sharedState){
        this.connection = connection;
        this.sharedState = sharedState;
    }

    @Override
    public void run(){
        try{
            TaggedConnection.Frame frame = this.connection.receive();

            if(frame.tag == 1){   // é um pedido de registo
                User user = User.deserialize(frame.data);
                try{
                    this.sharedState.register(user.getUsername(), user.getPassword());

                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
                    DataOutputStream os = new DataOutputStream(byteArray);
                    os.writeInt(0); // All good

                    this.connection.send(1, byteArray.toByteArray());
                }
                catch(UsernameAlreadyExistsException e){
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
                    DataOutputStream os = new DataOutputStream(byteArray);
                    os.writeInt(1); // Something went wrong

                    this.connection.send(1, byteArray.toByteArray());
                }
            }
            else if(frame.tag == 2){   // é um pedido de autenticação
                User user = User.deserialize(frame.data);

                try{
                    this.sharedState.login(user.getUsername(), user.getPassword());

                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
                    DataOutputStream os = new DataOutputStream(byteArray);
                    os.writeInt(0); // All good

                    this.connection.send(2, byteArray.toByteArray());
                }
                catch(NonExistantUsernameException e){
                    ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4);
                    DataOutputStream os = new DataOutputStream(byteArray);
                    os.writeInt(1); // Something went wrong

                    this.connection.send(2, byteArray.toByteArray());
                }
            }
        }
        catch (Exception ignored){
            //mudar
        }

    }
}

public class Server {
    final static int WORKERS_PER_CONNECTION = 3;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        SharedState sharedState = new SharedState();

        while(true){
            Socket socket = serverSocket.accept();
            TaggedConnection connection = new TaggedConnection(socket);
            ServerWorker serverWorker = new ServerWorker(connection, sharedState);

            for(int i = 0; i < WORKERS_PER_CONNECTION; i++){
                new Thread(serverWorker).start();
            }
        }
    }
}
