package TestEmploye;

import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class LoginEmployeTestValido {

	@Test
	void testLoginEmployee() {
		String body = """
				{
				    "username": "felice",
				    "password": "pippo"
				}
				""";
		
		 given()
	        .baseUri("http://localhost:8080")
	        .basePath("/api/auth/login")
	        .contentType("application/json")
	        .body(body)
	    .when()
	        .post()
	    .then()
	        .statusCode(200)
	        .body("token", notNullValue());
		
	}

}
