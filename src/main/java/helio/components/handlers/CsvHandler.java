package helio.components.handlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import helio.Utils;
import helio.blueprints.components.DataHandler;

/**
 * This object implements the {@link DataHandler} interface allowing to handle CSV data. It allows to reference data allocated in a column by using the column name, or, the column index starting from 0 as first index.
 * This object can be configured with a {@link JsonObject} that may contain three keys: 'separator' (mandatory) specifies the character used to separate the columns (default is ';'), 'delimitator' (optional) specifies the characters used to delimit the columns text (null by default), and 'has_headers' (optional) that specifies if the CSV has headers in the first row (by default is always true).
 * @author Andrea Cimmino
 *
 */
public class CsvHandler implements DataHandler{


	private static final long serialVersionUID = 1L;
	Logger logger = LoggerFactory.getLogger(CsvHandler.class);

	private String separator = ";";
	private String delimitator = "";
	private Boolean hasHeaders = true;
	private Map<String,String> headers = new HashMap<>();


	/**
	 * This constructor initializes the {@link CsvHandler} with the default values for the separator, delimitator and has_headers
	 */
	public CsvHandler() {
		super();
	}

	/**
	 * This constructor initializes the {@link CsvHandler} with a provided separator
	 * @param separator the character used to separate the columns
	 */
	public CsvHandler(String separator) {
		super();
		this.separator = separator;
	}


	@Override
	public Queue<String> splitData(InputStream dataStream) {
		ConcurrentLinkedQueue<String> queueOfresults = new ConcurrentLinkedQueue<>();
		if(dataStream!=null) {
			try {
				// check if CSV has header, also, keep track of the header in order to retrieve later the data based on the header's names
				BufferedReader br = new BufferedReader(new InputStreamReader(dataStream));
				String line;
				boolean firstLine = true;
				while ((line = br.readLine()) != null) {
					if(hasHeaders && firstLine) {
						loadHeaders(line);
						firstLine = false;
					}else {
						if(firstLine) {
							loadHeaderWithNumbers(line);
						}
						queueOfresults.add(line);
					}
				}
				br.close();
				dataStream.close();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		return queueOfresults;
	}

	private void loadHeaderWithNumbers(String line) {
		String[] tokens = line.split(delimitator+separator+delimitator);
		 tokens[0] = tokens[0].replaceAll("^"+delimitator, "");
		 tokens[tokens.length-1] = tokens[tokens.length-1].replaceAll(delimitator+"$", "");
		 for(int index = 0; index < tokens.length; index++) {
			 headers.put(String.valueOf(index), String.valueOf(index));
		 }

	}

	private void loadHeaders(String line) {
		 String[] tokens = line.split(delimitator+separator+delimitator);
		 tokens[0] = tokens[0].replaceAll("^"+delimitator, "");
		 tokens[tokens.length-1] = tokens[tokens.length-1].replaceAll(delimitator+"$", "");
		 for(int index = 0; index < tokens.length; index++)
			headers.put(String.valueOf(tokens[index]).trim(), String.valueOf(index));
	}


	@Override
	public List<String> filter(String filter, String dataChunk) {
		List<String> results = new ArrayList<>();
		String result = null;
		String[] tokens = dataChunk.split(delimitator+separator+delimitator);
		tokens[0] = tokens[0].replaceAll("^"+delimitator, "");
		tokens[tokens.length-1] = tokens[tokens.length-1].replaceAll(delimitator+"$", "");
		try {
			if(headers.containsKey(filter)) {
				 result = tokens[Integer.valueOf(headers.get(filter))];
			}else if(headers.containsValue(filter)) {
				 result = tokens[Integer.valueOf(filter)];
			}else {
				logger.error("Provided CSV data reference provided is not valid, provided reference "+filter+", available ones are: "+headers);
			}
		}catch(Exception e) {
			logger.warn(Utils.concatenate("Error accesing a row in the CSV due missing values in such row. The filter provided was",filter," and the row is ", dataChunk));
		}
		if(result!=null)
			results.add(result);
		return results;
	}

	private static final String SEPARATOR_KEY = "separator";
	private static final String DELIMITATOR_KEY = "delimitator";
	private static final String HEADERS_KEY = "has_headers";
	@Override
	public void configure(JsonObject configuration) {
		if(configuration.has(SEPARATOR_KEY)) {
			this.separator = configuration.get(SEPARATOR_KEY).getAsString();
			if(this.separator.isEmpty())
				throw new IllegalArgumentException("CsvHandler needs to receive non empty value for the key 'separator'");
		}else {
			throw new IllegalArgumentException("CsvHandler needs to receive json object with the mandatory key 'separator'");
		}
		if(configuration.has(DELIMITATOR_KEY)) {
			this.delimitator = configuration.get(DELIMITATOR_KEY).getAsString();
		}else {
			logger.warn("CsvHandler was not provided with a text delimitator, by default empty char will be used");
		}
		if(configuration.has(HEADERS_KEY)) {
			this.hasHeaders = configuration.get(HEADERS_KEY).getAsBoolean();
		}else {
			logger.warn("CsvHandler was not provided with the flag has_headers, by default it will be considered the first line as the columns headers");
		}

	}



}
