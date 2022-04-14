package dhbw.smartmoderation.exceptions;

public class NoGroupMessageTextFoundException extends Exception{

    public NoGroupMessageTextFoundException(Throwable cause, String groupId){
        super("Something went wrong with the Message for the privateGroup : " + groupId,cause);
    }
}
