package unipi.cloudstorage.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import unipi.cloudstorage.user.requests.RegistrationRequest;

import java.util.HashMap;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    //    @WithMockUser(value = "spring")
    @Test
    void isTriesToRegisterWithoutBody() throws Exception {
        mvc.perform(post("/api/registration"))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void isTriesToRegisterCorrectly() throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        mvc.perform(
                post("/api/registration")
                    .content(asJsonString(new RegistrationRequest(
                        String.valueOf(randomNumber)+"@gmail.com",
                        String.valueOf(randomNumber)+"_password",
                        String.valueOf(randomNumber)+"_first_name",
                        String.valueOf(randomNumber)+"_last_name"
                    )))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    void isTriesToRegisterWithoutField() throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("email",String.valueOf(randomNumber)+"@gmail.com");
        //requestBody.put("password",String.valueOf(randomNumber)+"_password");
        requestBody.put("firstName",String.valueOf(randomNumber)+"_firstName");
        requestBody.put("lastName",String.valueOf(randomNumber)+"_lastName");

        mvc.perform(
            post("/api/registration")
                .content(asJsonString(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isBadRequest());
    }

    @Test
    void isTriesToRegisterWithEmptyField() throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("email",String.valueOf(randomNumber)+"@gmail.com");
        requestBody.put("password",String.valueOf(randomNumber)+"_password");
        requestBody.put("firstName",String.valueOf(randomNumber)+"_firstName");
        requestBody.put("lastName",""); // Should not be empty

        mvc.perform(
            post("/api/registration")
                .content(asJsonString(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isBadRequest());
    }


    @Test
    void isTriesToRegisterWithNullField() throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        HashMap<String, String> requestBody = new HashMap<>();
        requestBody.put("email",String.valueOf(randomNumber)+"@gmail.com");
        requestBody.put("password",String.valueOf(randomNumber)+"_password");
        requestBody.put("firstName",String.valueOf(randomNumber)+"_firstName");
        requestBody.put("lastName",null); // Should not be empty

        mvc.perform(
            post("/api/registration")
                .content(asJsonString(requestBody))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isBadRequest());
    }


    @Test
    void isTriesToRegisterWithAlreadyInsertedEmail() throws Exception {
        int randomNumber = new Random().nextInt(1000000);

        // Create new user
        mvc.perform(
            post("/api/registration")
                .content(asJsonString(new RegistrationRequest(
                        String.valueOf(randomNumber)+"@gmail.com",
                        String.valueOf(randomNumber)+"_password",
                        String.valueOf(randomNumber)+"_first_name",
                        "_last_name"
                )))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isCreated());

        // try to create new user with the same email
        mvc.perform(
            post("/api/registration")
                .content(asJsonString(new RegistrationRequest(
                        String.valueOf(randomNumber)+"@gmail.com",
                        String.valueOf(randomNumber)+"_password2",
                        String.valueOf(randomNumber)+"_first_name2",
                        String.valueOf(randomNumber)+"_last_name2"
                )))
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andDo(print())
        .andExpect(status().isConflict());

    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}