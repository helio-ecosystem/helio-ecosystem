package helio.materialiser.engine.data.handler;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

import blueprints.materialiser.mappings.HelioMaterialiserMapping;
import helio.Configuration;
import helio.Helio;
import helio.blueprints.components.DataProvider;
import helio.components.handlers.RDFHandler;
import helio.components.providers.FileProvider;
import helio.materialiser.test.utils.TestUtils;

public class RDFHandlerTest  {

	/*
	@Test
	public void rdfHandlerTest() throws Exception {
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
		// Default separator ; and no delimiter, and column names
		Model expected = TestUtils.readModel("./src/test/resources/handlers-tests/rdf/test-rdf.ttl");

		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/rdf/test-rdf.ttl"));
		JsonObject configuration = new JsonObject();
		configuration.addProperty("format", "TURTLE");
		RDFHandler handler = new RDFHandler();
		handler.configure(configuration);


		Queue<String> data = handler.splitData(fileProvider.getData());
		String polledData = data.poll();
		InputStream inputStream = new ByteArrayInputStream(polledData.getBytes(Charset.forName("UTF-8")));
		Model parsedData = ModelFactory.createDefaultModel();
		parsedData.read(inputStream, HelioConfiguration.DEFAULT_BASE_URI, "TURTLE");

		Assert.assertTrue(TestUtils.compareModels(parsedData, expected));
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
	}


	@Test
	public void rdfHandlerMaterialisationTest() throws Exception {
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/rdf/test-rdf.ttl"));

		JsonObject configuration = new JsonObject();
		configuration.addProperty("format", "TURTLE");
		RDFHandler handler = new RDFHandler();
		handler.configure(configuration);
		DataSource ds = new DataSource();
		ds.setId("test");
		ds.setDataHandler(handler);
		ds.setDataProvider(fileProvider);

		RuleSet rs = new RuleSet();
		rs.setResourceRuleId("test resources");
		Set<String> ids = new HashSet<>();
		ids.add("test");
		rs.setDatasourcesId(ids);
		HelioMaterialiserMapping mappings = new HelioMaterialiserMapping();
		mappings.getDatasources().add(ds);
		mappings.getRuleSets().add(rs);

		HelioMaterialiser helio = new HelioMaterialiser(mappings);
		helio.updateSynchronousSources();


		Model expected = TestUtils.readModel("./src/test/resources/handlers-tests/rdf/test-rdf.ttl");
		Model generated = helio.getRDF();
		Assert.assertTrue(TestUtils.compareModels(expected, generated));
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
	}*/

	@Test
	public void rdfHandlerMaterialisationNTTest() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		DataProvider fileProvider = new FileProvider(new File("./src/test/resources/handlers-tests/rdf/output-with-links.nt"));

		JsonObject configuration = new JsonObject();
		configuration.addProperty("format", "N3");
		RDFHandler handler = new RDFHandler();
		handler.configure(configuration);
		DataSource ds = new DataSource();
		ds.setId("test");
		ds.setDataHandler(handler);
		ds.setDataProvider(fileProvider);

		RuleSet rs = new RuleSet();
		rs.setResourceRuleId("test resources");
		Set<String> ids = new HashSet<>();
		ids.add("test");
		rs.setDatasourcesId(ids);
		HelioMaterialiserMapping mappings = new HelioMaterialiserMapping();
		mappings.getDatasources().add(ds);
		mappings.getRuleSets().add(rs);

		Helio helio = new Helio(mappings);
		helio.updateSynchronousSources();


		Model expected = TestUtils.readModel("./src/test/resources/handlers-tests/rdf/output-with-links.nt");
		Model generated = helio.getRDF();
		generated.write(System.out, "TURTLE");
		Assert.assertTrue(TestUtils.compareModels(expected, generated));
		Configuration.HELIO_CACHE.deleteGraphs();
	}



}
