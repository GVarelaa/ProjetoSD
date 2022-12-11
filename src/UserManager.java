import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class UserManager {
    private HashMap<String, User> users;
    private ReentrantLock lock;

    public UserManager(){
        this.users = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void register(String username, String password){
        try{
            this.lock.lock();

            if(this.users.containsKey(username)){
                throw new UsernameAlreadyExistsException();
            }

            User newUser = new User(username, password);
            this.users.put(username, newUser);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    public boolean login(String username, String password){
        try{
            this.lock.lock();

            if(!this.users.containsKey(username)){
                throw new NonExistantUsernameException();
            }

            User user = this.users.get(username);

            return user.getUsername().equals(username) && user.getPassword().equals(password);
        }
        finally {
            this.lock.unlock();
        }

    }
}
