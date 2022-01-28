package helio.materialiser.engine.mappings;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import blueprints.exceptions.MalformedMappingException;
import helio.Configuration;
import helio.materialiser.test.utils.TestUtils;

public class RMLTranslatorTest {


	@Test
	public void testXML1() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		Model expected = TestUtils.readModel("./src/test/resources/rml-tests/xml/test-xml-1-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/rml-tests/xml/test-xml-1-mapping.ttl");

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testXML2() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		Model expected = TestUtils.readModel("./src/test/resources/rml-tests/xml/test-xml-2-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/rml-tests/xml/test-xml-2-mapping.ttl");

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testCSV1() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		Model expected = TestUtils.readModel("./src/test/resources/rml-tests/csv/test-csv-1-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/rml-tests/csv/test-csv-1-mapping.ttl");

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testJSON1() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		String mappingFile = "./src/test/resources/rml-tests/json/test-json-1-mapping.ttl";
		String expectedFile = "./src/test/resources/rml-tests/json/test-json-1-expected.ttl";
		Configuration.HELIO_CACHE.deleteGraphs();
		Model expected = TestUtils.readModel(expectedFile);
		Model generated = TestUtils.generateRDFSynchronously(mappingFile);

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	/*@Test
	public void testMixed1() throws Exception {
		// Comment?
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
		String mappingFile = "./src/test/resources/rml-tests/mixed/test-mixed-1-mapping.ttl";
		String expectedFile = "./src/test/resources/rml-tests/mixed/test-mixed-1-expected.ttl";
		Model expected = TestUtils.readModel(expectedFile);
		Model generated = TestUtils.generateRDFSynchronously(mappingFile);

		generated.write(System.out,"TTL");
		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		HelioConfiguration.HELIO_CACHE.deleteGraphs();

	}*/

	@Test
	public void testMixed2() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		String mappingFile = "./src/test/resources/rml-tests/mixed/test-mixed-2-mapping.ttl";
		String expectedFile = "./src/test/resources/rml-tests/mixed/test-mixed-2-expected.ttl";
		Model expected = TestUtils.readModel(expectedFile);
		Model generated = TestUtils.generateRDFSynchronously(mappingFile);

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testMixed3() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		String mappingFile = "./src/test/resources/rml-tests/mixed/test-mixed-3-mapping.ttl";
		//
		Boolean expeptionThrown = false;
		try {
			expeptionThrown = TestUtils.generateRDFSynchronously(mappingFile).isEmpty();

		}catch (MalformedMappingException e) {
			expeptionThrown = true;
		}
		//
		Assert.assertTrue(expeptionThrown);
		Configuration.HELIO_CACHE.deleteGraphs();
	}


	@Test
	public void testLessons01() throws Exception {
		Configuration.HELIO_CACHE.deleteGraphs();
		String mappingFile = "./src/test/resources/rml-tests/lessons/accidents.ttl";
		//
		Boolean expeptionThrown = false;
		try {
			expeptionThrown = TestUtils.generateRDFSynchronously(mappingFile).isEmpty();

		}catch (MalformedMappingException e) {
			expeptionThrown = true;
		}
		//
		Assert.assertTrue(expeptionThrown);
		Configuration.HELIO_CACHE.deleteGraphs();
	}



}
