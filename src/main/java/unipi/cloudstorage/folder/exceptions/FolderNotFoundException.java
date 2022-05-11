package unipi.cloudstorage.folder.exceptions;

public class FolderNotFoundException extends Exception{
    public FolderNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
