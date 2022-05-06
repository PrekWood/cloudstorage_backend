package unipi.cloudstorage.file;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unipi.cloudstorage.file.enums.UserFileField;
import unipi.cloudstorage.otp.Otp;
import unipi.cloudstorage.shared.enums.OrderWay;
import unipi.cloudstorage.user.User;

import java.util.List;

@Repository
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    // Get files by user id
    List<UserFile> findAllByUserId(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdOrderByDateAddDesc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdOrderByDateAddAsc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdOrderByFileNameDesc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdOrderByFileNameAsc(@Param("uid") Long uid);

    // Get favorite files by user id
    List<UserFile> findAllByUserIdAndFavoriteIsTrue(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueOrderByDateAddDesc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueOrderByDateAddAsc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueOrderByFileNameDesc(@Param("uid") Long uid);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueOrderByFileNameAsc(@Param("uid") Long uid);

    // Search for files by name given a user
    List<UserFile> findAllByUserIdAndFileNameContains(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFileNameContainsOrderByDateAddDesc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFileNameContainsOrderByDateAddAsc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFileNameContainsOrderByFileNameDesc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFileNameContainsOrderByFileNameAsc(@Param("uid") Long uid, @Param("s") String s);

    // Search for favorite files by name given a user
    List<UserFile> findAllByUserIdAndFavoriteIsTrueAndFileNameContains(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByDateAddDesc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByDateAddAsc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByFileNameDesc(@Param("uid") Long uid, @Param("s") String s);
    List<UserFile> findAllByUserIdAndFavoriteIsTrueAndFileNameContainsOrderByFileNameAsc(@Param("uid") Long uid, @Param("s") String s);
}
