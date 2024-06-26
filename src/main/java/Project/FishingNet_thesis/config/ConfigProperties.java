package Project.FishingNet_thesis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config.properties")
@Data
public class ConfigProperties {
    @Value("${app.FEbaseurl}")
    private String FEbaseUrl;
    @Value("${app.jwtsecret}")
    private String jwtSecret;

}
