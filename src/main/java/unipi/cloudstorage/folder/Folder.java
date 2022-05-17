package unipi.cloudstorage.folder;

import lombok.*;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.sharing.Sharable;
import unipi.cloudstorage.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Folder extends Sharable {
    // Fields that are not written in the database
    public static final Long ROOT_FOLDER = Long.valueOf(-1);
    @Transient
    private List<HashMap<String, Object>> breadcrumb;
    @Transient
    private List<UserFile> files;
    @Transient
    private List<Folder> subfolders;
}
