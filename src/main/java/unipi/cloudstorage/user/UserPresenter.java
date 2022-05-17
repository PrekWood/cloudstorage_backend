package unipi.cloudstorage.user;

import org.springframework.stereotype.Component;
import unipi.cloudstorage.file.UserFile;
import unipi.cloudstorage.shared.ModelPresenter;
import unipi.cloudstorage.user.responses.PresentedUserResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class UserPresenter implements ModelPresenter<User> {

    @Override
    public PresentedUserResponse present(User user) {
        PresentedUserResponse response = new PresentedUserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCountryCode(user.getCountryCode());
        response.setPhoneValidated(user.isPhoneValidated());
        response.setImagePath(user.getImagePath());
        return response;
    }

    @Override
    public List<PresentedUserResponse> presentMultiple(List<User> userList) {
        List<PresentedUserResponse> userListToReturn = new ArrayList<>();
        for (User user : userList) {
            userListToReturn.add(present(user));
        }
        return userListToReturn;
    }

}


