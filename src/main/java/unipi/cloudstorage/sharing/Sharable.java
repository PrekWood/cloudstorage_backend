package unipi.cloudstorage.sharing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Sharable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @OneToMany(fetch = FetchType.EAGER)
    private List<UserWithPrivileges> sharedWith;
    @ManyToOne
    private Folder folder;
    private LocalDateTime dateAdd;
    private String name;
}

