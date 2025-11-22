package Utils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.sql.Connection;
import java.sql.Statement;


import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.BeforeEach;

import GestioneDb.DataBaseManager;


public abstract class BaseTest {

	@BeforeEach
	void setup() throws Exception {
		pulisciTabellaUsers();
	}

	@AfterEach
	void tearDown() throws Exception {
		Connection conn = DataBaseManager.getConnection();
		pulisciTabellaUsers();
		if (conn != null && !conn.isClosed()) {
			DataBaseManager.close();
		}
	}

	private void pulisciTabellaUsers() throws Exception {
		try (Statement st = DataBaseManager.getConnection().createStatement()) {
			st.execute("DELETE FROM users");
			DataBaseManager.commit();
		} catch (Exception e) {
			DataBaseManager.rollback();
			throw e;
		}
	}

	protected void creaAdminViaApi() {
		String json = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");

		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(json).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));
	}

	protected String loginRecuperaToken() {
		String loginJson = CreaJson.creaJsonAccesso("admin", "adminpass");

		return given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(loginJson).when().post().then().statusCode(200).extract().path("token");
	}
}
