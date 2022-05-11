package unipi.cloudstorage.shared;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.parser.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.requests.RegistrationRequest;
import unipi.cloudstorage.user.responses.PresentedUserResponse;

import java.util.HashMap;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class CloudStorageTest {


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createTestUserAndLogin(MockMvc mvc)  throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        RegistrationRequest registrationRequestBody = new RegistrationRequest(
                String.valueOf(randomNumber)+"@gmail.com",
                String.valueOf(randomNumber)+"_password",
                String.valueOf(randomNumber)+"_first_name",
                String.valueOf(randomNumber)+"_last_name"
        );

        // create new user
        MvcResult registerResult = mvc.perform(
            post("/api/user")
                .content(asJsonString(registrationRequestBody))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isCreated())
        .andReturn();

        String presentedUserString = registerResult.getResponse().getContentAsString();
        PresentedUserResponse presentedUser = new ObjectMapper().readValue(presentedUserString, PresentedUserResponse.class);

        // create new user and login
        String email = registrationRequestBody.getEmail();
        String password = registrationRequestBody.getPassword();
        MvcResult loginResult = mvc.perform(
            post("/api/login?username="+email+"&password="+password)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isOk())
        .andReturn();

        String loginResponseString = loginResult.getResponse().getContentAsString();
        HashMap<String, Object> loginResponse = new ObjectMapper().readValue(loginResponseString, HashMap.class);
        return String.valueOf(loginResponse.get("token"));
    }
}
