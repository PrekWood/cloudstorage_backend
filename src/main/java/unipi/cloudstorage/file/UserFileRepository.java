package unipi.cloudstorage.file;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unipi.cloudstorage.otp.Otp;
import unipi.cloudstorage.shared.enums.OrderWay;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.userWithPrivileges.UserWithPrivileges;

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

    List<UserFile> findAllByUserIdAndNameContains(
            @Param("uid") Long uid,
            @Param("s") String search,
            @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFolderIdAndNameContains(
            @Param("uid") Long uid,
            @Param("fid") Long fid,
            @Param("s") String search,
            @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFavoriteAndNameContains(
            @Param("uid") Long uid,
            @Param("fav") Boolean fav,
            @Param("s") String search,
            @Param("s") Sort sort
    );

    List<UserFile> findAllByUserIdAndFolderIdAndFavoriteAndNameContains(
            @Param("uid") Long uid,
            @Param("fid") Long fid,
            @Param("fav") Boolean fav,
            @Param("s") String search,
            @Param("s") Sort sort
    );


    @Query("""
        SELECT file
        FROM UserFile file 
        JOIN file.sharedWith sharedWith 
        WHERE sharedWith.user.id = :uid
    """)
    List<UserFile> searchForSharedFiles(
            @Param("uid") Long uid,
            @Param("s") Sort sort
    );

    @Query("""
        SELECT file
        FROM UserFile file
        WHERE file.folder.id = :folderId
    """)
    List<UserFile> getFilesOfSharedFolder(
            @Param("folderId") Long folderId,
            @Param("s") Sort sort
    );

    @Query("""
        SELECT file
        FROM UserFile file
        JOIN file.sharedWith sharedWith
        WHERE sharedWith.user.id = :uid
        AND file.name LIKE  %:searchQuery%
    """)
    List<UserFile> searchForSharedFiles(
            @Param("uid") Long uid,
            @Param("searchQuery") String searchQuery,
            @Param("s") Sort sort
    );

    @Query(value = """
        SELECT sharable.sharedWith
        FROM Sharable sharable
        WHERE sharable.id = :shareableId
    """)
    List<UserWithPrivileges> loadSharedWith(
        @Param("shareableId") Long shareableId
    );


}
