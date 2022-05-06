package unipi.cloudstorage.file.requests;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class FileUploadRequest {
//    private String localFilePath;
    private MultipartFile file;
}
