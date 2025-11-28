package TestAdmin;

import static org.hamcrest.CoreMatchers.equalTo;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;
import static io.restassured.RestAssured.given;
import static Utils.ApiTestUtils.*;

@ExtendWith(ReportExtension.class)
public class TestAdminProtected_EndPoint_IsProtectedAdmin extends BaseTest {

	@Test
	void adminProtetto_RitornaTrue() throws SQLException {
		ExtentTestManager.startTest("IPA-001 → Admin protetto → restituisce TRUE → 200 OK");

		ExtentTestManager.logInfo("Registro admin via API e lo marco come PROTETTO nel DB...");
		creaAdminViaApiProtetto(); // crea admin protetto a true

		ExtentTestManager.logInfo("Eseguo login come ADMIN...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");

		ExtentTestManager.logInfo("Invio GET a /api/admin-profile/is-protected-admin con token ADMIN...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin).when()
				.get("/api/admin-profile/is-protected-admin").then().statusCode(200).body(equalTo("true"));

		ExtentTestManager.logPass("✔ Admin protetto → l’API risponde TRUE come previsto (200 OK)");

	}

	@Test
	void employee_RotornaFalse() {
		ExtentTestManager.startTest("IPA-002 → Employee → restituisce FALSE → 200 OK");
		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Registro employee via API...");
		creaEmployeeViaApi();
		ExtentTestManager.logInfo("Effettuo login come EMPLOYEE...");
		String tokenEmployee = loginRecuperaToken("employee1", "emppass");
		ExtentTestManager.logInfo("Token employee: " + tokenEmployee);
		ExtentTestManager.logInfo("Invio GET a /api/admin-profile/is-protected-admin con token EMPLOYEE...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenEmployee).when()
				.get("/api/admin-profile/is-protected-admin").then().statusCode(200).body(equalTo("false"));
		ExtentTestManager.logPass("✔ Employee → l’API risponde FALSE come previsto (200 OK)");
	}

	@Test
	void richiestaSenzaToken_Unauthorized() {
		ExtentTestManager.startTest("IPA-003 → Richiesta senza token → 401 Unauthorized");

		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Invio GET SENZA Authorization header...");

		given().baseUri("http://localhost:8080").when().get("/api/admin-profile/is-protected-admin").then()
				.statusCode(401); // ← Aspettato

		ExtentTestManager.logPass("✔ Senza token → 401 Unauthorized come previsto");
	}

	@Test
	void tokenManipolato_Unauthorized() {

		ExtentTestManager.startTest("IPA-004 → Token manipolato → 401 Unauthorized");

		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Eseguo login come ADMIN...");
		String tokenValido = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token valido: " + tokenValido);
		ExtentTestManager.logInfo("Genero TOKEN MANIPOLATO (corruzione della firma)...");
		// Altero le ultime lettere del token
		String tokenManipolato = tokenValido.substring(0, tokenValido.length() - 5) + "ABCDE";
		ExtentTestManager.logInfo("Token manipolato: " + tokenManipolato);

		ExtentTestManager.logInfo("Invio GET a /api/admin-profile/is-protected-admin con token manipolato...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenManipolato).when()
				.get("/api/admin-profile/is-protected-admin").then().statusCode(401);
		ExtentTestManager.logPass("✔ Token manipolato rifiutato correttamente → 401 Unauthorized");
	}

	@Test
	void tokenScaduto_Unauthorized() {
		ExtentTestManager.startTest("IPA-005 → Token scaduto → 401 Unauthorized");
		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Genero TOKEN SCADUTO...");
		String tokenScaduto = generaTokenScaduto();
		ExtentTestManager.logInfo("Token scaduto: " + tokenScaduto);
		ExtentTestManager.logInfo("Invio GET a /api/admin-profile/is-protected-admin con token SCADUTO...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenScaduto).when()
				.get("/api/admin-profile/is-protected-admin").then().statusCode(401);
		ExtentTestManager.logPass("✔ Token scaduto rifiutato correttamente → 401 Unauthorized");
	}

	@Test
	void headerSenzaBearer_Unauthorized() {
		ExtentTestManager.startTest("IPA-006 → Authorization senza 'Bearer' → 401 Unauthorized");
		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Effettuo login per ottenere token valido...");
		String tokenValido = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token ottenuto: " + tokenValido);
		ExtentTestManager.logInfo("Invio richiesta SENZA prefisso 'Bearer '...");
		given().baseUri("http://localhost:8080").header("Authorization", tokenValido) // ❌ Niente 'Bearer '
				.when().get("/api/admin-profile/is-protected-admin").then().statusCode(401);
		ExtentTestManager.logPass("✔ Header senza 'Bearer' correttamente rifiutato → 401 Unauthorized");
	}

	@Test
	void metodoHttpErrato_MethodNotAllowed() throws SQLException {
		ExtentTestManager.startTest("IPA-007 → Metodo HTTP errato (POST) → 405 Method Not Allowed");

		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Eseguo login per ottenere token valido...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token: " + tokenAdmin);

		ExtentTestManager.logInfo("Invio POST invece di GET a /is-protected-admin...");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").when().post("/api/admin-profile/is-protected-admin") // ❌ Metodo
																										// sbagliato
				.then().statusCode(405); // ✔ Atteso

		ExtentTestManager.logPass("✔ Metodo HTTP errato correttamente rifiutato → 405 Method Not Allowed");
	}
}
