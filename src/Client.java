import Connections.Demultiplexer;
import Connections.TaggedConnection;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 12345);
        TaggedConnection connection = new TaggedConnection(socket);
        Demultiplexer multiplexer = new Demultiplexer(connection);
        multiplexer.start();

        Thread[] threads = {

                new Thread(() -> {
                    try  {
                        // send request
                        String username = "guilherme";
                        String password = "lol";

                        int size = 8 + username.length() + password.length();
                        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
                        DataOutputStream os = new DataOutputStream(byteArray);
                        os.writeUTF(username);
                        os.writeUTF(password);

                        multiplexer.send(1, byteArray.toByteArray());
                        Thread.sleep(100);
                        // get reply
                        byte[] data = multiplexer.receive(1); // bloqueia enquanto nao existirem mensagens / erro

                        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));
                        int flag = is.readInt();

                        if(flag == 0){
                            System.out.println("Registo com sucesso!");
                        }
                        else{
                            System.out.println("Username existente!");
                            multiplexer.close();
                        }

                    }  catch (Exception ignored) {}
                }),
        };

        for (Thread t: threads) t.start();
        //for (Thread t: threads) t.join();
        //multiplexer.close();
    }
}
