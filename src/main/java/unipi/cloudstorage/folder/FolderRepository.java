package unipi.cloudstorage.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Folder findFirstByUserIdAndFolderIsNull(@Param("uid") Long uid);

    List<Folder> findAllByUserIdAndFolderId(@Param("uid") Long uid, @Param("fid") Long fid);
    List<Folder> findAllByFolderId(@Param("fid") Long fid);

    Folder findFirstByUserIdAndId(@Param("uid") Long uid, @Param("fid") Long fid);

    @Query("""
        SELECT folder
        FROM Folder folder
        WHERE folder.user.id = :uid
        AND folder.folder.id = :fid
        AND folder.name LIKE %:nameLike%
    """)
    List<Folder> searchForSubfolders(
            @Param("uid") Long uid,
            @Param("fid") Long fid,
            @Param("nameLike") String nameLike
    );


    @Query("""
        SELECT folder
        FROM Folder folder
        JOIN folder.sharedWith sharedWith
        WHERE sharedWith.user.id = :uid
    """)
    List<Folder> getSharedFolders(@Param("uid") Long idUser);

    @Query("""
        SELECT folder
        FROM Folder folder
        JOIN folder.sharedWith sharedWith
        WHERE sharedWith.user.id = :uid
        AND folder.name like %:searchQuery%
    """)
    List<Folder> searchForSharedFolders(
            @Param("uid") Long idUser,
            @Param("searchQuery") String searchQuery
    );

}
