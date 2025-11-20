package Utils;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	private static final ObjectMapper mapper = new ObjectMapper();

	public static <T> List<T> readJson(String file, TypeReference<List<T>> type) {
		try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(file)) {
			return mapper.readValue(is, type);
		} catch (Exception e) {
			throw new RuntimeException("Errore nella lettura del file JSON: " + file, e);
		}

	}

}
