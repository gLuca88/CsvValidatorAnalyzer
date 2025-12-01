package TestAdminDB;

import static Utils.ApiTestUtils.creaAdminViaApi;
import static Utils.ApiTestUtils.loginRecuperaToken;
import static org.junit.jupiter.api.Assertions.*;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import GestioneDb.DataBaseManager;
import ReportConfig.ExtentTestManager;
import ReportConfig.ReportExtension;
import Utils.BaseTest;

@ExtendWith(ReportExtension.class)
public class TestLoginDb extends BaseTest {

	@Test
	void loginValido_VerificaAdminNelDB() throws Exception {
		ExtentTestManager.startTest("LOGIN-DB-001 → Verifica dati admin nel DB dopo login valido");
		ExtentTestManager.logInfo("Registro admin via API...");
		creaAdminViaApi();
		ExtentTestManager.logInfo("Eseguo login come admin...");
		String token = loginRecuperaToken("admin", "adminpass");
		ExtentTestManager.logInfo("Token ottenuto: " + token);
		ExtentTestManager.logInfo("Verifico i dati dell'admin nel database...");
		String sql = "SELECT username, role, password FROM users WHERE username = 'admin'";
		PreparedStatement ps = DataBaseManager.getConnection().prepareStatement(sql);
		ResultSet rs = ps.executeQuery();
		assertTrue(rs.next(), "ERRORE: nessun admin trovato nel DB!");
		String dbUsername = rs.getString("username");
		String dbRole = rs.getString("role");
		String dbPassword = rs.getString("password");
		assertEquals("admin", dbUsername, "Username errato nel DB!");
		assertEquals("ADMIN", dbRole, "Ruolo errato nel DB!");
		// password deve essere HASHATA (non in chiaro)
		assertNotEquals("adminpass", dbPassword, "La password NON è hashata!");
		assertTrue(dbPassword.startsWith("$2a$") || dbPassword.startsWith("$2b$"),
				" Il formato della password non sembra un hash BCrypt!");
		ExtentTestManager.logPass("✔ Dati DB admin validi → Test OK");
	}

}
