package unipi.cloudstorage.userToken.exceptions;

public class UserTokenNotFound extends Exception{
    public UserTokenNotFound(String errorMessage) {
        super(errorMessage);
    }
    public UserTokenNotFound() {
        super("");
    }
}
