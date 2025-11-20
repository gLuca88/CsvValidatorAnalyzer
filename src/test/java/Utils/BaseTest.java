package Utils;

import static org.hamcrest.CoreMatchers.equalTo;

import java.sql.Connection;
import java.sql.Statement;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import GestioneDb.DataBaseManager;
import static io.restassured.RestAssured.*;

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

	// crea admin via api
	protected void creaAdminViaApi() {
		String json = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");

		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(json).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));
	}
}
