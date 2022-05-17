package unipi.cloudstorage.userWithPrivileges;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.sharing.Sharable;
import unipi.cloudstorage.sharing.enums.FilePrivileges;
import unipi.cloudstorage.user.User;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
public class UserWithPrivileges {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @ManyToOne
    private Sharable sharable;
    private FilePrivileges privileges;


    @Transient
    private Boolean owner;
}
