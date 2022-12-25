import Connections.TaggedConnection;
import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistantUsernameException;
import Exceptions.UsernameAlreadyExistsException;
import SharedState.SharedState;
import SharedState.User;
import SharedState.UserManager;
import SharedState.Position;
import SharedState.Reservation;
import SharedState.Reward;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


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
            // Session state
            boolean notificationsOn = false;
            while (true){
                TaggedConnection.Frame frame = this.connection.receive();

                if(frame.tag == 1){   // é um pedido de registo
                    User user = User.deserialize(frame.data);
                    try{
                        this.sharedState.register(user.getUsername(), user.getPassword());

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(true); // All good

                        this.connection.send(1, byteStream.toByteArray());
                    }
                    catch(UsernameAlreadyExistsException e){
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(false); // Something went wrong

                        this.connection.send(1, byteStream.toByteArray());
                    }
                }
                else if(frame.tag == 2){   // é um pedido de autenticação
                    User user = User.deserialize(frame.data);

                    try{
                        this.sharedState.login(user.getUsername(), user.getPassword());

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(true); // All good

                        this.connection.send(2, byteStream.toByteArray());
                    }
                    catch(NonExistantUsernameException e){
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(false); // Something went wrong

                        this.connection.send(2, byteStream.toByteArray());
                    }
                }
                else if(frame.tag == 3){
                    Position p = Position.deserialize(frame.data);
                    List<Position> positions = this.sharedState.listFreeScooters(p);

                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4 + positions.size()*8);
                    DataOutputStream os = new DataOutputStream(byteStream);

                    os.writeInt(positions.size()); // Comprimento da lista
                    for(Position pos : positions){
                        os.write(pos.serialize());
                    }

                    this.connection.send(3, byteStream.toByteArray());
                }
                else if (frame.tag == 4) {
                    Position p = Position.deserialize(frame.data);
                    List<Position> positions = this.sharedState.listRewards(p);

                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4 + positions.size()*8);
                    DataOutputStream os = new DataOutputStream(byteStream);

                    os.writeInt(positions.size());
                    for (Position pos : positions) {
                        os.write(pos.serialize());
                    }

                    this.connection.send(4, byteStream.toByteArray());
                }
                else if (frame.tag == 5) {
                    Position p = Position.deserialize(frame.data);

                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(frame.data);
                    DataInputStream is = new DataInputStream(byteInputStream);
                    String username = is.readUTF();

                    try {
                        Reservation reservation = this.sharedState.activateScooter(p, username);

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(13); // (x) 4 bytes + (y) 4 bytes + (reservation_id) 4 bytes + (bool) 1 byte
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(true);
                        os.write(reservation.getInitialPosition().serialize());
                        os.writeInt(reservation.getReservationID());

                        this.connection.send(5, byteStream.toByteArray());
                    }
                    catch (NoScootersAvailableException e){
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(false); // Something went wrong

                        this.connection.send(5, byteStream.toByteArray());
                    }
                }
                else if (frame.tag == 6) {
                    Position p = Position.deserialize(frame.data);

                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(frame.data);
                    DataInputStream is = new DataInputStream(byteInputStream);
                    int reservationID = is.readInt();

                    double cost = this.sharedState.parkScooter(reservationID, p);

                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(8);  // 8 - double
                    DataOutputStream os = new DataOutputStream(byteStream);
                    os.writeDouble(cost);

                    this.connection.send(6, byteStream.toByteArray());
                }
                else if (frame.tag == 7){
                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(frame.data);
                    DataInputStream is = new DataInputStream(byteInputStream);
                    boolean onOff = is.readBoolean();

                    if (onOff == true){ // Ler a posição
                        int x = is.readInt();
                        int y = is.readInt();
                        Position p = new Position(x, y);
                        List<Reward> rewards = new ArrayList<Reward>();

                        while(true){
                            rewards = this.sharedState.askForNotifications(p);

                            for(Reward r : rewards){
                                this.connection.send(7, r.serialize());
                            }
                        }
                    }
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
