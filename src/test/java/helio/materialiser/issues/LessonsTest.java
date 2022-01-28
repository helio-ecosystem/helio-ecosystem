package helio.materialiser.issues;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import helio.components.loader.Extensions;
import helio.components.readers.RmlReader;
import helio.materialiser.test.utils.TestUtils;

public class LessonsTest {



	@Test
	public void testIssue01()  {
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");
		Extensions.registerMappingReader(new RmlReader());

		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/lessons/test01/accidents.ttl");
		Model expectedModel = TestUtils.readModel("./src/test/resources/lessons/test01/expected-rdf.ttl");
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
		Assert.assertTrue(!generated.isEmpty());

	}

	@Test
	public void testIssue02()  {
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");
		Extensions.registerMappingReader(new RmlReader());
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/lessons/test02/SoloUnTripleMapRML.ttl");
		generated.write(System.out, "TTL");
		Model expectedModel = TestUtils.readModel("./src/test/resources/lessons/test02/expected-rdf.ttl");
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
		Assert.assertTrue(!generated.isEmpty());
	}

}
