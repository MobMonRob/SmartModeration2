package dhbw.smartmoderation.exceptions;

public class NoGroupHeadersFoundException extends Exception {

    public NoGroupHeadersFoundException(Throwable cause,String groupId){
        super("Something went wrong with the Messageheaders for the privateGroup : " + groupId,cause);
    }
}
