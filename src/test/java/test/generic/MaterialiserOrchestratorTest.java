package test.generic;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import blueprints.SparqlResultsFormat;
import blueprints.exceptions.MalformedMappingException;
import blueprints.materialiser.mappings.HelioMaterialiserMapping;
import helio.Configuration;
import helio.MaterialiserOrchestrator;
import helio.components.readers.JsonTranslator;

public class MaterialiserOrchestratorTest {

	public static final String JSON_MAPPING = "{" +
			"  \"datasources\" : [\n" +
			"      {\n" +
			"        \"id\" : \"test\",\n" +
			"        \"provider\" : { \"type\" : \"FileProvider\", \"file\" : \"./src/test/resources/json-file-1.json\"},\n" +
			"        \"handler\" : { \"type\" : \"JsonHandler\", \"iterator\" : \"$.book[*]\"}\n" +
			"      }\n" +
			"  ],\n" +
			"\"resource_rules\" : [\n" +
			"    { \n" +
			"      \"id\" : \"Astrea Queries\",\n" +
			"      \"datasource_ids\" : [\"test\"],\n" +
			"      \"subject\" : \"http://localhost:8080/[REPLACE(TRIM({$.title}), ' ', '')]\",\n" +
			"      \"properties\"  : [\n" +
			"            {\n" +
			"               \"predicate\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#type\", \n" +
			"               \"object\" : \"https://w3id.org/def/astrea#SPARQLQuery\",\n" +
			"               \"is_literal\" : \"False\" \n" +
			"            },\n" +
			"            {\n" +
			"               \"predicate\" : \"https://w3id.org/def/astrea#body\", \n" +
			"               \"object\" : \"[TRIM({$.title})]\",\n" +
			"               \"lang\" : \"en\",\n" +
			"               \"is_literal\" : \"True\" \n" +
			"            },{\n" +
			"               \"predicate\" : \"https://w3id.org/def/astrea#order\", \n" +
			"               \"object\" : \"{$.price}\",\n" +
			"                \"datatype\" : \"http://www.w3.org/2001/XMLSchema#nonNegativeInteger\",\n" +
			"               \"is_literal\" : \"True\" \n" +
			"            }                       \n" +
			"      ]\n" +
			"    }" +
			"  ]\n" +
			"} " ;




	@Test
	public void testAddData() throws IOException, MalformedMappingException, InterruptedException {
		JsonTranslator translator = new JsonTranslator();
		HelioMaterialiserMapping mappings = translator.translate(JSON_MAPPING);

		MaterialiserOrchestrator orchestrator = new MaterialiserOrchestrator(mappings);
		orchestrator.updateSynchronousSources();

		Configuration.HELIO_CACHE.deleteGraphs();
		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		orchestrator.updateSynchronousSources();
		//Thread.sleep(200);

		Assert.assertFalse(Configuration.HELIO_CACHE.getGraphs().isEmpty());

	}


	@Test
	public void testAddDataAndQuery() throws IOException, MalformedMappingException, InterruptedException {
		JsonTranslator translator = new JsonTranslator();
		HelioMaterialiserMapping mappings = translator.translate(JSON_MAPPING);

		MaterialiserOrchestrator orchestrator = new MaterialiserOrchestrator(mappings);
		orchestrator.updateSynchronousSources();


		Configuration.HELIO_CACHE.deleteGraphs();
		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		orchestrator.updateSynchronousSources();

		String  input = Configuration.HELIO_CACHE.solveTupleQuery("SELECT DISTINCT ?s { ?s ?p ?o .}", SparqlResultsFormat.JSON);
		Assert.assertFalse(input.isEmpty());

	}


}
