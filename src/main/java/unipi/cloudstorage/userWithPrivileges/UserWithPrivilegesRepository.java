package unipi.cloudstorage.userWithPrivileges;

import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserWithPrivilegesRepository extends JpaRepository<UserWithPrivileges,Long> {

    @Modifying
    @Transactional
    @Query(value = """
       DELETE
       FROM sharable_shared_with
       WHERE sharable_id = :idFile
    """, nativeQuery = true)
    void deleteSharableSharedWithRecords(
        @Param("idFile") Long idFile
    );

    List<UserWithPrivileges> findAllBySharableId(@Param("idSharable") Long idSharable);
}
