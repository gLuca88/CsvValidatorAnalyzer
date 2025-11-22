package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import GestioneDb.DataBaseManager;
import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;

@ExtendWith(ReportExtension.class)
public class TestModificaProfiloAdminProtetto extends BaseTest {

	@Test
	void testUpDateAdminProtetto() throws Exception {

		ExtentTestManager.startTest("Modifica Profilo Admin Protetto");

		try {
			// --- PRECONDIZIONE ---
			ExtentTestManager.logInfo("Creo admin tramite API...");
			creaAdminViaApi();

			ExtentTestManager.logInfo("Eseguo login per ottenere il token JWT...");
			String token = loginRecuperaToken();
			ExtentTestManager.logInfo("Token ottenuto: " + token);

			// --- COSTRUZIONE JSON UPDATE ---
			String updateJson = """
					{
					  "newUsername": "newAccountAdmin",
					  "newPassword": "nuovaPass123"
					}
					""";

			ExtentTestManager.logInfo("Invio PUT /api/admin-profile/update...");

			// --- CHIAMATA API ---
			given().baseUri("http://localhost:8080").basePath("/api/admin-profile/update")
					.contentType("application/json").header("Authorization", "Bearer " + token).body(updateJson).when()
					.put().then().statusCode(200).body(equalTo("UPDATED_AND_LOGOUT"));

			ExtentTestManager.logInfo("Risposta API valida. Procedo con le verifiche DB.");

			// --- VERIFICA NEL DATABASE ---
			PreparedStatement ps = DataBaseManager.getConnection().prepareStatement(
					"SELECT username, password, protected_admin FROM users WHERE username = 'newAccountAdmin'");
			ResultSet rs = ps.executeQuery();

			assertTrue(rs.next(), "❌ Admin aggiornato non trovato nel DB");
			ExtentTestManager.logInfo("Admin aggiornato trovato nel DB");

			// Username aggiornato
			assertEquals("newAccountAdmin", rs.getString("username"), "❌ Username NON aggiornato nel DB");
			ExtentTestManager.logInfo("Username aggiornato correttamente");

			// Password aggiornata e criptata
			assertTrue(new BCryptPasswordEncoder().matches("nuovaPass123", rs.getString("password")),
					"❌ La password NON è stata aggiornata nel DB");
			ExtentTestManager.logInfo("Password aggiornata correttamente (hash verificato)");

			// protected_admin deve restare TRUE
			assertTrue(rs.getBoolean("protected_admin"), "❌ L’admin protetto NON deve perdere la protezione");
			ExtentTestManager.logInfo("Campo protected_admin confermato TRUE");

			// --- SUCCESSO ---
			ExtentTestManager.logPass("✔ Modifica credenziali admin protetto verificata con successo");

		} catch (Exception e) {

			// --- ERRORE ---
			ExtentTestManager.logFail("❌ Errore test modifica admin protetto: " + e.getMessage());
			throw e;
		}
	}

}
