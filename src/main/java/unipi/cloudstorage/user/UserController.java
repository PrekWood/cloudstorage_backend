package unipi.cloudstorage.user;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unipi.cloudstorage.shared.ResponseHandler;
import java.util.HashMap;

@RestController
@AllArgsConstructor
@RequestMapping(path = "api/")
public class UserController extends ResponseHandler {

    private final UserService userService;

    @PostMapping("registration/is-loged-in")
    public ResponseEntity<?> isLogedIn(){
        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if(logedInUser == null || !logedInUser.isPhoneValidated()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body((new HashMap<>()).put("error", "You are not loged in"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED).body("");
    }

    @GetMapping("user")
    public ResponseEntity<?> getUserDetails(){
        // Search for user
        User logedInUser = userService.loadUserFromJwt();
        if(logedInUser == null || !logedInUser.isPhoneValidated()){
            return createErrorResponse(HttpStatus.FORBIDDEN,"You are not loged in");
        }

        return createSuccessResponse(HttpStatus.ACCEPTED,userService.present(logedInUser));
    }

}

