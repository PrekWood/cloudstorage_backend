package unipi.cloudstorage.folder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {

    Folder findFirstByUserIdAndParentFolderIsNull(@Param("uid") Long uid);
    List<Folder> findAllByUserIdAndParentFolderId(@Param("uid") Long uid, @Param("fid") Long fid);
    Folder findFirstByUserIdAndId(@Param("uid") Long uid, @Param("fid") Long fid);
}
