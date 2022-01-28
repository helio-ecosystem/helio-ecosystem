package helio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * This class provides a set of methods that ease the code writing
 * @author Andrea Cimmino
 *
 */
public class Utils {

	public static final Gson GSON;
	static {
		GSON = new GsonBuilder()
				  .excludeFieldsWithoutExposeAnnotation()
				  .create();
	}
	
	private Utils() {
		super();
	}

	/**
	 * This method concatenates a set of strings efficiently in memory
	 * @param str a set of {@link String} values
 	 * @return a unique {@link String} concatenating all the input string values provided
	 */
	public static String concatenate(String ... str) {
		StringBuilder builder = new StringBuilder();
		for (String element : str) {
			builder.append(element);
		}
		return builder.toString();
	}

	/**
	 * This method checks whether a URL is correctly formed
	 * @param urlStr a set of {@link String} value containing the URL
	 * @return a boolean value that is true if the URL is correct, or false otherwise
	 */
	public static boolean isValidURL(String urlStr) {
	    try {
	    		if(urlStr == null || urlStr.contains(" ") || urlStr.isEmpty())
	    			throw new MalformedURLException();
	        new URL(urlStr);
	        return true;
	      }
	      catch (MalformedURLException e) {
	          return false;
	      }
	  }

	/**
	 * This method creates the a graph name for a subject taking into account its related {@link DataSource} and {@link RuleSet} ids.<p>
	 * The graph identifier created should be used to name a graph that contains all the triples related to the subject.
	 * @param subject an RDF subject, i.e., a valid URI
	 * @param datasourceId the id of a {@link DataSource}
	 * @param ruleSetId the id of a {@link RuleSet}
	 * @return a valid URI that identifies the graph where all the triples related to a subject should be stored
	 */
	public static String createGraphIdentifier(String subject, String datasourceId, String ruleSetId) {
		StringBuilder builder = new StringBuilder();
		builder.append(subject);
		if(!subject.endsWith("/"))
			builder.append("/");
		builder.append(String.valueOf(datasourceId.hashCode()).replace("-", "0"));
		builder.append("/");
		builder.append(String.valueOf(ruleSetId.hashCode()).replace("-", "0"));
		return builder.toString();
	}

	/**
	 * This method reads the content of a file
	 * @param fileName the file directory
	 * @return the content read from the file
	 */
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

	public static void parseRDF(String turtleContent, Model model) {
		InputStream stream = new ByteArrayInputStream(turtleContent.getBytes());
		model.read(stream, null, "TTl");
	}

	public static String mapTranslationRulesId(String rulesId) {
		return String.valueOf(rulesId.hashCode()).replace('-', '0');
	}
	

	

	public static JsonObject toJsonObject(String jsonObject) {
		return GSON.fromJson(jsonObject, JsonObject.class);
	}

	public static String toJsonString(Map<String,Object> brand) {
		return GSON.toJson(brand);
	}

}
