package unipi.cloudstorage.user;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.otp.Otp;
import unipi.cloudstorage.otp.OtpRepository;
import unipi.cloudstorage.user.exceptions.EmailAlreadyBeingUsedUserException;
import unipi.cloudstorage.user.exceptions.UserNotFoundException;
import unipi.cloudstorage.user.responses.PresentedUserResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final OtpRepository otpRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserPresenter presenter;

    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(user == null) {
            throw new UsernameNotFoundException(String.format("user with email %s not found", email));
        }
        return user;
    }

    public void signUpUser(User user) throws EmailAlreadyBeingUsedUserException {
        boolean userExists = userRepository.findByEmail(user.getEmail()) != null;

        if (userExists) {
            throw new EmailAlreadyBeingUsedUserException("email already taken");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    public void updateUser(User userToUpdate) throws UserNotFoundException {
        // Search for user
        User existingUser = userRepository.findById(userToUpdate.getId()).orElse(null);
        if(existingUser == null){
            throw new UserNotFoundException("User was not found while trying to update user");
        }

        existingUser.setEmail(userToUpdate.getEmail());
        existingUser.setFirstName(userToUpdate.getFirstName());
        existingUser.setLastName(userToUpdate.getLastName());
        existingUser.setPhoneNumber(userToUpdate.getPhoneNumber());
        existingUser.setPhoneValidated(userToUpdate.isPhoneValidated());
        existingUser.setImagePath(userToUpdate.getImagePath());
        userRepository.save(existingUser);
    }


    public User loadUserFromJwt(){
        Object loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(loggedInUserEmail == null || loggedInUserEmail.equals("")){
            return null;
        }
        return this.loadUserByUsername((String)loggedInUserEmail);
    }

    public PresentedUserResponse present(User user){
        return presenter.present(user);
    }

    public User getUserById(Long userId) throws UserNotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            throw new UserNotFoundException("Invalid user id");
        }
        return user.get();
    }


}
