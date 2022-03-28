package test.generic;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.MappingExecutionException;

import helio.tests.TestUtils;

public class DataTypesTest {

	@Test
	public void test01() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		Model expected = TestUtils.readModel("./src/test/resources/datatypes-tests/test01-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/datatypes-tests/test01-mapping.json"));
		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}

	@Test
	public void test02() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		Model expected = TestUtils.readModel("./src/test/resources/datatypes-tests/test02-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/datatypes-tests/test02-mapping.json"));

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}

	@Test
	public void test03() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		Model expected = TestUtils.readModel("./src/test/resources/datatypes-tests/test03-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/datatypes-tests/test03-mapping.json"));
		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}


}
