// package helio.tests.projects;
//
//import java.io.IOException;
//
//import org.apache.jena.rdf.model.Model;
//import org.junit.Assert;
//import org.junit.Test;
//
//import helio.exceptions.SparqlQuerySyntaxException;
//import helio.exceptions.SparqlRemoteEndpointException;
//import helio.tests.TestUtils;
//
//public class ThemisTest {
//
//
//	@Test
//	public void test1() throws IOException, InterruptedException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
//		Model expected = TestUtils.readModel("./src/test/resources/themis-tests/themis-1-expected.ttl");
//		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/themis-tests/themis-1-mapping.json");
//
//		Assert.assertTrue(TestUtils.compareModels(generated, expected));
//
//	}
//
//
//
//}
