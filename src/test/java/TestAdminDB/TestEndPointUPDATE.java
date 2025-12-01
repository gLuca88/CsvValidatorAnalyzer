package TestAdminDB;

import static Utils.ApiTestUtils.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import GestioneDb.DataBaseWrapper;
import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;
import Utils.CreaJson;

@ExtendWith(ReportExtension.class)
public class TestEndPointUPDATE extends BaseTest {

	private DataBaseWrapper db;

	@Test
	void upd001_AdminProtetto_ModificaProfilo_DB() throws Exception {
		db = new DataBaseWrapper();
		ExtentTestManager.startTest("UPD-DB-001 → Admin protetto modifica profilo → DB aggiornato correttamente");

		// Precondizione: admin protetto nel DB
		ExtentTestManager.logInfo("Creo admin via API e lo impongo come PROTETTO nel DB...");
		creaAdminViaApiProtetto();
		// Login → ottengo token valido
		ExtentTestManager.logInfo("Effettuo login come ADMIN...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		// Preparo JSON di update
		String newUser = "admin_updated";
		String newPass = "newpass123";
		String jsonUpdate = CreaJson.creaJsonUpdate(newUser, newPass);

		// Invio PUT all’endpoint
		ExtentTestManager.logInfo("Invio PUT /api/admin-profile/update...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(200).body(equalTo("UPDATED_AND_LOGOUT"));
		// Verifica username aggiornato nel DB
		ExtentTestManager.logInfo("Verifica nel DB dell'username aggiornato...");
		assertTrue(db.verificaCondizione("users", "username", newUser),
				"❌ L'username aggiornato NON è stato trovato nel DB!");
		// Verifica che protected_admin sia ancora TRUE
		ExtentTestManager.logInfo("Verifica protected_admin = TRUE...");
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"),
				"❌ protected_admin NON è TRUE dopo l'update!");
		ExtentTestManager.logPass("DB aggiornato correttamente dopo l’update del profilo admin");
	}

	@Test
	void employeeNonPuoModificareAdmin_DBInvariato() throws Exception {

		ExtentTestManager.startTest("UPD-DB-002 → Employee NON modifica admin protetto → DB invariato");
		// Creo admin protetto
		ExtentTestManager.logInfo("Creo admin via API e lo imposto protetto...");
		creaAdminViaApiProtetto();
		// Leggo stato DB PRIMA del tentativo (baseline)
		db = new DataBaseWrapper();
		boolean adminEsistePrima = db.verificaCondizione("users", "username", "admin");
		boolean adminProtettoPrima = db.verificaCondizione("users", "protected_admin", "1");
		assertTrue(adminEsistePrima, "Admin NON presente prima del test");
		assertTrue(adminProtettoPrima, "Admin NON marcato come protetto prima del test");
		// Creo employee
		ExtentTestManager.logInfo("Creo employee via API...");
		creaEmployeeViaApi();
		// Employee fa login
		ExtentTestManager.logInfo("Login come EMPLOYEE...");
		String tokenEmployee = loginRecuperaToken("employee1", "emppass");
		// Preparo JSON valido
		String jsonUpdate = CreaJson.creaJsonUpdate("nuovoNome", "nuovaPass123");
		// Invio PUT con EMPLOYEE → deve dare 403
		ExtentTestManager.logInfo("Employee tenta di modificare admin protetto...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenEmployee)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(403);
		ExtentTestManager.logInfo("Accesso vietato come previsto (403)");
		// Verifico che il DB sia invariato
		boolean adminEsisteDopo = db.verificaCondizione("users", "username", "admin");
		boolean adminProtettoDopo = db.verificaCondizione("users", "protected_admin", "1");

		assertTrue(adminEsisteDopo, "L'admin è misteriosamente sparito dal DB!");
		assertTrue(adminProtettoDopo, "L'admin ha perso il flag protected_admin!");

		ExtentTestManager.logPass("DB invariato → Employee NON può modificare admin protetto");
	}

	@Test
	void updateSenzaToken_DbInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-003 → Update senza token → DB invariato");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Registro admin protetto nel DB...");
		creaAdminViaApiProtetto(); // admin + protected_admin = TRUE
		db = new DataBaseWrapper();

		// Verifica precondizione
		ExtentTestManager.logInfo("Verifico che 'admin' esista nel DB prima dell'update...");
		assertTrue(db.verificaCondizione("users", "username", "admin"), "Admin non presente nel DB!");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Preparo JSON valido, ma invio la richiesta SENZA TOKEN...");

		String jsonUpdate = CreaJson.creaJsonUpdate("admin_modificato_illecitamente", "nuovapass123");

		given().baseUri("http://localhost:8080").contentType("application/json").body(jsonUpdate).when()
				.put("/api/admin-profile/update").then().statusCode(401); // ← 401 atteso

		ExtentTestManager.logInfo("Ricevuto 401 Unauthorized come previsto");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Controllo che il DB NON sia stato modificato...");

		boolean usernameRestatoUguale = db.verificaCondizione("users", "username", "admin");
		assertTrue(usernameRestatoUguale, " ERRORE: il username admin NON doveva essere modificato!");

		boolean protectedFlag = db.verificaCondizione("users", "protected_admin", "1");
		assertTrue(protectedFlag, " ERRORE: il flag protected_admin doveva restare TRUE!");

		ExtentTestManager.logPass(" DB invariato come previsto → Test PASSED");
	}

