package unipi.cloudstorage.folder;

import lombok.*;
import unipi.cloudstorage.file.UserFile;
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
public class Folder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDateTime dateAdd;
    @ManyToOne
    private User user;
    @ManyToOne
    private Folder parentFolder;

    @Transient
    private List<HashMap<String, Object>> breadcrumb;
    @Transient
    private List<UserFile> files;
    @Transient
    private List<Folder> subfolders;
}
