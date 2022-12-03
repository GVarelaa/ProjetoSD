public class Client {
    private String username;
    private String password;

    public Client() {
        this.username = "";
        this.password = "";
    }

    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Client(Client c) {
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
}
