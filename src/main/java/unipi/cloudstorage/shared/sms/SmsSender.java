package unipi.cloudstorage.shared.sms;

import java.io.IOException;

public interface SmsSender {
    public void sendSms(String countryCodePrefix, String phoneNumber, String pinCode) throws IOException;
}
