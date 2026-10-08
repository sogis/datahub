package ch.so.agi.datahub;

import ch.so.agi.datahub.config.AppProperties;
import java.time.Clock;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class DatahubApplication {
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(DatahubApplication.class);
    if (java.util.Arrays.stream(args).anyMatch(a -> a.equals("--app.mode=worker-single"))) {
      app.setWebApplicationType(WebApplicationType.NONE);
    }
    var context = app.run(args);
    if (context.getBean(AppProperties.class).getMode().equals("worker-single")) {
      System.exit(SpringApplication.exit(context));
    }
  }

  @Bean
  Clock clock(AppProperties p) {
    return Clock.system(java.time.ZoneId.of(p.getTimeZone()));
  }

  @Bean
  PasswordEncoder encoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  ResourceBundle resourceBundle() {
    return ResourceBundle.getBundle("DatahubMessages", Locale.of("de", "CH"));
  }
}
