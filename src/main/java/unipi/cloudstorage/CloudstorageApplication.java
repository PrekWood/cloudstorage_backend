package unipi.cloudstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserRole;
import unipi.cloudstorage.user.UserService;

@SpringBootApplication
public class CloudstorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudstorageApplication.class, args);
	}

	@Autowired
	UserService userService;

	/*@Bean
	public void setUsers(){
		System.out.println("setUsers");

		userService.signUpUser(new User(
				"nikosprek2000@gmail.com",
				"nikosprek2000",
				"6986674566",
				"Nikos",
				"Prekas",
				UserRole.USER,
				true
		));
		userService.signUpUser(new User(
				"nikosprek2001@gmail.com",
				"nikosprek2001",
				"6986674566",
				"Nikos",
				"Prekas",
				UserRole.USER,
				true
		));

	}*/
}
