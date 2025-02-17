package com.example.news;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NewsApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".")
				.filename("SYSTEM32.env")
				.load();

		System.setProperty("spring.datasource.url", dotenv.get("SPRING_DATASOURCE_URL", "jdbc:mysql://localhost:4306/news?useSSL=false&serverTimezone=UTC"));
		System.setProperty("spring.datasource.username", dotenv.get("SPRING_DATASOURCE_USERNAME", "root"));
		System.setProperty("spring.datasource.password", dotenv.get("SPRING_DATASOURCE_PASSWORD", "123456789"));
		System.setProperty("api.prefix", dotenv.get("API_PREFIX", "api/v1"));
		System.setProperty("jwt.secret", dotenv.get("JWT_SECRET", "Z54uiPhveohL/uORp8a8rHhu0qalR4Mj+aIOz5ZA5zY="));
		System.setProperty("jwt.expiration", dotenv.get("JWT_EXPIRATION", "8640000"));
		System.setProperty("cors.allowed.origins", dotenv.get("CORS_ALLOWED_ORIGINS"));

		SpringApplication.run(NewsApplication.class, args);
	}

}
