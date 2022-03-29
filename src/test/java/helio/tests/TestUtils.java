package helio.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.AbstractHelio;
import helio.Helio;
import helio.Utils;
import helio.blueprints.ComponentType;
import helio.blueprints.Components;
import helio.blueprints.components.MappingProcessor;
import helio.blueprints.components.TranslationUnit;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.MappingExecutionException;
import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.core.SparqlEndpointConfiguration;

public class TestUtils {

	static Logger logger = LoggerFactory.getLogger(TestUtils.class);
	static String file = "./src/test/resources/test-" + UUID.randomUUID().toString() + ".md";
	static Helio helio;
	static SparqlEndpointConfiguration conf = new SparqlEndpointConfiguration();
	static SparqlEndpoint sparql;
	static {
		try {
			Components.registerAndLoad("/Users/andreacimmino/Desktop/helio-handler-csv-0.0.3.jar",
					"handlers.CsvHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-handler-jayway/releases/download/v0.0.2/helio-handler-jayway-0.0.2.jar",
					"handlers.JsonHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-handler-jsoup/releases/download/v0.0.1/helio-handler-jsoup-0.0.1.jar",
					"handlers.JsoupHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-provider-url/releases/download/v0.0.1/helio-provider-url-0.0.1.jar",
					"provider.URLProvider", ComponentType.PROVIDER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-handler-regex/releases/download/v0.0.1/helio-handler-regex-0.0.1.jar",
					"handlers.RegexHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-handler-xml/releases/download/v0.0.1/helio-handler-xml-0.0.1.jar",
					"handlers.XmlHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}

		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-provider-file/releases/download/v.0.0.1/helio-provider-file-0.0.1.jar",
					"providers.FileProvider", ComponentType.PROVIDER);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"/Users/andreacimmino/Desktop/Lab/json-mapping/target/helio-processor-jmapping-0.2.1-jar-with-dependencies.jar",
					"helio.jmapping.processor.JMappingProcessor", ComponentType.PROCESSOR);
			Components.registerAndLoad("/Users/andreacimmino/Desktop/Lab/json-mapping/target/helio-processor-jmapping-0.2.1-jar-with-dependencies.jar", "helio.jmapping.functions.HF", ComponentType.FUNCTION);
			Components.registerAndLoad("/Users/andreacimmino/Desktop/Lab/json-mapping/target/helio-processor-jmapping-0.2.1-jar-with-dependencies.jar", "helio.jmapping.RDFHandler", ComponentType.HANDLER);

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Components.registerAndLoad(null, "helio.components.handlers.RDFHandler", ComponentType.HANDLER);
		} catch (Exception e) {
			e.printStackTrace();
		}

		helio = new Helio();
		conf.setEndpointQuery("http://localhost:7200/repositories/app");
		conf.setEndpointUpdate("http://localhost:7200/repositories/app/statements");
		sparql = new SparqlEndpoint(conf);
		AbstractHelio.setEagerLiteralValidation(false);
		AbstractHelio.setSilentAcceptanceOfUnknownDatatypes(true);
	}

	public static Model readModel(String file) {
		FileInputStream out;
		Model expected = ModelFactory.createDefaultModel();
		try {
			out = new FileInputStream(file);
			expected = ModelFactory.createDefaultModel();
			expected.read(out, "http://helio.linkeddata.es/resources/", "TURTLE");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return expected;
	}

	public static String readFile(String fileName) {
		StringBuilder data = new StringBuilder();
		// 1. Read the file
		try {
			FileReader file = new FileReader(fileName);
			BufferedReader bf = new BufferedReader(file);
			// 2. Accumulate its lines in the data var
			bf.lines().forEach(line -> data.append(line).append("\n"));
			bf.close();
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data.toString();
	}

	public static Set<TranslationUnit> processJMapping(String mappingFile) throws IncompatibleMappingException, MappingExecutionException, IncorrectMappingException, ExtensionNotFoundException {
		String mappingContent = readFile(mappingFile);
		MappingProcessor processor = Components.getMappingProcessors().get("JMappingProcessor");
		return processor.parseMapping(mappingContent);
		
	}

	public static Model generateRDFSynchronously(Set<TranslationUnit> units) {
		Model model = ModelFactory.createDefaultModel();
		try {
			
			long startTime2 = System.nanoTime();
			units.parallelStream().forEach(unit -> helio.add(unit));
			long endTime2 = (System.nanoTime()- startTime2) / 1000000;
			System.out.println("Loading units: "+endTime2);
			
			long startTime4 = System.nanoTime();
			model = helio.runSynchronous();
			long endTime4 = (System.nanoTime()- startTime4) / 1000000;
			System.out.println("Generating RDF: "+endTime4);
			
			long startTime3 = System.nanoTime();
			
			long endTime3 = (System.nanoTime() - startTime3) / 1000000;
			System.out.println("Reading RDF: "+endTime3);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}
	
	public static Model fetchData(List<String> ids) {
		Model model = ModelFactory.createDefaultModel();
		String query = Utils.concatenate("CONSTRUCT {?s ?p ?o} WHERE { GRAPH ?g { ?s ?p ?o} VALUES ?g ",ids.toString().replace('[', '{').replace(']', '}').replace(',', ' '),"}");
		try {
			model.read(new ByteArrayInputStream(sparql.query(query, ResultsFormat.FMT_RDF_NT).toByteArray()),null,"NT");
		}catch(Exception e) {
			System.out.println(query);
			e.printStackTrace();
		}
			return model;
	}

	public static Boolean compareModels(Model model1, Model model2) {
		if (model1 == null || model2 == null || model1.isEmpty() || model2.isEmpty())
			return false;
		Boolean model2Contains1 = contains(model1, model2);
		System.out.println("-----");
		Boolean model1Contains2 = contains(model2, model1);

		return model2Contains1 && model1Contains2;
	}

	public static Boolean contains(Model model1, Model model2) {
		Writer writer = new StringWriter();
		model1.write(writer, "NT");
		String[] triplet = writer.toString().split("\n");
		boolean result = true;
		for (String element : triplet) {
			String query = Utils.concatenate("ASK {\n", element, "\n}");
			try {
				Boolean aux = QueryExecutionFactory.create(query, model2).execAsk();
				if (!aux) {
					result = false;
					// System.out.println("Not present in model 2:"+ element);
					// break;
				}
			} catch (Exception e) {
				System.out.println(element);
				System.out.println(query);
				System.out.println(e.toString());
			}
		}
		return result;
	}

	private static boolean compare(RDFNode obj1, RDFNode obj2) {
		Boolean equal = false;
		if (obj1.isLiteral() && obj2.isLiteral()) {
			equal = obj1.asLiteral().getLexicalForm().equals(obj2.asLiteral().getLexicalForm());
		} else if (obj1.isResource() && obj2.isResource() && !obj1.isAnon() && !obj2.isAnon()) {
			equal = obj1.equals(obj2);
		}
		if (obj1.isAnon() && obj2.isAnon()) {
			equal = true;
		}

		return equal;
	}

}
