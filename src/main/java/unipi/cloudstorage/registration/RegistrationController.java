package unipi.cloudstorage.registration;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "registration")
@AllArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping
    public String register(@RequestBody RegistrationRequest request) {
        System.out.println("register");

        return registrationService.register(request);
    }

    @GetMapping(path = "confirm")
    public String confirm() {
        //@RequestParam("token") String token
        return "confirm";//registrationService.confirmToken(token);
    }

}
