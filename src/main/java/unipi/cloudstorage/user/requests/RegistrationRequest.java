package unipi.cloudstorage.user.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class RegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
