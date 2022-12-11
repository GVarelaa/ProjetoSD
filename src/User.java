import java.io.*;

public class User {
    private String username;
    private String password;

    public User() {
        this.username = "";
        this.password = "";
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(User c) {
        this.username = c.getUsername();
        this.password = c.getPassword();
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static User deserialize(byte[] data) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(data));

        String username = is.readUTF();
        String password = is.readUTF();

        return new User(username, password);
    }

    public byte[] serialize() throws IOException{
        int size = 8 + this.username.length() + this.password.length();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream(size);
        DataOutputStream os = new DataOutputStream(byteArray);

        os.writeUTF(this.username);
        os.writeUTF(this.password);

        return byteArray.toByteArray();
    }
}