	@Test
	void updateConTokenManipolato_DbInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-004 → Token manipolato → DB invariato");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Registro admin protetto...");
		creaAdminViaApiProtetto();
		db = new DataBaseWrapper();

		// Precondizione: admin esiste
		assertTrue(db.verificaCondizione("users", "username", "admin"), "Admin non trovato nel DB prima dell'update!");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Effettuo login come ADMIN per ottenere un token valido...");
		String tokenValido = loginRecuperaToken("admin", "adminpass");

		// Corrompiamo il token (firma alterata)
		String tokenManipolato = tokenValido.substring(0, tokenValido.length() - 5) + "ABCDE";

		ExtentTestManager.logInfo("Token manipolato generato: " + tokenManipolato);

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Invio PUT con token NON valido...");
		String jsonUpdate = CreaJson.creaJsonUpdate("admin_illegalUpdate", "newpassXYZ");

		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenManipolato)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401); // token non valido = 401

		ExtentTestManager.logInfo("Ricevuto 401 Unauthorized come previsto");

		// ------------------------------------------------------------
		ExtentTestManager.logInfo("Verifico che il DB NON è stato modificato...");

		assertTrue(db.verificaCondizione("users", "username", "admin"), "ERRORE: username NON doveva essere cambiato!");

		assertTrue(db.verificaCondizione("users", "protected_admin", "1"), "❌ protected_admin doveva restare TRUE!");

		ExtentTestManager.logPass("DB invariato → Test PASSED");
	}

	@Test
	void updateConTokenScaduto_DBInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-005 → Token scaduto → DB invariato");

		// 1. Precondizioni: creo admin protetto
		ExtentTestManager.logInfo("Creo admin via API e lo imposto come protetto...");
		creaAdminViaApiProtetto();
		ExtentTestManager.logInfo("Verifico che l'admin iniziale esista nel DB...");
		db = new DataBaseWrapper();
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));
		// 2. Genero token SCADUTO
		ExtentTestManager.logInfo("Genero token scaduto...");
		String tokenScaduto = generaTokenScaduto();
		// 3. Preparo JSON valido
		ExtentTestManager.logInfo("Preparo JSON valido per update...");
		String jsonUpdate = CreaJson.creaJsonUpdate("admin_updated", "newpass123");
		ExtentTestManager.logInfo("Invio PUT /api/admin-profile/update con token SCADUTO...");
		// 4. Invio la richiesta con TOKEN SCADUTO → deve dare 401
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenScaduto)
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401); // <-- TOKEN NON VALIDO / SCADUTO
		ExtentTestManager.logInfo("L’API ha correttamente rifiutato il token scaduto.");
		// 5. Controlli sul DB → deve essere INVARIATO
		ExtentTestManager.logInfo("Verifico che NESSUN dato nel DB sia cambiato...");

		// username originale ancora presente
		assertTrue(db.verificaCondizione("users", "username", "admin"));

		// username NUOVO NON deve esistere
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));

		// protected_admin deve rimanere TRUE
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		ExtentTestManager.logPass("✔ DB invariato → update rifiutato come previsto (401 Unauthorized)");
	}

	@Test
	void updateHeaderSenzaBearer_DBInvariato() throws Exception {

		ExtentTestManager.startTest("UPD-DB-006 → Header senza Bearer → DB invariato");

		ExtentTestManager.logInfo("Creo admin via API e lo imposto come protetto...");
		creaAdminViaApiProtetto();

		db = new DataBaseWrapper();

		// Verifico stato iniziale DB
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		// 2. Login → ottengo token valido
		ExtentTestManager.logInfo("Effettuo login come admin...");
		String tokenValido = loginRecuperaToken("admin", "adminpass");

		// 3. Preparo JSON valido
		String jsonUpdate = CreaJson.creaJsonUpdate("admin_updated", "newpass123");
		// 4. Invio richiesta SENZA “Bearer”
		ExtentTestManager.logInfo("Invio PUT con header Authorization SENZA Bearer...");
		given().baseUri("http://localhost:8080").header("Authorization", tokenValido) // ❌ manca Bearer
				.contentType("application/json").body(jsonUpdate).when().put("/api/admin-profile/update").then()
				.statusCode(401);
		// 5. Controlli sul DB → deve essere invariato
		ExtentTestManager.logInfo("Verifico che il DB sia invariato...");
		// username originale ancora presente
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		// username NUOVO NON deve essere presente
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));
		// protected_admin invariato
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));
		ExtentTestManager.logPass("✔ Header senza Bearer → update rifiutato → DB invariato");
	}

	@Test
	void updateContentTypeErrato_DBInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-007 → Content-Type errato → DB invariato");
		// 1. Precondizioni
		ExtentTestManager.logInfo("Creo admin e lo imposto come PROTETTO...");
		creaAdminViaApiProtetto();

		DataBaseWrapper db = new DataBaseWrapper();
		// Stato iniziale DB
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		// 2. Login → ottengo token
		ExtentTestManager.logInfo("Effettuo login come admin...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		// 3. Preparo JSON valido
		String jsonUpdate = CreaJson.creaJsonUpdate("admin_updated", "newpass123");
		// 4. Invio PUT con Content-Type ERRATO
		ExtentTestManager.logInfo("Invio PUT con Content-Type text/plain...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("text/plain") // ❌ ERRATO
				.body(jsonUpdate).when().put("/api/admin-profile/update").then().statusCode(415);
		// 5. Verifica DB → deve essere invariato
		ExtentTestManager.logInfo("Verifico che il DB sia invariato...");
		// username originale ancora presente
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		// username nuovo NON deve esistere
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));
		// protected_admin invariato
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		ExtentTestManager.logPass("✔ Content-Type errato → update rifiutato → DB invariato");
	}

	@Test
	void updateJsonMalformato_DBInvariato() throws Exception {

		ExtentTestManager.startTest("UPD-DB-008 → JSON malformato → DB invariato");

		// 1. Creo admin protetto
		ExtentTestManager.logInfo("Creo admin via API e lo imposto PROTETTO...");
		creaAdminViaApiProtetto();

		db = new DataBaseWrapper();
		// Stato iniziale DB
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		// 2. Login → ottengo token valido
		ExtentTestManager.logInfo("Effettuo login come admin...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");

		// 3. Preparo JSON MALFORMATO
		String jsonMalformato = """
				{
				    "newUsername": "admin_updated",
				    "newPassword": "newpass123"
				"""; // ← Manca parentesi finale
		ExtentTestManager.logInfo("JSON malformato creato...");
		// 4. Invio PUT con JSON malformato
		ExtentTestManager.logInfo("Invio PUT con JSON malformato...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").body(jsonMalformato).when().put("/api/admin-profile/update").then()
				.statusCode(400);
		// 5. Verifica DB → deve essere invariato
		ExtentTestManager.logInfo("Verifico che il DB non sia stato modificato...");
		// username originale deve ancora esistere
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		// username nuovo NON deve esistere
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));
		// protected_admin invariato
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));
		ExtentTestManager.logPass("✔ JSON malformato → update rifiutato → DB invariato");

	}

	@Test
	void updateJsonIncompleto_DBInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-009 → JSON incompleto → DB invariato");
		// 1. Creo admin protetto
		ExtentTestManager.logInfo("Creo admin via API e lo imposto PROTETTO...");
		creaAdminViaApiProtetto();
		DataBaseWrapper db = new DataBaseWrapper();
		// Stato iniziale DB
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));
		// 2. Login → ottengo token valido
		ExtentTestManager.logInfo("Effettuo login come admin...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		// 3. Preparo JSON INCOMPLETO
		String jsonIncompleto = """
				{
				    "newUsername": "admin_updated"
				}
				""";
		ExtentTestManager.logInfo("JSON incompleto creato (manca newPassword)...");
		// 4. Invio PUT con JSON incompleto
		ExtentTestManager.logInfo("Invio PUT con JSON incompleto...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").body(jsonIncompleto).when().put("/api/admin-profile/update").then()
				.statusCode(400);
		// 5. Verifica DB → deve essere invariato
		ExtentTestManager.logInfo("Verifico che il DB non sia stato modificato...");

		// username originale deve esistere ancora
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		// username nuovo NON deve esistere
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));
		// protected_admin invariato
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		ExtentTestManager.logPass("✔ JSON incompleto → update rifiutato → DB invariato");
	}

	@Test
	void metodoHttpErrato_DBInvariato() throws Exception {
		ExtentTestManager.startTest("UPD-DB-010 → Metodo HTTP errato (GET) → DB invariato");
		// 1. Creo admin protetto
		ExtentTestManager.logInfo("Creo admin via API e lo imposto PROTETTO...");
		creaAdminViaApiProtetto();
		db = new DataBaseWrapper();

		// Stato iniziale DB
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));
		// 2. Login → token valido
		ExtentTestManager.logInfo("Effettuo login come ADMIN...");
		String tokenAdmin = loginRecuperaToken("admin", "adminpass");
		// 3. Invio GET invece del PUT
		ExtentTestManager.logInfo("Invio GET invece di PUT → deve ritornare 405...");
		given().baseUri("http://localhost:8080").header("Authorization", "Bearer " + tokenAdmin)
				.contentType("application/json").when().get("/api/admin-profile/update") // ❌ metodo sbagliato
				.then().statusCode(405);
		// 4. Verifica DB invariato
		ExtentTestManager.logInfo("Verifico che il DB non sia stato modificato...");

		// username originale deve esistere
		assertTrue(db.verificaCondizione("users", "username", "admin"));
		// nessun username “admin_updated”
		assertFalse(db.verificaCondizione("users", "username", "admin_updated"));
		// protected_admin invariato
		assertTrue(db.verificaCondizione("users", "protected_admin", "1"));

		ExtentTestManager.logPass("✔ Metodo HTTP errato → DB invariato come previsto (405 + no update)");
	}

	

}
