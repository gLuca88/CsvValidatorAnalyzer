package Utils;

public class CreaJson {

	public static String creaJsonAccesso(String user, String password, String ruolo) {

		return """

				{
				   "username":"%s",
				   "password":"%s",
				   "role":"%s"
				}

				""".formatted(user, password, ruolo);

	}

	public static String creaJsonAccesso(String user, String password) {

		return """
				{
				"username":"%s",
				"password":"%s"

				}
				""".formatted(user, password);
	}

}
