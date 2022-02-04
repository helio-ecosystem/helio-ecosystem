package helio.tests.lessons;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import helio.tests.TestUtils;

public class LessonsTest {



	@Test
	public void testIssue01()  {
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/lessons/test01/accidents.ttl");
		Model expectedModel = TestUtils.readModel("./src/test/resources/lessons/test01/expected-rdf.ttl");
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
		Assert.assertTrue(!generated.isEmpty());

	}

	@Test
	public void testIssue02()  {
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/lessons/test02/SoloUnTripleMapRML.ttl");

		Model expectedModel = TestUtils.readModel("./src/test/resources/lessons/test02/expected-rdf.ttl");
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
		Assert.assertTrue(!generated.isEmpty());
	}

}
