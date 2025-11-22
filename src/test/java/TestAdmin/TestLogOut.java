package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;


@ExtendWith(ReportExtension.class)
public class TestLogOut extends BaseTest {

	@Test
	void adminEsegueIlLogOut() {

		ExtentTestManager.startTest("Logout Admin");

		try {
			ExtentTestManager.logInfo("Creo admin via API...");
			creaAdminViaApi();

			ExtentTestManager.logInfo("Eseguo login per recuperare il token...");
			String token = loginRecuperaToken();

			ExtentTestManager.logInfo("Token estratto: " + token);

			ExtentTestManager.logInfo("Invio POST /api/auth/logout con Authorization Bearer...");
			given().baseUri("http://localhost:8080").basePath("/api/auth/logout")
					.header("Authorization", "Bearer " + token).when().post().then().statusCode(200)
					.body(equalTo("Logout eseguito"));

			ExtentTestManager.logPass("✔ Logout eseguito correttamente");

		} catch (Exception e) {

			ExtentTestManager.logFail("❌ Errore durante il test di logout: " + e.getMessage());
			throw e;
		}
	}

}
