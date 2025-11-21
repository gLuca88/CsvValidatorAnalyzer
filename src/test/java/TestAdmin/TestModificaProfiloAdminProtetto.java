package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import GestioneDb.DataBaseManager;
import Utils.BaseTest;


public class TestModificaProfiloAdminProtetto extends BaseTest {

	@Test
	void testUpDateAdminProtetto() throws Exception {

		creaAdminViaApi();

		String token = loginRecuperaToken();

		String updateJson = """
				{
				  "newUsername": "newAccountAdmin",
				  "newPassword": "nuovaPass123"
				}
				""";

		// 4) CHIAMO L’API UPDATE ADMIN
		given().baseUri("http://localhost:8080").basePath("/api/admin-profile/update").contentType("application/json")
				.header("Authorization", "Bearer " + token).body(updateJson).when().put().then().statusCode(200)
				.body(equalTo("UPDATED_AND_LOGOUT"));

		// Verifica nel DB — l’admin ora si chiama "newAccountAdmin"
		PreparedStatement ps = DataBaseManager.getConnection().prepareStatement(
				"SELECT username, password, protected_admin FROM users WHERE username = 'newAccountAdmin'");
		ResultSet rs = ps.executeQuery();

		assertTrue(rs.next(), "❌ Admin aggiornato non trovato nel DB");

		// Username corretto
		assertEquals("newAccountAdmin", rs.getString("username"), "❌ Username NON aggiornato nel DB");

		// Password corretta
		assertTrue(new BCryptPasswordEncoder().matches("nuovaPass123", rs.getString("password")),
				"❌ La password NON è stata aggiornata nel DB");

		// protected_admin deve restare TRUE
		assertTrue(rs.getBoolean("protected_admin"), "❌ L’admin protetto NON deve perdere la protezione");

		System.out.println("✔ Username, password e protezione admin aggiornati correttamente nel DB");
	}

}
