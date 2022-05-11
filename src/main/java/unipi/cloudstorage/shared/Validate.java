package unipi.cloudstorage.shared;

public class Validate {
    public static boolean isEmpty(String text){
        return text == null || text.equals("");
    }
    public static boolean isEmpty(Boolean text){
        return text == null;
    }
}
