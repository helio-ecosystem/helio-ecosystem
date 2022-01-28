package helio.components.handlers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.json.JSONObject;
import org.json.XML;

import com.google.gson.JsonObject;

import helio.blueprints.components.DataHandler;

public class XmlAsJsonHandler implements DataHandler{

	private static final long serialVersionUID = 1L;
	private JsonHandler jsonHandler;


	public XmlAsJsonHandler() {
		jsonHandler = new JsonHandler();
	}


	@Override
	public Queue<String> splitData(InputStream dataStream) {
		Queue<String> queueOfresults = new ConcurrentLinkedQueue<>();
		if(dataStream!=null) {
		    Reader streamReader = new InputStreamReader(dataStream);
			JSONObject equivalentJson = XML.toJSONObject(streamReader);
		    InputStream targetStream = new ByteArrayInputStream(equivalentJson.toString().getBytes());
			queueOfresults = jsonHandler.splitData(targetStream);
		}
		return queueOfresults;
	}


	@Override
	public List<String> filter(String filter, String dataChunk) {
		return jsonHandler.filter(filter, dataChunk);
	}

	@Override
	public void configure(JsonObject config) {
		jsonHandler.configure(config);

	}



}
