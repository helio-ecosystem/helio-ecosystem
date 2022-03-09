//package helio.configuration;
//
//import java.io.ByteArrayOutputStream;
//import java.util.Timer;
//
//import org.apache.jena.sparql.resultset.ResultsFormat;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import helio.Helio;
//import helio.Utils;
//import helio.blueprints.exceptions.ExtensionNotFoundException;
//import helio.blueprints.exceptions.IncorrectMappingException;
//import helio.blueprints.mappings.Mapping;
//import helio.exceptions.ConfigurationException;
//import sparql.streamline.exception.SparqlConfigurationException;
//
///**
// * This class is an implementation of Helio
// * @author Andrea Cimmino
// *
// */
//public class HelioImpl {
//
//	public static Logger logger = LoggerFactory.getLogger(HelioImpl.class);
//
//	private static Helio translationManager = new Helio();
//	public static Timer time = new Timer();
//
//
//	private HelioImpl() {
//		super();
//	}
//
//	// TODO: TEST ASYNC AND SCHEDULED
//	public static void addTranslationsTasks(Mapping mapping) throws IncorrectMappingException, ExtensionNotFoundException, SparqlConfigurationException, ConfigurationException {
//		if(mapping==null)
//			throw new IncorrectMappingException("Provided mapping can not be null");
//		mapping.checkMapping();
//		translationManager.createFrom(mapping);
//	}
//
//	public static void removeTranslationsTask(String id) {
//		translationManager.delete(id);
//	}
//
//	public static Helio getTranslationManager() {
//		return translationManager;
//	}
//
//
//	public static void translate() {
//		translationManager.runSynchronous();
//	}
//
//	public static void translate(String subject) {
//		translationManager.runSynchronous(subject);
//	}
//
//	
//
//	// TODO: do similar with translation rule id
//
//
//	public static void close() {
//		time.cancel();
//		time.purge();
//	}
//
//
//
//
//
//
//
//
//}
