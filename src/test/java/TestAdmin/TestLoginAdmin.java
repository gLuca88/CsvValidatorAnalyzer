package TestAdmin;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.type.TypeReference;

import Model.LoginInvalidCase;
import Utils.BaseTest;
import Utils.CreaJson;
import Utils.JsonUtils;

public class TestLoginAdmin extends BaseTest {

	@Test
	void loginAdminCorretto() {

		creaAdminViaApi();
		String loginJson = CreaJson.creaJsonAccesso("admin", "adminpass");
		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json")
				.body(loginJson).when().post().then().statusCode(200).body("token", notNullValue());
		System.out.println("✔ Login admin corretto verificato");

	}

	static Stream<LoginInvalidCase> provider() {
		List<LoginInvalidCase> list = JsonUtils.readJson("loginInvalidCases.json",
				new TypeReference<List<LoginInvalidCase>>() {
				});
		return list.stream();
	}

	@ParameterizedTest
	@MethodSource("provider")
	void loginErrato(LoginInvalidCase caso) {
		System.out.println("▶ TEST → " + caso.getDescrizione());
		creaAdminViaApi();

		String json = CreaJson.creaJsonAccesso(caso.getUsername(), caso.getPassword());
		given().baseUri("http://localhost:8080").basePath("/api/auth/login").contentType("application/json").body(json)
				.when().post().then().statusCode(401).body(equalTo("Credenziali non valide"));

		System.out.println("✔ RISULTATO OK: " + caso.getDescrizione());
	}

}
