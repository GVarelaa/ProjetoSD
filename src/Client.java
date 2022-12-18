import Connections.Demultiplexer;
import Connections.TaggedConnection;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;

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
        int size = 8 + username.length() + password.length();
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
        int size = 8 + username.length() + password.length();
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
}
