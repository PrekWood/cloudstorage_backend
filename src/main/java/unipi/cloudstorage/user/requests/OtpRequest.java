package unipi.cloudstorage.user.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class OtpRequest {
    private String phoneNumber;
    private Long idCountryCode;
}
