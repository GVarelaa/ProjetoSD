package Exceptions;

public class NonExistantUsernameException extends Exception{
    public NonExistantUsernameException(String msg){
        super(msg);
    }
}
