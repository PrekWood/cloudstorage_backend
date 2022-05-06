package unipi.cloudstorage.otp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.user.User;

@Service
@AllArgsConstructor
public class OtpService {
    private final OtpRepository otpRepository;

    public void save(Otp otp){
        otpRepository.save(otp);
    }

    public Otp getLastOtpOfUser(User user){
        return otpRepository.getLastOtpOfUser(user.getId());
    }
}
