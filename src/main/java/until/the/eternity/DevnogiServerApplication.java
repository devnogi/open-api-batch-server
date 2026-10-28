package until.the.eternity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import until.the.eternity.config.openapi.OpenApiWebClientProperties;

@EnableScheduling
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties(OpenApiWebClientProperties.class)
public class DevnogiServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DevnogiServerApplication.class, args);
    }
}
