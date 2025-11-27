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

	public static String creaJsonUpdate(String newUser, String newPassword) {

		return """
				 {
				            "newUsername": "%s",
				            "newPassword": "%s"
				        }

				""".formatted(newUser, newPassword);

	}

}
