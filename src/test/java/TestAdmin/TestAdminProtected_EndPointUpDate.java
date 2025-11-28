package TestAdmin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;
import Utils.CreaJson;
import static Utils.ApiTestUtils.*;

import static io.restassured.RestAssured.given;

@ExtendWith(ReportExtension.class)
public class TestAdminProtected_EndPointUpDate extends BaseTest {

	@Test
	void UPD001_AdminModificaProfilo_200() {
		ExtentTestManager.startTest("UPD-001 → Admin protetto modifica il proprio profilo → 200 OK");

		creaAdminViaApi();
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_updated", "newpass123");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(200);

		ExtentTestManager.logPass("✔ Admin aggiornato correttamente → 200 OK");
	}

	@Test
	void UPD002_EmployeeNonAutorizzato_403() {

		ExtentTestManager.startTest("UPD-002 → Employee NON può modificare admin → 403 Forbidden");

		creaAdminViaApi();
		creaEmployeeViaApi();

		String tokenEmployee = loginRecuperaToken("employee1", "emppass");

		String jsonUpdate = CreaJson.creaJsonUpdate("x", "y");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenEmployee)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(403);

		ExtentTestManager.logPass("✔ Accesso vietato → 403 Forbidden");
	}

	@Test
	void UPD003_RichiestaSenzaToken_401() {

		ExtentTestManager.startTest("UPD-003 → Richiesta senza token → 401 Unauthorized");

		creaAdminViaApi();

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_new", "newpass123");

		given().baseUri("http://localhost:8080").contentType("application/json").body(jsonUpdate).when()
				.put("/api/admin-profile/update").then().statusCode(401);

		ExtentTestManager.logPass("✔ Richiesta senza token rifiutata → 401");
	}

	@Test
	void UPD004_TokenManipolato_401() {

		ExtentTestManager.startTest("UPD-004 → Token manipolato → 401 Unauthorized");

		creaAdminViaApi();

		String tokenValido = loginRecuperaToken("admin", "adminpass");
		String tokenManipolato = tokenValido.substring(0, tokenValido.length() - 3) + "XYZ";

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_new", "newpass123");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenManipolato)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401);

		ExtentTestManager.logPass("✔ Token manipolato rifiutato → 401");
	}

	@Test
	void UPD005_TokenScaduto_401() {

		ExtentTestManager.startTest("UPD-005 → Token scaduto → 401 Unauthorized");

		creaAdminViaApi();

		String tokenScaduto = generaTokenScaduto();

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_new", "newpass123");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenScaduto)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401);

		ExtentTestManager.logPass("✔ Token scaduto rifiutato → 401");
	}

	@Test
	void UPD006_HeaderSenzaBearer_401() {

		ExtentTestManager.startTest("UPD-006 → Authorization senza 'Bearer' → 401 Unauthorized");

		creaAdminViaApi();
		String token = loginRecuperaToken("admin", "adminpass");

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_new", "newpass123");

		given().baseUri("http://localhost:8080").header("Authorization", token) // ❌ manca "Bearer "
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401);

		ExtentTestManager.logPass("✔ Header senza Bearer rifiutato → 401");
	}

	@Test
	void UPD007_ContentTypeErrato_415() {

		ExtentTestManager.startTest("UPD-007 → Content-Type errato → 415 Unsupported Media Type");

		creaAdminViaApi();
		String token = loginRecuperaToken("admin", "adminpass");

		String bodyNonJson = "newUsername=aaa&newPassword=bbb";

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + token).contentType("text/plain") // ❌
																														// sbagliato
				.body(bodyNonJson).when().put("/api/admin-profile/update").then().statusCode(415);

		ExtentTestManager.logPass("✔ Content-Type errato → 415");
	}

	@Test
	void UPD008_JsonMalformato_400() {

		ExtentTestManager.startTest("UPD-008 → JSON malformato → 400 Bad Request");

		creaAdminViaApi();
		String token = loginRecuperaToken("admin", "adminpass");

		String jsonMalformato = """
				{
				    "newUsername": admin_new,
				    "newPassword": "abc123"
				}
				"""; // ❌ admin_new NON tra virgolette

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + token)
				.contentType("application/json").body(jsonMalformato).when().put("/api/admin-profile/update").then()
				.statusCode(400);

		ExtentTestManager.logPass("✔ JSON malformato rifiutato → 400");
	}

	@Test
	void UPD009_JsonIncompleto_400() {

		ExtentTestManager.startTest("UPD-009 → JSON incompleto → 400 Bad Request");
		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Effettuo login come ADMIN...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token admin: " + tokenAdmin);

		ExtentTestManager.logInfo("Creo JSON incompleto (manca newPassword)...");

		String jsonIncompleto = """
				{
				    "newUsername": "admin_nuovo"
				}
				""";

		ExtentTestManager.logInfo("Invio PUT /api/admin-profile/update con JSON incompleto...");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").body(jsonIncompleto).when().put("/api/admin-profile/update").then()
				.statusCode(400);

		ExtentTestManager.logPass("✔ JSON incompleto correttamente rifiutato → 400 Bad Request");

	}

	@Test
	void UPD010_MetodoHttpErrato_405() {

		ExtentTestManager.startTest("UPD-010 → Metodo HTTP errato (GET) → 405 Method Not Allowed");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Effettuo login come ADMIN...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token admin: " + tokenAdmin);

		ExtentTestManager.logInfo("Invio GET invece di PUT a /api/admin-profile/update...");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").when().get("/api/admin-profile/update") // ❌ Metodo sbagliato
				.then().statusCode(405);

		ExtentTestManager.logPass("✔ Metodo GET rifiutato correttamente → 405 Method Not Allowed");

	}

}
