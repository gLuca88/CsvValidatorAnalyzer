package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;

import Model.LoginInvalidCase;
import ReportConfig.ExtentTestManager;
import Utils.BaseTest;
import Utils.CreaJson;
import Utils.JsonUtils;
import org.junit.jupiter.api.extension.ExtendWith;
import ReportConfig.ReportExtension;

@ExtendWith(ReportExtension.class)
public class TestLoginAdmin extends BaseTest {

	@Test
	void loginAdminCorretto() {

		ExtentTestManager.startTest("Login Admin Corretto");

		try {
			ExtentTestManager.logInfo("Creo admin via API...");
			creaAdminViaApi();

			ExtentTestManager.logInfo("Invio richiesta di login...");
			String loginJson = CreaJson.creaJsonAccesso("admin", "adminpass");

			given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
					.body(loginJson).when().post().then().statusCode(200).body("token", notNullValue());

			ExtentTestManager.logPass("✔ Login admin corretto verificato");

		} catch (Exception e) {
			ExtentTestManager.logFail("❌ Errore durante il test login valido: " + e.getMessage());
			throw e;
		}
	}

	static Stream<LoginInvalidCase> provider() {
		List<LoginInvalidCase> list = JsonUtils.readJson("loginInvalidCases.json",
				new TypeReference<List<LoginInvalidCase>>() {
				});
		return list.stream();
	}

	@ParameterizedTest
	@MethodSource("provider")
	void loginErrato(LoginInvalidCase caso) {

		ExtentTestManager.startTest("Login errato → " + caso.getDescrizione());

		try {
			ExtentTestManager.logInfo("Creo admin via API...");
			creaAdminViaApi();

			ExtentTestManager
					.logInfo("Creo JSON con username=" + caso.getUsername() + ", password=" + caso.getPassword());

			String json = CreaJson.creaJsonAccesso(caso.getUsername(), caso.getPassword());

			ExtentTestManager.logInfo("Invio POST /login...");

			given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
					.body(json).when().post().then().statusCode(401).body(equalTo("Credenziali non valide"));

			ExtentTestManager.logPass("✔ RISULTATO OK: " + caso.getDescrizione());

		} catch (Exception e) {
			ExtentTestManager
					.logFail("❌ Errore nel test login errato (" + caso.getDescrizione() + "): " + e.getMessage());
			throw e;
		}
	}

}
