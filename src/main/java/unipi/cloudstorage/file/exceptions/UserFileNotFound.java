package unipi.cloudstorage.file.exceptions;

public class UserFileNotFound extends Exception{
    public UserFileNotFound(String errorMessage) {
        super(errorMessage);
    }
    public UserFileNotFound() {
        super("");
    }
}
