package unipi.cloudstorage.user.exceptions;

public class EmailAlreadyBeingUsedUserException extends Exception{
    public EmailAlreadyBeingUsedUserException(String errorMessage) {
        super(errorMessage);
    }
}
