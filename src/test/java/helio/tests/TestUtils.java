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

import helio.Helio;
import helio.blueprints.TranslationUnit;
import helio.blueprints.UnitBuilder;
import helio.blueprints.components.ComponentType;
import helio.blueprints.components.Components;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.TranslationUnitExecutionException;

public class TestUtils {

	static Logger logger = LoggerFactory.getLogger(TestUtils.class);

	static {
		try {
			Components.registerAndLoad(
					"/Users/andreacimmino/Desktop/helio-handler-csv-0.1.0.jar",
					"handlers.CsvHandler", 
					ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		try {
			Components.registerAndLoad(
					"https://github.com/helio-ecosystem/helio-handler-jayway/releases/download/v0.1.0/helio-handler-jayway-0.1.0.jar",
					"handlers.JsonHandler", ComponentType.HANDLER);
		} catch (ExtensionNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			Components.registerAndLoad(
					"/Users/andreacimmino/Desktop/helio-processor-jmapping-0.2.4.jar",
					"helio.jmapping.builder.JMappingBuilder", ComponentType.BUILDER);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			Components.registerAndLoad(
					"/Users/andreacimmino/Desktop/helio-builder-jld11map-0.1.2.jar",
					"helio.builder.jld11map.JLD11Builder", ComponentType.BUILDER);
			

		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			Components.registerAndLoad(
					"/Users/andreacimmino/Desktop/helio-provider-files-0.1.0.jar",
					"helio.providers.files.FileProvider", ComponentType.PROVIDER);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
	}

	public static void voidMethod() {
		//
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

	public static Set<TranslationUnit> processJMapping(String mappingFile){
		try {
		long startTime2 = System.nanoTime();
		String mappingContent = readFile(mappingFile);
		UnitBuilder processor = Components.getMappingProcessors().get("JMappingBuilder");
		Set<TranslationUnit> units = processor.parseMapping(mappingContent);
		long endTime2 = (System.nanoTime()- startTime2) / 1000000;
		System.out.println("Building units: "+endTime2);
		return units;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Model generateRDFSynchronously(Set<TranslationUnit> units) {
		Model model = ModelFactory.createDefaultModel();
		Helio helio = new Helio(10);
		try {
			
			long startTime2 = System.nanoTime();
			units.parallelStream().forEach(unit -> {
				try {
					helio.add(unit);
				} catch (TranslationUnitExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			long endTime2 = (System.nanoTime()- startTime2) / 1000000;
			System.out.println("Loading units: "+endTime2);
			
			long startTime4 = System.nanoTime();
			helio.readAndFlushAll().forEach(data -> model.read(new ByteArrayInputStream(data.getBytes()), "", "TURTLE"));
			
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
			String query = "ASK {\n"+ element+ "\n}";
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
