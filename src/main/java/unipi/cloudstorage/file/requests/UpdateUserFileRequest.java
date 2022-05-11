package unipi.cloudstorage.file.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class UpdateUserFileRequest {
    String fileName;
    Boolean favorite;
}


