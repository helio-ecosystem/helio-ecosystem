package helio.materialiser.engine.mappings;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import blueprints.expressions.EvaluableExpression;
import helio.Configuration;
import helio.blueprints.components.DataProvider;
import helio.components.handlers.JsonHandler;
import helio.components.providers.InMemoryProvider;

public class ExecutableMappingTest {

	private static final String JSON_DOCUMENT = "{\n" +
			"    \"book\": \n" +
			"    [\n" +
			"        {\n" +
			"            \"title\": \"Beginning JSON\",\n" +
			"            \"author\": \"Ben Smith\",\n" +
			"            \"price\": 49.99\n" +
			"        },\n" +
			" \n" +
			"        {\n" +
			"            \"title\": \"JSON at Work\",\n" +
			"            \"author\": \"Tom Marrs\",\n" +
			"            \"price\": 29.99\n" +
			"        },\n" +
			" \n" +
			"        {\n" +
			"            \"title\": \"Learn JSON in a DAY\",\n" +
			"            \"author\": \"Acodemy\",\n" +
			"            \"price\": 8.99\n" +
			"        },\n" +
			" \n" +
			"        {\n" +
			"            \"title\": \"JSON: Questions and Answers\",\n" +
			"            \"author\": \"George Duckett\",\n" +
			"            \"price\": 6.00\n" +
			"        },\n" +
			"        {\n" +
			"            \"author\": \"George Duckett 2\",\n" +
			"            \"price\": 6.00\n" +
			"        }\n" +
			"    ],\n" +
			" \n" +
			"    \"price range\": \n" +
			"    {\n" +
			"        \"cheap\": 10.00,\n" +
			"        \"medium\": 20.00\n" +
			"    }\n" +
			"}";


	@Test
	public void test() throws IOException {
		PipedOutputStream output = new PipedOutputStream();
		PipedInputStream input = new PipedInputStream(output);

		output.write(JSON_DOCUMENT.getBytes());
		output.close();

		DataProvider memoryProvider = new InMemoryProvider(input);
		DataHandler jsonHandler = new JsonHandler("$.book[*]");
		DataSource ds = new DataSource("test",jsonHandler, memoryProvider);
		List<RuleSet> ruleSets = instantiateRuleSet();

		AsyncronousTranslationTask exec = new AsyncronousTranslationTask(ds, ruleSets);

		exec.generateRDFSynchronously();

		Model model = Configuration.HELIO_CACHE.getGraphs();
		Assert.assertFalse(model.isEmpty());

	}

	private List<RuleSet> instantiateRuleSet(){
		List<RuleSet> results = new ArrayList<>();
		RuleSet ruleSet = new RuleSet();
		ruleSet.setResourceRuleId("This is the id");
		ruleSet.setSubjectTemplate(new EvaluableExpression("http://fakeDomain.com/{$.tile}") );
		Set<String> arrayList = new HashSet<>();
		arrayList.add("one datasource id");
		ruleSet.setDatasourcesId(arrayList);
		// add properties
		Rule rule1 = new Rule();
		rule1.setPredicate(new EvaluableExpression("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"));
		rule1.setObject(new EvaluableExpression("http://xmlns.com/foaf/0.1/book"));
		rule1.setIsLiteral(false);
		Rule rule2 = new Rule();
		rule2.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/title"));
		rule2.setObject(new EvaluableExpression("{$.title}") );
		rule2.setIsLiteral(true);
		Rule rule3 = new Rule();
		rule3.setPredicate(new EvaluableExpression("http://xmlns.com/foaf/0.1/price"));
		rule3.setObject(new EvaluableExpression("{$.price}") );
		rule3.setIsLiteral(true);
		ruleSet.getProperties().add(rule1);
		ruleSet.getProperties().add(rule2);
		ruleSet.getProperties().add(rule3);
		results.add(ruleSet);

		return results;

	}

}
