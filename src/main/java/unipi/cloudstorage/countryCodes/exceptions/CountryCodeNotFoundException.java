package unipi.cloudstorage.countryCodes.exceptions;

public class CountryCodeNotFoundException extends Exception{
    public CountryCodeNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
