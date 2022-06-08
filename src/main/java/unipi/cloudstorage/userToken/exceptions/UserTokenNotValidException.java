package unipi.cloudstorage.userToken.exceptions;

public class UserTokenNotValidException extends Exception{
    public UserTokenNotValidException(String errorMessage) {
        super(errorMessage);
    }
    public UserTokenNotValidException() {
        super("");
    }
}
