package unipi.cloudstorage.sharing.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unipi.cloudstorage.sharing.enums.ShareableType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateShareableLinkRequest {
    private Long objectId;
    private ShareableType type;
}
