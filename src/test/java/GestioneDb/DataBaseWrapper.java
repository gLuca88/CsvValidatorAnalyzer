package GestioneDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DataBaseWrapper {

	private final Connection connection;

	public DataBaseWrapper() throws Exception {
		// Otteniamo la connessione dal manager (singleton)
		this.connection = DataBaseManager.getConnection();
	}

	/**
	 * Metodo generico che controlla se esiste almeno un record nella tabella users
	 * che soddisfa la condizione: campo = valore
	 *
	 * @param campo  il nome della tabela (es. "user,admin,db")
	 * @param campo  il nome della colonna (es. "role", "username",
	 *               "protected_admin")
	 * @param valore il valore da cercare (es. "ADMIN", "pippo", "1")
	 * @return true se esiste almeno un record che soddisfa la condizione
	 * @throws Exception
	 */
	public boolean verificaCondizione(String tableName, String campo, String valore) throws Exception {

		String queryTemplate = "SELECT COUNT(*) FROM {table} WHERE {campo} = ?";
		String query = queryTemplate.replace("{table}", tableName).replace("{campo}", campo);

		PreparedStatement ps = connection.prepareStatement(query);
		ps.setString(1, valore);

		ResultSet rs = ps.executeQuery();
		rs.next();

		return rs.getInt(1) > 0;
	}

}
