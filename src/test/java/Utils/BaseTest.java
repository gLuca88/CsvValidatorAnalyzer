package Utils;

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

}
