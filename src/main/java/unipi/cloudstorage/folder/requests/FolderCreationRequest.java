package unipi.cloudstorage.folder.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FolderCreationRequest {
    private String name;
    private Long parentFolderId;
}
