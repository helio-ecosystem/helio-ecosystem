package helio.tests;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.MappingExecutionException;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class GithubIssuesTest {

	@Test
	public void testIssue7() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		
		Model generated = TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/git-issues/issue07/mapping-issue7.json"));
		Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue07/expected-rdf.ttl");
		
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
	}

	/**
	 * This method test the generation of subjects using the function current time stamp
	 * @throws ExtensionNotFoundException 
	 * @throws IncorrectMappingException 
	 * @throws MappingExecutionException 
	 * @throws IncompatibleMappingException 
	 * @throws SparqlRemoteEndpointException
	 * @throws SparqlQuerySyntaxException
	 * @throws MalformedMappingException if the provided mapping has syntax errors
	 */
	@Test
	public void testIssue8() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException {
		Model generated =  TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/git-issues/issue08/mapping-issue8.json"));
		Boolean contains = generated.contains(null, RDF.type, ResourceFactory.createResource("https://bimerr.iot.linkeddata.es/def/building#Building"));
		String literal = generated.listObjectsOfProperty(ResourceFactory.createProperty("https://bimerr.iot.linkeddata.es/def/building#description")).nextNode().asLiteral().toString();
		contains &= literal.equals("A office building which contains 12 space and 16 staffs.");
		contains &= generated.contains(null, ResourceFactory.createProperty("https://w3id.org/def/saref4bldg#hasSpace"), ResourceFactory.createResource("https://www.data.bimerr.occupancy.es/resource/S2_Researcher_Office"));
		Assert.assertTrue(contains);
	}

	// https://github.com/oeg-upm/helio/issues/14
	// This test takes ~1h to be ran
	@Test
	public void testIssue14() throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		Model expected = TestUtils.readModel("./src/test/resources/git-issues/issue14/expected-rdf.ttl");
		Model generated =  TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/git-issues/issue14/rml-mapping.txt"));
		//generated.write(System.out, "Turtle");
		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}


	@Test
	public void testIssue26() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
		Model generated =  TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/git-issues/issue26/rml-map.ttl"));
		Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue26/expected.ttl");
		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
}
//
//
//	@Test
//	public void testIssue27() throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException  {
//		Model generated =  TestUtils.generateRDFSynchronously(TestUtils.processJMapping("./src/test/resources/git-issues/issue27/mapping.ttl"));
//		Model expectedModel = TestUtils.readModel("./src/test/resources/git-issues/issue27/expected.ttl");
//		Assert.assertTrue(TestUtils.compareModels(generated, expectedModel));
//	}
}
