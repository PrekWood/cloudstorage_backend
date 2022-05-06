package unipi.cloudstorage.userToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unipi.cloudstorage.file.UserFile;

import java.util.List;

@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    @Modifying
    @Query("UPDATE UserToken SET valid = false WHERE user.id = :idUser")
    void setUserTokensInvalid(@Param("idUser") Long idUser);
}
