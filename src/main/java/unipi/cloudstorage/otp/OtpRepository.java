package unipi.cloudstorage.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query(
        value = """
            SELECT *
            FROM otp
            WHERE user_id = ?1
            ORDER BY id DESC
            LIMIT 1;
        """,
        nativeQuery = true
    )
    Otp getLastOtpOfUser(Long idUser);
}
