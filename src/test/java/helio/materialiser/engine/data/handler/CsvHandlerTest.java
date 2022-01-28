package helio.materialiser.engine.data.handler;

import java.io.File;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

import helio.blueprints.components.DataProvider;
import helio.components.handlers.CsvHandler;
import helio.components.providers.FileProvider;

public class CsvHandlerTest {

	@Test
	public void csvTest() throws Exception {
		// Default separator ; and no delimiter, and column names
		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/csv/test-csv.csv"));

		CsvHandler csv = new CsvHandler();
		Queue<String> data = csv.splitData(fileProvider.getData());
		String polledData = data.poll();
		// Accessing with indexes
		Assert.assertTrue(csv.filter("0", polledData).get(0).equals("p1"));
		Assert.assertTrue(csv.filter("1", polledData).get(0).equals("958062172"));
		Assert.assertTrue(csv.filter("2", polledData).get(0).equals("958.06"));
		Assert.assertTrue(csv.filter("3", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("4", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("5", polledData).isEmpty());
		// Accessing with column names
		Assert.assertTrue(csv.filter("", polledData).get(0).equals("p1"));
		Assert.assertTrue(csv.filter("Nano", polledData).get(0).equals("958062172"));
		Assert.assertTrue(csv.filter("Millisec", polledData).get(0).equals("958.06"));
		Assert.assertTrue(csv.filter("TheadsDataChunks", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("ThreadsInjector", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("Fake-Column", polledData).isEmpty());

	}

	@Test
	public void csvTestConfiguration() throws Exception {
		// Configured separator ; and delimiter ", and column names
		JsonObject configuration = new JsonObject();
		configuration.addProperty("separator", ";");
		configuration.addProperty("delimitator", "\"");

		// Default separator ; and no delimiter
		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/csv/test-csv-2.csv"));

		CsvHandler csv = new CsvHandler();
		csv.configure(configuration);
		Queue<String> data = csv.splitData(fileProvider.getData());
		String polledData = data.poll();
		// Accessing with indexes
		Assert.assertTrue(csv.filter("0", polledData).get(0).equals("p1"));
		Assert.assertTrue(csv.filter("1", polledData).get(0).equals("958\"06\"2172"));
		Assert.assertTrue(csv.filter("2", polledData).get(0).equals("958.06"));
		Assert.assertTrue(csv.filter("3", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("4", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("5", polledData).isEmpty());
		// Accessing with column names
		Assert.assertTrue(csv.filter("", polledData).get(0).equals("p1"));
		Assert.assertTrue(csv.filter("Nano", polledData).get(0).equals("958\"06\"2172"));
		Assert.assertTrue(csv.filter("Millisec", polledData).get(0).equals("958.06"));
		Assert.assertTrue(csv.filter("TheadsDataChunks", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("ThreadsInjector", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("Fake-Column", polledData).isEmpty());

	}

	@Test
	public void csvTestConfigurationNoHeaders() throws Exception {
		// Configured separator ; and delimiter ", and column names
		JsonObject configuration = new JsonObject();
		configuration.addProperty("separator", ";");
		configuration.addProperty("delimitator", "\"");
		configuration.addProperty("has_headers", "false");

		// Default separator ; and no delimiter
		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/csv/test-csv-3.csv"));

		CsvHandler csv = new CsvHandler();
		csv.configure(configuration);
		Queue<String> data = csv.splitData(fileProvider.getData());
		String polledData = data.poll();
		// Accessing with indexes
		Assert.assertTrue(csv.filter("0", polledData).get(0).equals("p1"));
		Assert.assertTrue(csv.filter("1", polledData).get(0).equals("958\"06\"2172"));
		Assert.assertTrue(csv.filter("2", polledData).get(0).equals("958.06"));
		Assert.assertTrue(csv.filter("3", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("4", polledData).get(0).equals("1"));
		Assert.assertTrue(csv.filter("5", polledData).isEmpty());
		// Accessing with column names
		Assert.assertTrue(csv.filter("", polledData).isEmpty());
		Assert.assertTrue(csv.filter("Nano", polledData).isEmpty());
		Assert.assertTrue(csv.filter("Millisec", polledData).isEmpty());
		Assert.assertTrue(csv.filter("TheadsDataChunks", polledData).isEmpty());
		Assert.assertTrue(csv.filter("ThreadsInjector", polledData).isEmpty());
		Assert.assertTrue(csv.filter("Fake-Column", polledData).isEmpty());

	}
}
