package unipi.cloudstorage.user.requests;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
