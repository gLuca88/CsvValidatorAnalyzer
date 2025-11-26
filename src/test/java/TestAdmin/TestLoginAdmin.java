package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;

import Model.LoginInvalidCase;
import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;
import Utils.CreaJson;
import Utils.JsonUtils;

@ExtendWith(ReportExtension.class)
public class TestLoginAdmin extends BaseTest {

	@Test
	void loginAdminCorretto() {

		ExtentTestManager.startTest("LOGIN-001 → LOGIN VALIDO");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Invio richiesta di login...");
		String loginJson = CreaJson.creaJsonAccesso("admin", "adminpass");

		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(loginJson).when().post().then().statusCode(200).body("token", notNullValue());
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

		ExtentTestManager.startTest(caso.getTcId() + " → " + caso.getDescrizione());

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Genero JSON errato...");
		String json = CreaJson.creaJsonAccesso(caso.getUsername(), caso.getPassword());

		ExtentTestManager.logInfo("Invio POST /login...");

		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json").body(json)
				.when().post().then().statusCode(401).body(equalTo("Credenziali non valide"));
	}

	@Test
	void controlloStrutturaToken() {

		ExtentTestManager.startTest("LOGIN-009 → Controllo struttura token JWT");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Ottengo token con loginRecuperaToken()...");
		String token = loginRecuperaToken();

		ExtentTestManager.logInfo("Token ottenuto: " + token);

		// Il token deve avere 3 parti
		String[] parts = token.split("\\.");
		assertEquals(3, parts.length, "Il token JWT non ha 3 parti!");

		// Decodifica payload
		String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

		ExtentTestManager.logInfo("Payload decodificato: " + payloadJson);

		assertTrue(payloadJson.contains("\"sub\":\"admin\""));
		assertTrue(payloadJson.contains("\"role\""));
		assertTrue(payloadJson.contains("\"iat\""));
		assertTrue(payloadJson.contains("\"exp\""));
		assertFalse(payloadJson.contains("password"));
	}

