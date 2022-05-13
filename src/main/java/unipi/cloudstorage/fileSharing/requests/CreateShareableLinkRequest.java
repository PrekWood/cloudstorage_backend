package unipi.cloudstorage.fileSharing.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unipi.cloudstorage.fileSharing.enums.ShareableType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateShareableLinkRequest {
    private Long objectId;
    private ShareableType type;
}
