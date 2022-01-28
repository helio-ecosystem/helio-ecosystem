package helio.components.handlers;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.google.gson.JsonObject;

import helio.blueprints.components.DataHandler;

/**
 * This object implements the {@link DataHandler} interface allowing to handle XML documents. It allows to reference data allocated in an XML document using the standardized <a href="https://www.w3.org/TR/1999/REC-xpath-19991116/">XPath</a> expressions.
 * This object can be configured with a {@link JsonObject} that must contain the key 'iterator' which value is an XPath used to split the XML document into sub-documents.
 * @author Andrea Cimmino
 *
 */
public class XmlHandler implements DataHandler{

	private static final long serialVersionUID = 1L;
	Logger logger = LoggerFactory.getLogger(JsonHandler.class);
	private static final String CONFIGURATION_KEY = "iterator";
	private String iterator;
	private 	final XPath XPATH = XPathFactory.newInstance().newXPath();

	/**
	 * This constructor creates an empty {@link XmlHandler} that will need to be configured using a valid {@link JsonObject}
	 */
	public XmlHandler() {
		super();
	}

	/**
	 * This constructor instantiates a valid {@link XmlHandler} with the provided iterator
	 * @param iterator a valid XPath expression
	 */
	public XmlHandler(String iterator) {
		this.iterator = iterator;
	}


	@Override
	public Queue<String> splitData(InputStream dataStream) {
		ConcurrentLinkedQueue<String> queueOfresults = new ConcurrentLinkedQueue<>();
		if(dataStream!=null) {
			try {
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(dataStream));
				// 3. Compile XPath
				XPathExpression expr =  XPATH.compile(iterator);
				// 4. Evaluate XPath in the document
				NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
				if(nodes.getLength()>0) {
					// 5. Transform each resultant node into a string xml document
					for(int index=nodes.getLength()-1; index >= 0 ; index--) {
						Node node = nodes.item(index);
						Document subXmlDocument = node.getOwnerDocument();
						DOMImplementationLS domImplLS = (DOMImplementationLS) subXmlDocument.getImplementation();
						LSSerializer serializer = domImplLS.createLSSerializer();
						String stringXmlDocument = serializer.writeToString(node);
						queueOfresults.add(stringXmlDocument);
					}
				}else {
					logger.warn("Given xPath expression does not match in the document");
				}
			} catch (Exception e) {
				logger.warn(e.toString());
			}
		}
		return queueOfresults;
	}

	@Override
	public List<String> filter(String filter, String dataChunk) {
		List<String> results = new ArrayList<>();
		try {
			// 2. Create XML document
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					.parse(new InputSource(new StringReader(dataChunk)));
			// 3. Compile XPath
			XPathExpression expr = XPATH.compile(filter);
			// 3. Evaluate XPath in the document
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			if (nodes.getLength() == 0) {
				logger.warn("xPath "+filter+" retrieved zero values from original document: "+dataChunk);
			} else {
				for (int index = nodes.getLength() - 1; index >= 0; index--)
					results.add(nodes.item(0).getTextContent());

			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return results;
	}

	@Override
	public void configure(JsonObject configuration) {
		if(configuration.has(CONFIGURATION_KEY)) {
			iterator = configuration.get(CONFIGURATION_KEY).getAsString();
			if(iterator.isEmpty())
				throw new IllegalArgumentException("XmlHandler needs to receive non empty value for the keey 'iterator'");
		}else {
			throw new IllegalArgumentException("XmlHandler needs to receive json object with the mandatory key 'iterator'");
		}
	}


}
