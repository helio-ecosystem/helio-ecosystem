package helio.components.handlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import helio.blueprints.components.DataHandler;

/**
 * This object implements the {@link DataHandler} interface allowing to handle HTML documents. It allows to reference data allocated in an HTML document using DOM traversal, CSS selectors, or HTML elements. The expressions allowed are documented the ones implemented and documented by <a href="https://jsoup.org/">Jsoup</a>
 * This object can be configured with a {@link JsonObject} that must contain the key 'iterator' which value is a Jsoup valid expression used to split the document HTML into sub-documents.
 * @author Andrea Cimmino
 *
 */
public class HtmlHandler implements DataHandler {

	private static final long serialVersionUID = 1L;
	private String iterator;
	private static final String ITERATOR_KEY = "iterator";
	Logger logger = LoggerFactory.getLogger(HtmlHandler.class);

	/**
	 * This constructor creates an empty {@link HtmlHandler} that will need to be configured using a valid {@link JsonObject}
	 */
	public HtmlHandler() {
		super();
	}

	@Override
	public Queue<String> splitData(InputStream dataStream) {
		ConcurrentLinkedQueue<String> queueOfresults = new ConcurrentLinkedQueue<>();
		if(dataStream!=null) {
			String htmlDocument = readHTMLDocument(dataStream);
			if(htmlDocument!=null && !htmlDocument.isEmpty()) {
				// 2. Parse raw data into HTML Document
				Document doc = Jsoup.parse(htmlDocument);
				// 3. Apply expression
				Elements elements = doc.select(this.iterator);
				// 4. Retrieve chunks of data
				Iterator<Element> it = elements.iterator();
				while(it.hasNext()) {
					Element element = it.next();
					queueOfresults.add(element.toString());
				}
			}else {
				logger.warn("Given expression does not match anything in the retrieved html document");
			}
		}
		return queueOfresults;
	}

	public String readHTMLDocument(InputStream dataStream) {
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(dataStream));
			String line;
			while ((line = br.readLine()) != null) {
				builder.append(line);
			}
			br.close();
			dataStream.close();
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return builder.toString();
	}

	@Override
	public List<String> filter(String filter, String dataChunk) {
		List<String> results = null;
		try {
			// 1. Parse raw data into HTML Document
			Document doc = Jsoup.parse(dataChunk);
			// 2. Apply expression
			Elements elements = doc.select(filter);
			// 3. Retrieve data values
			Iterator<Element> elementsIterator = elements.iterator();
			results = new ArrayList<>();
			int index = 0;
			while(elementsIterator.hasNext()) {
				Element element = elementsIterator.next();
				results.add(element.html());
				index++;
			}
			if(index == 0) {
				results = null;
				logger.error("Literal not found with the data reference provided: "+filter);
			}
		}catch(Exception e) {
			logger.error("Literal (2) not found with the data reference provided: "+filter);
		}

		return results;
	}

	@Override
	public void configure(JsonObject configuration) {
		if(configuration.has(ITERATOR_KEY)) {
			this.iterator = configuration.get(ITERATOR_KEY).getAsString();
			if(this.iterator.isEmpty())
				throw new IllegalArgumentException("HtmlHandler needs to receive non empty value for the key 'iterator'");
		}else {
			throw new IllegalArgumentException("HtmlHandler needs to receive json object with the mandatory key 'iterator'");
		}

	}


}
