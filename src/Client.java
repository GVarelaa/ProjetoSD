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

    public Client() throws IOException{
        Socket socket = new Socket("localhost", 12345);
        TaggedConnection connection = new TaggedConnection(socket);
        this.multiplexer = new Demultiplexer(connection);
    }

    public void start() throws IOException{
        this.multiplexer.start();
    }

    public void close() throws IOException {
        this.multiplexer.close();
    }

    public int register(String username, String password) throws IOException, InterruptedException {
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
        int flag = is.readInt();

        return flag;
    }

    public int login(String username, String password) throws IOException, InterruptedException {
        int size = 4 + username.length() + password.length();
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeUTF(username);
        os.writeUTF(password);

        this.multiplexer.send(2, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(2); // bloqueia enquanto nao existirem mensagens / erro

        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
        int flag = is.readInt();

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
            positions.add(0, new Position(x, y));
        }

        return positions;
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
        int size = 8; // (x)4 + (y)4 bytes
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeInt(p.getX());
        os.writeInt(p.getY());

        this.multiplexer.send(5, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(5);
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

        Reservation reservation = null;
        int returnCode = is.readInt();

        if (returnCode == 0){
            int x = is.readInt();
            int y = is.readInt();
            int codReservation = is.readInt();
            reservation = new Reservation(codReservation, new Position(x, y)) ;
        }

        return reservation;
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
    public int parkScooter(Position p, int codReservation) throws IOException, InterruptedException {
        int size = 12; // (x)4 + (y)4 bytes + (code)4 bytes
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);
        os.writeInt(p.getX());
        os.writeInt(p.getY());
        os.writeInt(codReservation);

        this.multiplexer.send(6, byteArray.toByteArray());

        // get reply
        byte[] data = this.multiplexer.receive(6);
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

        return is.readInt();
    }
}
