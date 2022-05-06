package unipi.cloudstorage.user.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class OtpValidationRequest {
    private String otpCode;
}
