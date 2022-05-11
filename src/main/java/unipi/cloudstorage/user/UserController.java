package unipi.cloudstorage.user;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unipi.cloudstorage.folder.Folder;
import unipi.cloudstorage.folder.FolderService;
import unipi.cloudstorage.shared.ResponseHandler;
import unipi.cloudstorage.user.exceptions.EmailAlreadyBeingUsedUserException;
import unipi.cloudstorage.user.requests.RegistrationRequest;
import unipi.cloudstorage.user.responses.PresentedUserResponse;

import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/")
public class UserController extends ResponseHandler {

    private final UserService userService;
    private final FolderService folderService;

    @PostMapping("registration/is-loged-in")
    public ResponseEntity<?> isLoggedIn(){
        // Search for user
        User loggedInUser = userService.loadUserFromJwt();
        if(loggedInUser == null || !loggedInUser.isPhoneValidated()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((new HashMap<>()).put("error", "You are not loged in"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("");
    }

    @GetMapping("user")
    public ResponseEntity<?> getUserDetails(){
        // Search for user
        User loggedInUser = userService.loadUserFromJwt();
        if(loggedInUser == null || !loggedInUser.isPhoneValidated()){
            return createErrorResponse(HttpStatus.FORBIDDEN,"You are not loged in");
        }

        return createSuccessResponse(HttpStatus.ACCEPTED,userService.present(loggedInUser));
    }

    @PostMapping("user" )
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {

        // Check for empty fields
        if (
            request.getEmail() == null || request.getEmail().equals("" ) ||
            request.getPassword() == null || request.getPassword().equals("" ) ||
            request.getFirstName() == null || request.getFirstName().equals("" ) ||
            request.getLastName() == null || request.getLastName().equals("" )
        ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((new HashMap<>()).put("error", "Please fill in all the nesessary fields" ));
        }

        // Create new object
        User newUser = new User(
            request.getEmail(),
            request.getPassword(),
            null,
            request.getFirstName(),
            request.getLastName(),
            UserRole.USER,
            false
        );

        // Try to sign in
        try {
            userService.signUpUser(newUser);
        } catch (EmailAlreadyBeingUsedUserException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body((new HashMap<>()).put("error", "E-mail already exists" ));
        }

        // Create user root folder
        Folder rootFolder = new Folder();
        rootFolder.setUser(newUser);
        rootFolder.setName("Home");
        rootFolder.setParentFolder(null);
        folderService.save(rootFolder);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.present(newUser));
    }

}

