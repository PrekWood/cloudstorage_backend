package unipi.cloudstorage.userToken;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.file.exceptions.UserFileNotFound;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.userToken.exceptions.UserTokenNotFound;

import javax.transaction.Transactional;
import java.util.Optional;

@AllArgsConstructor
@Service
@Component
@Transactional
public class UserTokenService {

    private final UserTokenRepository repository;

    public void save(UserToken token){
        repository.save(token);
    }

    public void update(UserToken tokenToUpdate) throws UserTokenNotFound {
        // Search for token
        UserToken existingToken = repository.findById(tokenToUpdate.getToken()).orElse(null);
        if(existingToken == null){
            throw new UserTokenNotFound("token wasn't found");
        }

        existingToken.setToken(tokenToUpdate.getToken());
        existingToken.setValid(tokenToUpdate.isValid());
        existingToken.setUser(tokenToUpdate.getUser());
        save(existingToken);
    }

    public void setUserTokensInvalid(User user){
        repository.setUserTokensInvalid(user.getId());
    }

    public UserToken findById(String token) throws UserTokenNotFound {
        Optional<UserToken> userToken = repository.findById(token);
        if(userToken.isPresent()){
            return userToken.get();
        }
        throw new UserTokenNotFound("Token not found");
    }

}
