package unipi.cloudstorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import unipi.cloudstorage.user.User;
import unipi.cloudstorage.user.UserRole;
import unipi.cloudstorage.user.UserService;

@SpringBootApplication
public class CloudstorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudstorageApplication.class, args);
	}

	/*@Bean
	public WebMvcConfigurer  corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/registration").allowedOrigins("http://localhost:3000");
			}
		};
	}*/

}
