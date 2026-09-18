package hu.ithink.mq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EntityScan("hu.ithink.mq.entities")
//@EnableJpaRepositories(basePackages = "hu.ithink.mq.repositories")
public class iThinkMqWebApplication {

  public static void main(String[] args) throws IOException {
    Files.createDirectories(Path.of("data"));
    SpringApplication.run(iThinkMqWebApplication.class, args);
  }

}
