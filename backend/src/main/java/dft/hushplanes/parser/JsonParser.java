package dft.hushplanes.parser;

import java.io.*;

import org.slf4j.*;

import com.google.gson.*;

public class JsonParser {
	private static final Logger LOG = LoggerFactory.getLogger(JsonParser.class);

	public static void main(String... args) throws FileNotFoundException {
		String folder = "h:\\temp\\sample";
		Gson gson = new GsonBuilder().create();
		for (File file : new File(folder).listFiles()) {
			LOG.trace("Loading {}", file);
			AircraftListJsonResponse model = gson.fromJson(new FileReader(file), AircraftListJsonResponse.class);
		}
	}
}
