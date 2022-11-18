package Models;

public class CustomException extends Exception{
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }
}
