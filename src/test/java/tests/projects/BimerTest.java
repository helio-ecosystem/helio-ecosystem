package tests.projects;

import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import helio.exceptions.SparqlQuerySyntaxException;
import helio.exceptions.SparqlRemoteEndpointException;
import helio.materialiser.test.utils.TestUtils;

public class BimerTest {

	@Test
	public void test1() throws IOException, InterruptedException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		Model expected = TestUtils.readModel("./src/test/resources/bimr-tests/helio-1-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/bimr-tests/helio-1-mapping.json");
		while(true) {
			Thread.sleep(5000);
		}
		//Assert.assertTrue(TestUtils.compareModels(generated, expected));		
	}


	/**
	 * In this test the subject uses the function now() therefore there is no way to check if the subjects are the same in the expected rdf and the generated rdf.
	 * This test only verifies the predicate and objects from the expected and generated RDF.
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws SparqlQuerySyntaxException
	 * @throws SparqlRemoteEndpointException
	 */
	@Test
	public void test2() throws IOException, InterruptedException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		Model expected = TestUtils.readModel("./src/test/resources/bimr-tests/helio-1-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/bimr-tests/helio-2-mapping.json");
	
		boolean equivalent = expected.listStatements().toList()
						.parallelStream()
						.map(st -> generated.contains(null, st.getPredicate(), st.getObject()))
						.allMatch( elem -> elem == true);
		Assert.assertTrue(equivalent);
		
	}


	@Test
	public void test3() throws IOException, InterruptedException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		Model expected = TestUtils.readModel("./src/test/resources/bimr-tests/helio-3-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/bimr-tests/helio-3-mapping.json");



		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}


	@Test
	public void test4() throws IOException, InterruptedException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		Model expected = TestUtils.readModel("./src/test/resources/bimr-tests/helio-4-expected.ttl");
		Model generated = TestUtils.generateRDFSynchronously("./src/test/resources/bimr-tests/helio-4-mapping.json");

		Assert.assertTrue(TestUtils.compareModels(generated, expected));
	}






}
