import Connections.Demultiplexer;
import Connections.TaggedConnection;
import SharedState.Position;
import SharedState.Reservation;
import SharedState.Reward;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Client {
    private final Demultiplexer multiplexer;

    /**
     * Client's Constructor
     * @throws IOException Exception
     */
    public Client() throws IOException{
        Socket socket = new Socket("localhost", 12345);
        TaggedConnection connection = new TaggedConnection(socket);
        this.multiplexer = new Demultiplexer(connection);
    }

    /**
     * Starts a multiplexer
     * @throws IOException Exception
     */
    public void start() throws IOException{
        this.multiplexer.start();
    }

    /**
     * Closes a multiplexer
     * @throws IOException Exception
     */
    public void close() throws IOException {
        this.multiplexer.close();
    }

    /**
     * Registration of an user
     * @param username user's name
     * @param password user's password
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    public boolean register(String username, String password) throws IOException, InterruptedException {
        // send request
        int size = 4 + username.length() + password.length();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeUTF(username);
        os.writeUTF(password);

        this.multiplexer.send(1, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(1); // bloqueia enquanto nao existirem mensagens / erro

        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        boolean flag = is.readBoolean();

        return flag;
    }

    /**
     * Login of an user
     * @param username user's name
     * @param password user's password
     * @return response
     * @throws IOException Exception
     * @throws InterruptedException Exception
     */
    public boolean login(String username, String password) throws IOException, InterruptedException {
        int size = 4 + username.length() + password.length();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeUTF(username);
        os.writeUTF(password);

        this.multiplexer.send(2, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(2); // bloqueia enquanto nao existirem mensagens / erro

        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        boolean flag = is.readBoolean();

        return flag;
    }

    /**
     * Sends to the multiplexer a request for free scooters near p
     * Waits for the response of the server
     * TODO test it!
     * @param p the position sent
     * @return a list of nearby scooter positions
     * @throws IOException
     * @throws InterruptedException
     */
    public List<Position> listFreeScooters(Position p) throws IOException, InterruptedException {
        //Thread freeScooters = new Thread(() -> {
            try{
                int size = 8; // (x)4 + (y)4 bytes
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
                DataOutputStream os = new DataOutputStream(byteArray);
                os.writeInt(p.getX());
                os.writeInt(p.getY());

                this.multiplexer.send(3, byteArray.toByteArray());

                // get reply
                byte[] data = this.multiplexer.receive(3);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

                List<Position> positions = new ArrayList<>();
                int length = is.readInt();

                for(int i=0; i<length; i++){
                    int x = is.readInt();
                    int y = is.readInt();
                    positions.add(new Position(x, y));
                }

                return positions;
            }
            catch (Exception ignored){}
            return null;
        //}).start();
    }

    /**
     * Sends to the multiplexer a request for listing rewards near p
     * Waits for the response of the server
     * TODO test it!
     * @param p the position sent
     * @return a list of pairs (origin - destination)
     * @throws IOException
     * @throws InterruptedException
     */
    public List<List<Position>> listRewards(Position p) throws IOException, InterruptedException {
        //Thread listRewards = new Thread(() -> {
            try{
                int size = 8; // (x)4 + (y)4 bytes
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
                DataOutputStream os = new DataOutputStream(byteArray);
                os.writeInt(p.getX());
                os.writeInt(p.getY());

                this.multiplexer.send(4, byteArray.toByteArray());

                // get reply
                byte[] data = this.multiplexer.receive(4);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

                List<List<Position>> rewards = new ArrayList<>();
                int length = is.readInt();

                for(int i=0; i<length; i++){
                    int xi = is.readInt();
                    int yi = is.readInt();
                    int xf = is.readInt();
                    int yf = is.readInt();

                    List<Position> pair = new ArrayList<>();
                    pair.add(0, new Position(xi, yi)); // Posição inicial
                    pair.add(1, new Position(xf, yf)); // Posição final

                    rewards.add(i, pair);
                }

                return rewards;
            }
            catch(IOException ignored){}
            return null;
        //}).start();
    }

    /**
     * Sends to the multiplexer a request for activating a scooter the closest to p
     * Waits for the response of the server
     * TODO test it!
     * @param p the position sent
     * @return a reservation (id and initial position)
     * @throws IOException
     * @throws InterruptedException
     */
    public Reservation activateScooter(Position p) throws IOException, InterruptedException {
        //Thread activateScooter = new Thread(() -> {
            try{
                int size = 8;;// + username.length(); // (x)4 + (y)4 bytes
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
                DataOutputStream os = new DataOutputStream(byteArray);
                os.writeInt(p.getX());
                os.writeInt(p.getY());
                //os.writeUTF(username);

                this.multiplexer.send(5, byteArray.toByteArray());

                // get reply
                byte[] data = this.multiplexer.receive(5);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

                Reservation reservation = null;
                int returnCode = is.readInt();

                if (returnCode >= 0) {
                    int x = is.readInt();
                    int y = is.readInt();
                    //int codReservation = is.readInt();
                    reservation = new Reservation(returnCode, new Position(x, y)) ;
                }
                else {
                    return null;
                }

                return reservation;
            }
            catch (Exception ignored){}
            return null;
        //}).start();
    }

    /**
     * Sends to the multiplexer a request for parking a scooter at p
     * Waits for the response of the server
     * TODO test it!
     * @param p the position sent
     * @param codReservation the code of the reservation
     * @return an int (if > 0, the reward, if < 0, the cost)
     * @throws IOException
     * @throws InterruptedException
     */
    public double parkScooter(Position p, int codReservation) throws IOException, InterruptedException {
        //Thread parkScooter = new Thread(() -> {
            try{
                int size = 12; // (x)4 + (y)4 bytes + (code)4 bytes
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream(size);
                DataOutputStream os = new DataOutputStream(byteStream);
                os.writeInt(p.getX());
                os.writeInt(p.getY());
                os.writeInt(codReservation);

                this.multiplexer.send(6, byteStream.toByteArray());

                // get reply
                byte[] data = this.multiplexer.receive(6);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

                double cost = is.readDouble();

                return cost;
            }
            catch (Exception ignored){

            }
        //}).start();
            return 0;
    }

    /**
     * Turns on or off notifications for rewards near a given position
     * @param onOff false - Off, true - On
     * @param p given position or null if onOff == false
     */
    public void turnOnNotifications(boolean onOff, Position p){

        try{
            int size = 1 + (p != null ? 4 : 0); // (onOff)1 + (p)4 bytes
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(size);
            DataOutputStream os = new DataOutputStream(byteStream);
            os.writeBoolean(onOff);
            if (p != null){
                os.writeInt(p.getX());
                os.writeInt(p.getY());
            }

            this.multiplexer.send(7, byteStream.toByteArray());
        }
        catch (Exception ignored){

        }

    }


    /**
     * Method that waits for notifications from the server regarding rewards near a position specified before
     */
    public void waitForNotifications(){
        try {
            while (true) {
                byte[] data = this.multiplexer.receive(7);
                DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
                List<Reward> rewards = new ArrayList<>();

                int length = is.readInt();
                for(int i=0; i<length; i++){
                    Reward r = Reward.deserialize(is);
                    rewards.add(r);
                }

                System.out.println("Novas notificações recebidas ....");
                for(int i=0; i<length; i++){
                    System.out.println(rewards.get(i).toString());
                }
            }
        } catch (Exception e){

        }
    }

}