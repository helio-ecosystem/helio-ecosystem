package helio.components.handlers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import helio.blueprints.components.DataHandler;

/**
 * This object implements the {@link DataHandler} interface allowing to handle Json documents. It allows to reference data allocated in an Json document using <a href="https://support.smartbear.com/alertsite/docs/monitors/api/endpoint/jsonpath.html">Json Path</a> expressions. An online tool to check whether a Json Path correctly references data from a Json object can be found at <a href="https://jsonpath.com/">https://jsonpath.com/</a>
 * This object can be configured with a {@link JsonObject} that must contain the key 'iterator' which value is a Json Path used to split the Json document into sub-documents.
 * @author Andrea Cimmino
 *
 */
public class JsonHandler implements DataHandler {

	private static final long serialVersionUID = 1L;
	private static final Gson GSON = new Gson();
	private String iterator;
	Logger logger = LoggerFactory.getLogger(JsonHandler.class);
	private static final String CONFIGURATION_KEY = "iterator";

	/**
	 * This constructor creates an empty {@link JsonHandler} that will need to be configured using a valid {@link JsonObject}
	 */
	public JsonHandler() {
		super();
	}

	/**
	 * This constructor instantiates a valid {@link JsonHandler} with the provided iterator
	 * @param iterator a valid Json Path expression
	 */
	public JsonHandler(String iterator) {
		this.iterator = iterator;
	}

	public String getIterator() {
		return iterator;
	}

	public void setIterator(String iterator) {
		this.iterator = iterator;
	}

	@Override
	public Queue<String> splitData(InputStream dataStream) {
		ConcurrentLinkedQueue<String> queueOfresults = new ConcurrentLinkedQueue<>();
		if(dataStream!=null) {
			Configuration conf =  Configuration.defaultConfiguration()
												.addOptions(Option.ALWAYS_RETURN_LIST)
												.addOptions(Option.REQUIRE_PROPERTIES)
												.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
			try {
				List<Map<String,String>> results = JsonPath.using(conf).parse(dataStream).read(iterator);
				for (Map<String, String> map : results) {
					if( map != null && !map.isEmpty())
						queueOfresults.add(GSON.toJson(map));
				}

				dataStream.close();
			} catch (Exception e) {
				logger.error(e.toString());
			}
		}
		return queueOfresults;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> filter(String filter, String dataChunk) {

		Configuration conf = Configuration.defaultConfiguration()
											.addOptions(Option.REQUIRE_PROPERTIES)
											.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);
		List<String> results = new ArrayList<>();
		try {
			Object parsed = JsonPath.using(conf).parse(dataChunk).read(filter);
			if(parsed!=null) {
				if (parsed instanceof Collection) {
					List<Object> resultsAux = (List<Object>) parsed;
					resultsAux.stream().filter(elem -> elem!=null).forEach(elem -> results.add(elem.toString()));
				}else {
					results.add(String.valueOf(parsed));
				}
			}
		}catch(Exception e) {
			logger.error(e.toString());
		}

		return results;
	}

	@Override
	public void configure(JsonObject arguments) {
		if(arguments.has(CONFIGURATION_KEY)) {
			iterator = arguments.get(CONFIGURATION_KEY).getAsString();
			if(iterator.isEmpty())
				throw new IllegalArgumentException("JsonHandler needs to receive non empty value for the keey 'iterator'");
		}else {
			throw new IllegalArgumentException("JsonHandler needs to receive json object with the mandatory key 'iterator'");
		}

	}



}
