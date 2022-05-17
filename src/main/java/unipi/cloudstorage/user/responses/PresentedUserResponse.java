package unipi.cloudstorage.user.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unipi.cloudstorage.countryCodes.CountryCode;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PresentedUserResponse {
    Long id;
    String email;
    String phoneNumber;
    String firstName;
    String lastName;
    CountryCode countryCode;
    boolean phoneValidated;
    String imagePath;
}
