import Exceptions.NonExistantUsernameException;
import Exceptions.UsernameAlreadyExistsException;

public interface ISharedState {
    void register(String username, String password) throws UsernameAlreadyExistsException;
    boolean login(String username, String password) throws NonExistantUsernameException;
}
