package TestAdmin;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.*;
import Utils.BaseTest;

public class TestLogOut extends BaseTest {

	@Test
	void adminEsegueIlLogOut() {
		creaAdminViaApi();
		String token = loginRecuperaToken(); // Prendo il token
		System.out.println("token estratto"+token);
		given().baseUri("http://localhost:8080").basePath("/api/auth/logout").header("Authorization", "Bearer " + token)
				.when().post().then().statusCode(200).body(equalTo("Logout eseguito"));

	}

}
