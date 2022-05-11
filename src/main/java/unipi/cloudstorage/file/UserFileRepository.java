package unipi.cloudstorage.file;

import org.springframework.data.domain.Sort;
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

    List<UserFile> findAllByUserId(
            @Param("uid") Long uid,
            @Param("s") Sort s
    );

    List<UserFile> findAllByUserIdAndFolderId(
            @Param("uid") Long uid,
            @Param("fid") Long fid,
            @Param("s") Sort s
    );

    List<UserFile> findAllByUserIdAndFavorite(
        @Param("uid") Long uid,
        @Param("fav") Boolean fav,
        @Param("s") Sort s
    );

    List<UserFile> findAllByUserIdAndFolderIdAndFavorite(
        @Param("uid") Long uid,
        @Param("fid") Long fid,
        @Param("fav") Boolean fav,
        @Param("s") Sort s
    );

    List<UserFile> findAllByUserIdAndFavoriteIsTrue(
        @Param("uid") Long uid,
        @Param("s") Sort s
    );

    List<UserFile> findAllByUserIdAndFileNameContains(
        @Param("uid") Long uid,
        @Param("s") String search,
        @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFolderIdAndFileNameContains(
        @Param("uid") Long uid,
        @Param("fid") Long fid,
        @Param("s") String search,
        @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFavoriteAndFileNameContains(
        @Param("uid") Long uid,
        @Param("fav") Boolean fav,
        @Param("s") String search,
        @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFolderIdAndFavoriteAndFileNameContains(
        @Param("uid") Long uid,
        @Param("fid") Long fid,
        @Param("fav") Boolean fav,
        @Param("s") String search,
        @Param("s") Sort sort
    );
}
