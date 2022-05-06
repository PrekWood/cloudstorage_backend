package unipi.cloudstorage.userToken;

import lombok.Getter;
import lombok.Setter;
import unipi.cloudstorage.user.User;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class UserToken {
    @Id
    String token;
    @ManyToOne
    User user;
    boolean valid = true;
}
