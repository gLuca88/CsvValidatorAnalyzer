package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import GestioneDb.DataBaseManager;
import GestioneDb.DataBaseWrapper;
import Utils.BaseTest;
import Utils.CreaJson;

public class TestRegistrazioneAdmin extends BaseTest {

	// primo avvio applicazione da zero registro un admin user-->admin
	// password-->adminpass
	@Test
	void registrazioneAdmin() throws Exception {

		String adminJson = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");

		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(adminJson).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));

		DataBaseWrapper dbControl = new DataBaseWrapper();
		boolean esisteRuoloAdmin = dbControl.verificaCondizione("users", "role", "ADMIN");

		assertTrue(esisteRuoloAdmin, "❌ Nessun utente ADMIN trovato nel DB");

		System.out.println("✔ Registrazione admin verificata nel DB");

	}

	@Test
	void testRegistrazioneAdminDuplicatoDeveFallire() {
		creaAdminViaApi();
		String adminDuplicato = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");
		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(adminDuplicato).when().post().then().statusCode(400).body(equalTo("Utente già esistente"));
		System.out.println("✔ Registrazione duplicata bloccata correttamente");
	}

	@Test
	void verificaAdminProtettoNelDBPrimaRegistrazione() throws SQLException {

		creaAdminViaApi();
		PreparedStatement ps = DataBaseManager.getConnection()
				.prepareStatement("SELECT role, protected_admin FROM users WHERE username = 'admin'");
		ResultSet rs = ps.executeQuery();
		assertTrue(rs.next(), "❌ Admin non presente nel DB");

		assertEquals("ADMIN", rs.getString("role"), "❌ Ruolo admin errato");
		assertTrue(rs.getBoolean("protected_admin"), "❌ L'admin NON risulta protetto");
		System.out.println("✔ Admin protetto verificato nel DB");
	}
	
	

}
