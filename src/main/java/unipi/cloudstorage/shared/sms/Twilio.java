package unipi.cloudstorage.shared.sms;

import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class Twilio implements SmsSender{

    private static final String MessagingServiceSid = "MGfe6b5fdaba1af340a9737c4fe93e67ea";
    private static final String BasicAuthToken = "QUM1YjQxNzMxYmY5YTIxM2IyY2MwYTlhNmUzYjRhZjE5ODoyMTViODZjNGZiZjg5MTMxZTVjNTQzYjE4ZDYxMTZmYw==";

    public void sendSms(String countryCodePrefix, String phoneNumber, String pinCode) throws IOException {

        // Build body
        StringBuilder requestBodyBuilder = new StringBuilder();
        requestBodyBuilder.append("To=");
        requestBodyBuilder.append(countryCodePrefix);
        requestBodyBuilder.append(phoneNumber);
        requestBodyBuilder.append("&From=CloudStrg");
        requestBodyBuilder.append("&MessagingServiceSid=");
        requestBodyBuilder.append(MessagingServiceSid);
        requestBodyBuilder.append("&Body=");
        requestBodyBuilder.append("Your cloudstorage verification code is: ");
        requestBodyBuilder.append(pinCode);

        // Make call to twilio
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, requestBodyBuilder.toString());
        Request request = new Request.Builder()
                .url("https://api.twilio.com/2010-04-01/Accounts/AC5b41731bf9a213b2cc0a9a6e3b4af198/Messages.json")
                .method("POST", body)
                .addHeader("Authorization", "Basic "+BasicAuthToken)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        Response response = client.newCall(request).execute();

        // throw exception if not 201
        if(response.code() != 201){
            throw new IOException("The SMS could not be sent successfully");
        }
    }


}
