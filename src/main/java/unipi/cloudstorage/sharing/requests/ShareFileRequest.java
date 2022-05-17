package unipi.cloudstorage.sharing.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import unipi.cloudstorage.sharing.enums.ShareableType;

import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShareFileRequest {
    private Long objectId;
    private ShareableType type;
    private List<HashMap<String, Object>> users;
}
