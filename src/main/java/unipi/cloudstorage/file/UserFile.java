package unipi.cloudstorage.file;

import lombok.*;
import org.springframework.core.io.FileSystemResource;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.sharing.Sharable;
import unipi.cloudstorage.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserFile extends Sharable {
    private String filePath;
    private String extension;
    private boolean favorite;
}
