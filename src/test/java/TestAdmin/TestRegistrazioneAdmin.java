package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import GestioneDb.DataBaseManager;
import GestioneDb.DataBaseWrapper;
import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;
import Utils.CreaJson;



@ExtendWith(ReportExtension.class)
public class TestRegistrazioneAdmin extends BaseTest {

	@Test
	void registrazioneAdmin() throws Exception {

		ExtentTestManager.startTest("Registrazione Primo Admin");

		try {
			String adminJson = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");
			ExtentTestManager.logInfo("Invio richiesta registrazione admin...");

			given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
					.body(adminJson).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));

			ExtentTestManager.logInfo("Registro admin nel DB...");

			DataBaseWrapper dbControl = new DataBaseWrapper();
			boolean esisteRuoloAdmin = dbControl.verificaCondizione("users", "role", "ADMIN");

			assertTrue(esisteRuoloAdmin, "❌ Nessun utente ADMIN trovato nel DB");

			ExtentTestManager.logPass("✔ Registrazione admin verificata nel DB");

		} catch (Exception e) {
			ExtentTestManager.logFail("❌ Errore registrazione admin: " + e.getMessage());
			throw e;
		}
	}

	@Test
	void testRegistrazioneAdminDuplicatoDeveFallire() {

		ExtentTestManager.startTest("Registrazione Admin Duplicato");

		try {
			ExtentTestManager.logInfo("Creo admin iniziale tramite API...");
			creaAdminViaApi();

			String adminDuplicato = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");

			ExtentTestManager.logInfo("Tento registrazione duplicata...");

			given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
					.body(adminDuplicato).when().post().then().statusCode(400).body(equalTo("Utente già esistente"));

			ExtentTestManager.logPass("✔ Registrazione duplicata bloccata correttamente");

		} catch (Exception e) {
			ExtentTestManager.logFail("❌ Errore test duplicato admin: " + e.getMessage());
			throw e;
		}
	}

	@Test
	void verificaAdminProtettoNelDBPrimaRegistrazione() throws SQLException {

		ExtentTestManager.startTest("Verifica Admin Protetto (prima registrazione)");

		try {
			ExtentTestManager.logInfo("Registro admin tramite API...");
			creaAdminViaApi();

			ExtentTestManager.logInfo("Controllo DB per admin protetto...");

			PreparedStatement ps = DataBaseManager.getConnection()
					.prepareStatement("SELECT role, protected_admin FROM users WHERE username = 'admin'");

			ResultSet rs = ps.executeQuery();

			assertTrue(rs.next(), "❌ Admin non presente nel DB");
			ExtentTestManager.logInfo("Admin trovato nel DB");

			assertEquals("ADMIN", rs.getString("role"), "❌ Ruolo admin errato");
			ExtentTestManager.logInfo("Ruolo admin corretto");

			assertTrue(rs.getBoolean("protected_admin"), "❌ L'admin NON risulta protetto");
			ExtentTestManager.logInfo("Campo protected_admin = TRUE");

			ExtentTestManager.logPass("✔ Admin protetto verificato correttamente nel DB");

		} catch (Exception e) {
			ExtentTestManager.logFail("❌ Errore verifica admin protetto: " + e.getMessage());
			throw e;
		}
	}

}
