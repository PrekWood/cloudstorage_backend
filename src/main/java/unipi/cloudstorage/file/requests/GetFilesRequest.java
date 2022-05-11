package unipi.cloudstorage.file.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
@ToString
public class GetFilesRequest {
    private String searchQuery;
    private Boolean onlyFavorites;
    private Long folderId;
}
