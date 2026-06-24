package api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ApiGatewayService {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayService.class, args);
    }
}
