package helio.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.Helio;
import helio.Mappings;
import helio.Utils;
import helio.blueprints.ComponentType;
import helio.blueprints.Components;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.mappings.Mapping;
import helio.configuration.Configuration;
import sparql.streamline.core.SparqlEndpointConfiguration;

public class TestUtils {

	static Logger logger = LoggerFactory.getLogger(TestUtils.class);
	static 	String file = "./src/test/resources/test-"+UUID.randomUUID().toString()+".md";
	static Helio helio;
	static {
		try {
			Components.registerAndLoad("/Users/andreacimmino/Desktop/helio-handler-csv-0.0.2.jar", "handlers.CsvHandler",ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-handler-jayway/releases/download/v0.0.2/helio-handler-jayway-0.0.2.jar", "handlers.JsonHandler",ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-handler-jsoup/releases/download/v0.0.1/helio-handler-jsoup-0.0.1.jar", "handlers.JsoupHandler",ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-provider-url/releases/download/v0.0.1/helio-provider-url-0.0.1.jar", "provider.URLProvider", ComponentType.PROVIDER); 
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-handler-regex/releases/download/v0.0.1/helio-handler-regex-0.0.1.jar", "handlers.RegexHandler",ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-handler-xml/releases/download/v0.0.1/helio-handler-xml-0.0.1.jar", "handlers.XmlHandler",ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-reader-rml/releases/download/v0.0.4/helio-reader-rml-0.0.4.jar", "readers.RmlReader", ComponentType.READER);
		}catch(Exception e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad("https://github.com/helio-ecosystem/helio-provider-file/releases/download/v.0.0.1/helio-provider-file-0.0.1.jar",  "providers.FileProvider", ComponentType.PROVIDER);
		}catch(Exception e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(null, "helio.components.functions.HF", ComponentType.FUNCTION);
			Components.registerAndLoad(null, "helio.components.handlers.RDFHandler", ComponentType.HANDLER);
			Components.registerAndLoad(null, "helio.components.readers.JsonReader", ComponentType.READER);
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		helio = new Helio();
		SparqlEndpointConfiguration conf = new SparqlEndpointConfiguration();
		conf.setEndpointQuery("http://localhost:7200/repositories/app");
		conf.setEndpointUpdate("http://localhost:7200/repositories/app/statements");
		helio.setSparqlEndpointConfiguration(conf);
		helio.getConfiguration().setEagerLiteralValidation(false);
		helio.getConfiguration().setSilentAcceptanceOfUnknownDatatypes(true);
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
				bf.lines().forEach( line -> data.append(line).append("\n"));
				bf.close();
				file.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
			return data.toString();
	 }

	 private static final String SEPARATOR = ";";
	public static Model generateRDFSynchronously(String mappingFile) {
		Model model = ModelFactory.createDefaultModel();

		try {
			StringBuilder line = new StringBuilder();
			line.append("Reading mapping file; Instantiating mapping; Creating unit; Translating; Fetching RDF ## ");
			long startTime1 = System.nanoTime();
			String mappingContent = readFile(mappingFile);
			long endTime1 = System.nanoTime();
			long duration1 = (endTime1 - startTime1) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(duration1);
			long startTime11 = System.nanoTime();
			Mapping mapping = Mappings.readMapping(mappingContent);
			long endTime11 = System.nanoTime();
			long duration11 = (endTime11 - startTime11) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration11);

			long startTime2 = System.nanoTime();
			helio.addMapping(mapping);
			long endTime2 = System.nanoTime();
			long duration2 = (endTime2 - startTime2) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration2);

			long startTime4 = System.nanoTime();
			helio.runSynchronous();
			long endTime4 = System.nanoTime();
			long duration4 = (endTime4 - startTime4) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration4);

			long startTime3 = System.nanoTime();
			model.read(new ByteArrayInputStream(helio.getRDF(mapping, ResultsFormat.FMT_RDF_NT).toByteArray()), helio.getConfiguration().getNamespace(), "NT");
			long endTime3 = System.nanoTime();
			long duration3 = (endTime3 - startTime3) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration3);
			logger.info(line.toString());
		}catch(Exception e) {
			e.printStackTrace();
		}
		return model;
	}


	public static Boolean compareModels(Model model1, Model model2) {
		if(model1==null || model2==null || model1.isEmpty() || model2.isEmpty())
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
			if(!aux) {
				result = false;
				//System.out.println("Not present in model 2:"+ element);
				//break;
			}}catch(Exception e) {
				System.out.println(element);
				System.out.println(query);
				System.out.println(e.toString());
			}
		}
		return result;
	}

	private static boolean compare(RDFNode obj1, RDFNode obj2) {
		Boolean equal = false;
		if(obj1.isLiteral() && obj2.isLiteral()) {
			equal = obj1.asLiteral().getLexicalForm().equals(obj2.asLiteral().getLexicalForm());
		}else if(obj1.isResource() && obj2.isResource() && !obj1.isAnon() && !obj2.isAnon()){
			equal = obj1.equals(obj2);
		}if( obj1.isAnon() && obj2.isAnon()) {
			equal = true;
		}

		return equal;
	}

}
