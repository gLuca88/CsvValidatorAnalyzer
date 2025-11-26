package Utils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import GestioneDb.DataBaseManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

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

	protected void creaEmployeeViaApi() {

		String json = CreaJson.creaJsonAccesso("employee1", "emppass", "EMPLOYEE");
		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(json).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));
	}

	protected String generaTokenScaduto() {

		SecretKey key = Keys.hmacShaKeyFor("mysecretkeyformyjwtgianlucatestsecure123456789".getBytes());
		long expired = System.currentTimeMillis() - 60_000;
		return Jwts.builder().subject("admin").claim("role", "ADMIN").issuedAt(new Date(expired))
				.expiration(new Date(expired)) // token già scaduto
				.signWith(key, Jwts.SIG.HS256).compact();
	}

	protected String generaTokenConChiaveDiversa() {

		// ❌ chiave diversa da quella del backend
		SecretKey keyFalsa = Keys.hmacShaKeyFor("chiaveDiversaPerTestAttacco123456".getBytes());

		long now = System.currentTimeMillis();

		return Jwts.builder().subject("admin").claim("role", "ADMIN").issuedAt(new Date(now))
				.expiration(new Date(now + 60_000)) // non scaduto
				.signWith(keyFalsa, Jwts.SIG.HS256) // ❗firma con chiave falsa
				.compact();
	}

}
