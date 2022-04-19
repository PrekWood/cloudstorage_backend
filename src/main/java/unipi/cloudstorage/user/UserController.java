package unipi.cloudstorage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("all")
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }


}