	@Test
	void accessoProtettoConTokenValido() {

		ExtentTestManager.startTest("LOGIN-010 → Accesso GET /api/admin/dbconfig/list");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();

		ExtentTestManager.logInfo("Recupero token di login...");
		String token = loginRecuperaToken();

		ExtentTestManager.logInfo("Invio GET all’endpoint protetto...");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + token).when()
				.get("/api/admin/dbconfig/list").then().statusCode(200);
	}

	@Test
	void loginJsonIncompleto_Response400() {

		ExtentTestManager.startTest("LOGIN-011 → JSON incompleto → 400 Bad Request");

		ExtentTestManager.logInfo("Creo JSON incompleto (manca username)");

		String jsonIncompleto = """
				{
				    "password": "adminpass"
				}
				""";

		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(jsonIncompleto).when().post().then().statusCode(400);
	}

	@Test
	void loginJsonMalformato_Response400() {

		ExtentTestManager.startTest("LOGIN-012 → JSON malformato → 400 Bad Request");
		ExtentTestManager.logInfo("Creo un JSON malformato impossibile da parsare...");
		String jsonMalformato = """
				{
				    "username": admin,
				    "password": "adminpass"
				}
				""";
		ExtentTestManager.logInfo("Invio POST /api/auth/login con JSON malformato...");
		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(jsonMalformato).when().post().then().statusCode(400);
	}

	@Test
	void accessoSenzaToken() {

		ExtentTestManager.startTest("LOGIN-013 → Nessun token → 401 Unauthorized");
		ExtentTestManager.logInfo("Invio richiesta SENZA Authorization header...");
		given().baseUri("http://localhost:8080").when().get("/api/admin/dbconfig/list").then().statusCode(401);

	}

	@Test
	void tokenManipolato_Response401() {

		ExtentTestManager.startTest("LOGIN-014 → Token manipolato → 401 Unauthorized");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Ottengo token valido...");
		String tokenValido = loginRecuperaToken();
		ExtentTestManager.logInfo("Token valido: " + tokenValido);
		ExtentTestManager.logInfo("Genero token MANIPOLATO...");
		String tokenCorrotto = tokenValido.substring(0, tokenValido.length() - 2) + "xx";
		ExtentTestManager.logInfo("Token manipolato: " + tokenCorrotto);
		ExtentTestManager.logInfo("Invio richiesta con token manipolato...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenCorrotto).when()
				.get("/api/admin/dbconfig/list").then().statusCode(401);
	}

	@Test
	void ruoloErrato_AccessoForbidden() {

		ExtentTestManager.startTest("LOGIN-015 → Ruolo errato → 403 Forbidden");

		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Creo employee via API...");
		creaEmployeeViaApi();
		ExtentTestManager.logInfo("Effettuo login come EMPLOYEE...");
		String tokenEmployee = loginRecuperaToken();
		ExtentTestManager.logInfo("Token employee: " + tokenEmployee);
		ExtentTestManager
				.logInfo("Invio GET a /api/admin/dbconfig/register con token EMPLOYEE(endpoint che registra db)...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenEmployee).when()
				.get("/api/admin/dbconfig/register").then().statusCode(403);
		ExtentTestManager.logPass("✔ Ruolo errato → correttamente bloccato con 403 Forbidden");
	}

	@Test
	void tokenScaduto_Response401() {

		ExtentTestManager.startTest("LOGIN-016 → Token scaduto → 401 Unauthorized");

		ExtentTestManager.logInfo("Genero token SCADUTO manualmente...");
		String tokenScaduto = generaTokenScaduto();
		ExtentTestManager.logInfo("Token scaduto: " + tokenScaduto);

		ExtentTestManager.logInfo("Invio GET a endpoint admin con token scaduto...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenScaduto).when()
				.get("/api/admin/dbconfig/list").then().statusCode(401);
		ExtentTestManager.logPass("✔ Token scaduto rifiutato correttamente → 401 Unauthorized");
	}

	@Test
	void hederSenzaBearer() {
		ExtentTestManager.startTest("LOGIN-017 → Authorization senza 'Bearer' → 401 Unauthorized");
		ExtentTestManager.logInfo("Creo admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Ottengo token valido...");
		String token = loginRecuperaToken();
		ExtentTestManager.logInfo("Token valido: " + token);
		ExtentTestManager.logInfo("Invio richiesta con Authorization malformato (senza Bearer)...");
		given().baseUri("http://localhost:8080").header("Authorization", token) // niente Bearer
				.when().get("/api/admin/dbconfig/list").then().statusCode(401);
		ExtentTestManager.logPass("✔ Header senza 'Bearer' correttamente rifiutato con 401 Unauthorized");
	}

	@Test
	void tokenFirmatoConChiaveDiversa() {

		ExtentTestManager.startTest("LOGIN-018 → Token firmato con chiave diversa → 401 Unauthorized");
		ExtentTestManager.logInfo("Genero token con chiave NON valida...");
		String tokenFalso = generaTokenConChiaveDiversa();
		ExtentTestManager.logInfo("Token falso: " + tokenFalso);
		ExtentTestManager.logInfo("Invio richiesta a /api/admin/dbconfig/list con token rifirmato illegalmente...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenFalso).when()
				.get("/api/admin/dbconfig/list").then().statusCode(401);
		ExtentTestManager.logPass("✔ Token firmato con chiave diversa rifiutato correttamente (401 Unauthorized)");

	}

	@Test
	void loginContentTypeErrato() {

		ExtentTestManager.startTest("LOGIN-019 → Content-Type errato → 415 Unsupported Media Type");

		ExtentTestManager.logInfo("Invio POST /api/auth/login con Content-Type text/plain...");

		String bodyNonJson = "username=admin&password=adminpass"; // NON JSON

		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("text/plain") // ❌ Content-Type
																										// sbagliato
				.body(bodyNonJson).when().post().then().statusCode(415); // ✔ expected

		ExtentTestManager.logPass("✔ Content-Type errato rifiutato con 415 Unsupported Media Type");
	}

	@Test
	void loginMetodoHttpErrato_Response405() {

		ExtentTestManager.startTest("LOGIN-020 → Metodo HTTP errato (GET) → 405 Method Not Allowed");

		ExtentTestManager.logInfo("Invio GET invece di POST all’endpoint /api/auth/login...");

		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json").when()
				.get() // ❌ Metodo sbagliato
				.then().statusCode(405);

		ExtentTestManager.logPass("✔ Metodo GET correttamente rifiutato con 405 Method Not Allowed");
	}

}
