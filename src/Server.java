import Connections.TaggedConnection;
import Exceptions.NoScootersAvailableException;
import Exceptions.NonExistentUsernameException;
import Exceptions.NotificationsDisabledException;
import Exceptions.UsernameAlreadyExistsException;
import SharedState.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


class ServerWorker implements Runnable{
    private TaggedConnection connection;
    private ScooterManager scooterManager;
    private String clientUsername; //username do cliente - lock?

    public ServerWorker(TaggedConnection connection, ScooterManager scooterManager){
        this.connection = connection;
        this.scooterManager = new ScooterManager();
        this.clientUsername = null;
    }

    @Override
    public void run(){
        try{
            while (true){
                TaggedConnection.Frame frame = this.connection.receive();

                if(frame.tag == 1){   // é um pedido de registo
                    User user = User.deserialize(frame.data);
                    try{
                        this.scooterManager.register(user.getUsername(), user.getPassword());

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
                        this.scooterManager.login(user.getUsername(), user.getPassword());

                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(true); // All good

                        this.clientUsername = user.getUsername();

                        this.connection.send(2, byteStream.toByteArray());
                    }
                    catch(NonExistentUsernameException e){
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1);
                        DataOutputStream os = new DataOutputStream(byteStream);
                        os.writeBoolean(false); // Something went wrong

                        this.connection.send(2, byteStream.toByteArray());
                    }
                }
                else if(frame.tag == 3){
                    Position p = Position.deserialize(frame.data);
                    List<Position> positions = this.scooterManager.listFreeScooters(p);

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
                    List<Position> positions = this.scooterManager.listRewards(p);

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
                    //String username = is.readUTF();

                    try {
                        Reservation reservation = this.scooterManager.activateScooter(p, this.clientUsername);

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

                    double cost = this.scooterManager.parkScooter(reservationID, p);

                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream(8);  // 8 - double
                    DataOutputStream os = new DataOutputStream(byteStream);
                    os.writeDouble(cost);

                    this.connection.send(6, byteStream.toByteArray());
                }
                else if (frame.tag == 7){
                    ByteArrayInputStream byteInputStream = new ByteArrayInputStream(frame.data);
                    DataInputStream is = new DataInputStream(byteInputStream);
                    boolean notificationsOn = is.readBoolean();
                    this.scooterManager.changeNotificationsState(this.clientUsername, notificationsOn);

                    if (notificationsOn){ // Ler a posição
                        int x = is.readInt();
                        int y = is.readInt();
                        Position p = new Position(x, y);
                        List<Reward> rewards = new ArrayList<Reward>();

                        while(true){

                            rewards = this.scooterManager.userNotifications(this.clientUsername, p);


                            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(4);  // 4 - int
                            DataOutputStream os = new DataOutputStream(byteStream);
                            os.writeInt(rewards.size());

                            this.connection.send(7, byteStream.toByteArray()); // Comprimento da lista de recompensas
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
        ScooterManager scooterManager = new ScooterManager();

        while(true){
            Socket socket = serverSocket.accept();
            TaggedConnection connection = new TaggedConnection(socket);
            ServerWorker serverWorker = new ServerWorker(connection, scooterManager);

            for(int i = 0; i < WORKERS_PER_CONNECTION; i++){
                new Thread(serverWorker).start();
            }
        }
    }
}
