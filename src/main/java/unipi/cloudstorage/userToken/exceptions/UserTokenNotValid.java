package unipi.cloudstorage.userToken.exceptions;

public class UserTokenNotValid extends Exception{
    public UserTokenNotValid(String errorMessage) {
        super(errorMessage);
    }
    public UserTokenNotValid() {
        super("");
    }
}
