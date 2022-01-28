package helio.materialiser.test.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.internal.Utils;

import helio.Configuration;
import helio.Helio;
import helio.Mappings;
import helio.bleprints.mappings.Mapping;

public class TestUtils {

	static Logger logger = LoggerFactory.getLogger(TestUtils.class);
	static 	String file = "./src/test/resources/test-"+UUID.randomUUID().toString()+".md";

	static {
		Helio.configuration = Configuration.createDefault();
	}
	

	public static Model readModel(String file) {
		FileInputStream out;
		Model expected = ModelFactory.createDefaultModel();
		try {
			out = new FileInputStream(file);
			expected = ModelFactory.createDefaultModel();
			expected.read(out, Helio.configuration.getNamespace(), "TURTLE");
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
			Helio.addTranslationsTasks(mapping);
			long endTime2 = System.nanoTime();
			long duration2 = (endTime2 - startTime2) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration2);

			long startTime4 = System.nanoTime();
			Helio.translate();
			long endTime4 = System.nanoTime();
			long duration4 = (endTime4 - startTime4) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration4);
			
			long startTime3 = System.nanoTime();
			model.read(new ByteArrayInputStream(Helio.getRDF(mapping, ResultsFormat.FMT_RDF_NT).toByteArray()), Helio.configuration.getNamespace(), "NT");
			long endTime3 = System.nanoTime();
			long duration3 = (endTime3 - startTime3) / 1000000;  //divide by 1000000 to get milliseconds.
			line.append(SEPARATOR).append(duration3);
			logger.info(line.toString());
			Helio.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return model;
	}


	public static Boolean compareModels(Model model1, Model model2) {
		if(model1==null || model2==null)
			return false;

		String query1 = prepareQuery(model1);
		Boolean model2Contains1 = QueryExecutionFactory.create(query1, model2).execAsk();
		String query2 = prepareQuery(model2);
		Boolean model1Contains2 = QueryExecutionFactory.create(query2, model1).execAsk();


		return model2Contains1 && model1Contains2;
	}

	private static String prepareQuery(Model model) {
		Writer writer = new StringWriter();
		model.write(writer, "NT");
		return Utils.concat("ASK {\n", writer.toString(), "\n}");
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
