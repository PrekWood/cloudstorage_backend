package unipi.cloudstorage.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.FileSystemResource;
import unipi.cloudstorage.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    private String filePath;
    private String extension;
    private String fileName;
    private boolean favorite;
    private LocalDateTime dateAdd;

    public String getRootDir(){
        return new FileSystemResource("").getFile().getAbsolutePath();
    }
}
