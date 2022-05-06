package unipi.cloudstorage.user;

import org.springframework.stereotype.Component;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.shared.ModelPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class UserPresenter implements ModelPresenter<User> {

    @Override
    public HashMap<String, Object> present(User user) {
        HashMap<String, Object> userToReturn = new HashMap<>();
        userToReturn.put("id", user.getId());
        userToReturn.put("email", user.getEmail());
        userToReturn.put("phoneNumber", user.getPhoneNumber());
        userToReturn.put("firstName", user.getFirstName());
        userToReturn.put("lastName", user.getLastName());
        userToReturn.put("countryCode", user.getCountryCode());
        userToReturn.put("phoneValidated", user.isPhoneValidated());
        return userToReturn;
    }

    @Override
    public List<HashMap<String, Object>> presentMultiple(List<User> userList) {
        List<HashMap<String, Object>> userListToReturn = new ArrayList<>();
        for (User user : userList) {
            userListToReturn.add(present(user));
        }
        return userListToReturn;
    }
}
