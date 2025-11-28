package Utils;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.crypto.SecretKey;

import GestioneDb.DataBaseManager;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class ApiTestUtils {

	// -----------------------------------------------------
	//CREA ADMIN via API
	// -----------------------------------------------------
	public static void creaAdminViaApi() {
		String json = CreaJson.creaJsonAccesso("admin", "adminpass", "ADMIN");

		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(json).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));
	}

	// -----------------------------------------------------
	//CREA EMPLOYEE via API
	// -----------------------------------------------------
	public static void creaEmployeeViaApi() {
		String json = CreaJson.creaJsonAccesso("employee1", "emppass", "EMPLOYEE");

		given().baseUri("http://localhost:8080").basePath("/api/auth/register").contentType("application/json")
				.body(json).when().post().then().statusCode(200).body(equalTo("Registrazione completata"));
	}

	// -----------------------------------------------------
	//LOGIN → ottieni token
	// -----------------------------------------------------
	public static String loginRecuperaToken(String username, String password) {
		String json = CreaJson.creaJsonAccesso(username, password);

		return given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(json).when().post().then().statusCode(200).extract().path("token");
	}

	// -----------------------------------------------------
	//TOKEN SCADUTO per test sicurezza
	// -----------------------------------------------------
	public static String generaTokenScaduto() {
		SecretKey key = Keys.hmacShaKeyFor("mysecretkeyformyjwtgianlucatestsecure123456789".getBytes());
		long expired = System.currentTimeMillis() - 60_000;

		return Jwts.builder().subject("admin").claim("role", "ADMIN").issuedAt(new Date(expired))
				.expiration(new Date(expired)).signWith(key, Jwts.SIG.HS256).compact();
	}

	// -----------------------------------------------------
	//TOKEN con CHIAVE FALSA
	// -----------------------------------------------------
	public static String generaTokenConChiaveDiversa() {
		SecretKey keyFake = Keys.hmacShaKeyFor("chiaveDiversaPerTestAttacco123456".getBytes());
		long now = System.currentTimeMillis();

		return Jwts.builder().subject("admin").claim("role", "ADMIN").issuedAt(new Date(now))
				.expiration(new Date(now + 60_000)).signWith(keyFake, Jwts.SIG.HS256).compact();
	}

	// -----------------------------------------------------
	//CREA ADMIN PROTETTO
	// -----------------------------------------------------
	public static void creaAdminViaApiProtetto() throws SQLException {
		creaAdminViaApi();

		try (Statement st = DataBaseManager.getConnection().createStatement()) {
			st.execute("UPDATE users SET protected_admin = TRUE WHERE username = 'admin'");
			DataBaseManager.commit();
		}
	}
}