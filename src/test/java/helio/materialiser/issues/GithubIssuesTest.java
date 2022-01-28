package helio.materialiser.issues;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import helio.components.loader.Extensions;
import helio.components.readers.RmlReader;
import helio.exceptions.SparqlQuerySyntaxException;
import helio.exceptions.SparqlRemoteEndpointException;
import helio.materialiser.test.utils.TestUtils;
import junit.framework.Assert;

public class GithubIssuesTest {

//	@Test
//	public void testIssue7()  {
//		long startTime = System.nanoTime();
//
//		long startTime1 = System.nanoTime();
//
//		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
//		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");
//		long endTime1 = System.nanoTime();
//		long duration1 = (endTime1 - startTime1) / 1000000;  //divide by 1000000 to get milliseconds.
//		System.out.println("components loading time (ms): "+duration1);
//
//
//		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue7/mapping-issue7.json");
//
//		long endTime = System.nanoTime();
//		long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
//		System.out.println("total time (ms): "+duration);
		//Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue7/mapping-issue7.json");
		//Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue7/expected-rdf.ttl");
//		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
//		Assert.assertTrue(!generated.isEmpty());
//		HelioConfiguration.HELIO_CACHE.deleteGraphs();
	//}

	/**
	 * This method test the generation of subjects using the function current time stamp
	 * @throws SparqlRemoteEndpointException
	 * @throws SparqlQuerySyntaxException
	 * @throws MalformedMappingException if the provided mapping has syntax errors
	 */
	/*@Test
	public void testIssue8() {
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue8/mapping-issue8.json");
		/*Boolean contains = generated.contains(null, RDF.type, (RDFNode) ResourceFactory.createResource("https://bimerr.iot.linkeddata.es/def/building#Building"));
		String literal = generated.listObjectsOfProperty(ResourceFactory.createProperty("https://bimerr.iot.linkeddata.es/def/building#description")).nextNode().asLiteral().toString();
		contains &= literal.equals("A office building which contains 12 space and 16 staffs.^^http://www.w3.org/2001/XMLSchema#string");
		contains &= generated.contains(null, ResourceFactory.createProperty("https://w3id.org/def/saref4bldg#hasSpace"), (RDFNode) ResourceFactory.createResource("https://www.data.bimerr.occupancy.es/resource/S2_Researcher_Office"));
		Assert.assertTrue(contains);
		HelioConfiguration.HELIO_CACHE.deleteGraphs();
	}*/
//
//	// https://github.com/oeg-upm/helio/issues/14
//	// This test takes ~1h to be ran
	@Test
	public void testIssue14() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException  {
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.CsvHandler", "DataHandler");
		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");

		Extensions.registerMappingReader(new RmlReader());
		//Fuseky.startPersistentService();

		long startTimeTotal = System.nanoTime();

		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue14/rml-mapping.txt");
		Model expectedModel = ModelFactory.createDefaultModel();//.readModel("./src/test/resources/git-issues/issue14/expected-rdf.ttl");
		generated.write(System.out, "NT");
		long stopTimeTotal = System.nanoTime();
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
		System.out.println("Total time: "+(stopTimeTotal - startTimeTotal) / 1000000);
	}


/*	@Test
	public void testIssue26()  {
		long startTimeTotal = System.nanoTime();
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
		Extensions.registerExtension(null, "helio.materialiser.data.handlers.CsvHandler", "DataHandler");

		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");
		Extensions.registerMappingReader(new RmlReader());
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue26/rml-map.ttl");

		//Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue14/expected-rdf.ttl");

		long stopTimeTotal = System.nanoTime();
		System.out.println("Total time: "+(stopTimeTotal - startTimeTotal) / 1000000);
	}
*/

//	@Test
//	public void testIssue27()  {
//		long startTimeTotal = System.nanoTime();
//		Extensions.registerExtension(null, "helio.materialiser.data.handlers.JsonHandler", "DataHandler");
//		Extensions.registerExtension(null, "helio.materialiser.data.handlers.CsvHandler", "DataHandler");
//
//		Extensions.registerExtension(null, "helio.materialiser.data.providers.FileProvider", "DataProvider");
//		Extensions.registerMappingReader(new RmlReader());
//		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/git-issues/issue27/mapping.ttl");
//
//		//Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue14/expected-rdf.ttl");
//
//		long stopTimeTotal = System.nanoTime();
//		System.out.println("Total time: "+(stopTimeTotal - startTimeTotal) / 1000000);
//	}
}
