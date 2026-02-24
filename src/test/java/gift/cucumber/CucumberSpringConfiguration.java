package gift.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("cucumber")
public class CucumberSpringConfiguration {

    @LocalServerPort
    private int port;

    @io.cucumber.java.Before
    public void setUp() {
        RestAssured.port = port;
    }
}
