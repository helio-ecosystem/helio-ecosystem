package helio.materialiser.engine.executors;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import blueprints.SparqlResultsFormat;
import blueprints.expressions.EvaluableExpression;
import helio.Configuration;
import helio.blueprints.components.DataProvider;
import helio.components.handlers.JsonHandler;
import helio.components.providers.FileProvider;
import helio.translation.ExecutableRule;

public class ExecutableRuletTest {

	private static final String DATA_FRAGMENT_1 = 	"{\n" +
			"            \"title\": \"JSON: Questions and Answers\",\n" +
			"            \"author\": \"George Duckett\",\n" +
			"            \"price\": 6.00\n" +
			"        }" ;

	private static final String DATA_FRAGMENT_2 = 	"{\n" +
			"            \"title\": \"JData for beginers\",\n" +
			"            \"author\": \"George Foreman\",\n" +
			"            \"price\": 72.00\n" +
			"        }" ;





	@Test
	public void testAddData() throws IOException {
		Configuration.HELIO_CACHE.deleteGraphs();

		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		JsonObject object1 = (new Gson()).fromJson("{\"file\" : \"./src/test/resources/json-file-1.json\"}", JsonObject.class);
		DataProvider memoryProvider = new FileProvider(object1);
		JsonObject object = (new Gson()).fromJson("{\"iterator\" : \"$.book[*]\"}", JsonObject.class);
		DataHandler jsonHandler = new JsonHandler();
		jsonHandler.configure(object);
		DataSource ds = new DataSource("test1", jsonHandler, memoryProvider, null);

		Rule rule = new Rule();
		rule.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/name"));
		rule.setObject(new EvaluableExpression("{$.title}") );
		rule.setIsLiteral(true);
		RuleSet rs = new RuleSet();
		rs.getProperties().add(rule);
		rs.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects"));

		ExecutableRule exec = new ExecutableRule(rs, ds, DATA_FRAGMENT_1);

		exec.generateRDF("http://fake-subject.es/objects", rule);
		Assert.assertFalse(Configuration.HELIO_CACHE.getGraphs().isEmpty());
		Configuration.HELIO_CACHE.deleteGraphs();
	}


	@Test
	public void testAddDataParallel() throws IOException, InterruptedException {
		Configuration.HELIO_CACHE.deleteGraphs();

		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		JsonObject object1 = (new Gson()).fromJson("{\"file\" : \"./src/test/resources/json-file-1.json\"}", JsonObject.class);
		DataProvider memoryProvider = new FileProvider(object1);
		JsonObject object = (new Gson()).fromJson("{\"iterator\" : \"$.book[*]\"}", JsonObject.class);
		DataHandler jsonHandler = new JsonHandler();
		jsonHandler.configure(object);
		DataSource ds = new DataSource("test1", jsonHandler, memoryProvider, null);

		Rule rule = new Rule();
		rule.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/name"));
		rule.setObject(new EvaluableExpression("{$.title}") );
		rule.setIsLiteral(true);
		RuleSet rs1 = new RuleSet();
		rs1.getProperties().add(rule);
		rs1.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects/1"));
		RuleSet rs2 = new RuleSet();
		rs2.getProperties().add(rule);
		rs2.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects/2"));

		ExecutableRule exec1 = new ExecutableRule(rs1, ds, DATA_FRAGMENT_1);
		ExecutableRule exec2 = new ExecutableRule(rs2, ds, DATA_FRAGMENT_2);

		ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
		forkJoinPool.submit(exec1);
		forkJoinPool.submit(exec2);


		forkJoinPool.awaitTermination(500, TimeUnit.DAYS);
		Assert.assertFalse(Configuration.HELIO_CACHE.getGraphs().isEmpty());
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testAddDataAndQuery() throws IOException {
		Configuration.HELIO_CACHE.deleteGraphs();

		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		JsonObject object1 = (new Gson()).fromJson("{\"file\" : \"./src/test/resources/json-file-1.json\"}", JsonObject.class);
		DataProvider memoryProvider = new FileProvider(object1);
		JsonObject object = (new Gson()).fromJson("{\"iterator\" : \"$.book[*]\"}", JsonObject.class);
		DataHandler jsonHandler = new JsonHandler();
		jsonHandler.configure(object);
		DataSource ds = new DataSource("test1", jsonHandler, memoryProvider, null);

		Rule rule = new Rule();
		rule.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/name"));
		rule.setObject(new EvaluableExpression("{$.title}") );
		rule.setIsLiteral(true);
		RuleSet rs = new RuleSet();
		rs.getProperties().add(rule);
		rs.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects"));

		ExecutableRule exec = new ExecutableRule(rs, ds, DATA_FRAGMENT_1);

		exec.generateRDF("http://fake-subject.es/objects", rule);

		String  input = Configuration.HELIO_CACHE.solveTupleQuery("SELECT DISTINCT ?s { ?s ?p ?o .}", SparqlResultsFormat.JSON);

		Assert.assertFalse(input.isEmpty());
		Assert.assertFalse(Configuration.HELIO_CACHE.getGraphs().isEmpty());
		Configuration.HELIO_CACHE.deleteGraphs();
	}

	@Test
	public void testAddDataParallelAndQuery() throws IOException, InterruptedException {
		Configuration.HELIO_CACHE.deleteGraphs();

		Assert.assertTrue(Configuration.HELIO_CACHE.getGraphs().isEmpty());

		JsonObject object1 = (new Gson()).fromJson("{\"file\" : \"./src/test/resources/json-file-1.json\"}", JsonObject.class);
		DataProvider memoryProvider = new FileProvider(object1);
		JsonObject object = (new Gson()).fromJson("{\"iterator\" : \"$.book[*]\"}", JsonObject.class);
		DataHandler jsonHandler = new JsonHandler();
		jsonHandler.configure(object);
		DataSource ds = new DataSource("test1", jsonHandler, memoryProvider, null);

		Rule rule = new Rule();
		rule.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/name"));
		rule.setObject(new EvaluableExpression("{$.title}") );
		rule.setIsLiteral(true);

		RuleSet rs1 = new RuleSet();
		rs1.getProperties().add(rule);
		rs1.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects/1"));
		RuleSet rs2 = new RuleSet();
		rs2.getProperties().add(rule);
		rs2.setSubjectTemplate(new EvaluableExpression("http://fake-subject.es/objects/2"));

		ExecutableRule exec1 = new ExecutableRule(rs1, ds, DATA_FRAGMENT_1);
		ExecutableRule exec2 = new ExecutableRule(rs2, ds, DATA_FRAGMENT_2);

		ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
		forkJoinPool.submit(exec1);
		forkJoinPool.submit(exec2);

		forkJoinPool.awaitTermination(500, TimeUnit.DAYS);

		String  input = Configuration.HELIO_CACHE.solveTupleQuery("SELECT DISTINCT ?s { ?s ?p ?o .}", SparqlResultsFormat.JSON);

		Assert.assertFalse(input.isEmpty());
		Assert.assertFalse(Configuration.HELIO_CACHE.getGraphs().isEmpty());
		Configuration.HELIO_CACHE.deleteGraphs();
	}




}
