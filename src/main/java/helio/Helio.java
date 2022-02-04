package helio;

import java.io.ByteArrayOutputStream;
import java.util.Timer;

import org.apache.jena.sparql.resultset.ResultsFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Mapping;
import helio.exceptions.SparqlQuerySyntaxException;
import helio.exceptions.SparqlRemoteEndpointException;
import helio.sparql.Sparql;
import helio.translation.TranslationManager;

/**
 * This class is an implementation of the interface {@link MaterialiserEngine}
 * @author Andrea Cimmino
 *
 */
public class Helio {

	public static Logger logger = LoggerFactory.getLogger(Helio.class);

	public static Configuration configuration = new Configuration();
	private static TranslationManager translationManager = new TranslationManager();
	public static Timer time = new Timer();


	private Helio() {
		super();
	}

	// TODO: TEST ASYNC AND SCHEDULED
	public static void addTranslationsTasks(Mapping mapping) throws IncorrectMappingException {
		if(mapping==null)
			throw new IncorrectMappingException("Provided mapping can not be null");
		mapping.checkMapping();
		translationManager.createFrom(mapping);
	}

	public static void removeTranslationsTask(String id) {
		translationManager.delete(id);
	}

	public static TranslationManager getTranslationManager() {
		return translationManager;
	}


	public static void translate() {
		translationManager.runSynchronous();
	}

	public static void translate(String subject) {
		translationManager.runSynchronous(subject);
	}

	public static ByteArrayOutputStream query(String query, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		return Sparql.query(query, format);
	}

	private static final String BULK_QUERY = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
	public static ByteArrayOutputStream getRDF(ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		return Sparql.query(BULK_QUERY, format);
	}

	// Advanced methods:
	private static final String BULK_QUERY_2_1 = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o . } filter(";
	private static final String BULK_QUERY_2_2 = ") }";
	private static final String BULK_QUERY_2_FILTER_1 = "|| STRENDS( str(?g), \"?rule=";
	private static final String BULK_QUERY_2_FILTER_2 ="\")";
	/**
	 * This method retrieves only RDF that is generated as result of a {@link Mapping}
	 * @param mapping
	 * @param format
	 * @return
	 * @throws SparqlQuerySyntaxException
	 * @throws SparqlRemoteEndpointException
	 */
	public static ByteArrayOutputStream getRDF(Mapping mapping, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		StringBuilder builder = new StringBuilder(BULK_QUERY_2_1);
		mapping.getTranslationRules()
			.parallelStream().map(tr -> Utils.mapTranslationRulesId(tr.getId()))
			.map(id -> Utils.concatenate(BULK_QUERY_2_FILTER_1,id,BULK_QUERY_2_FILTER_2))
			.forEach(elem -> builder.append(elem));
		String query = builder.append(BULK_QUERY_2_2).toString();
		query = query.replaceAll("\\(\\|\\|", "(");
		return Sparql.query(query, format);
	}

	// TODO: do similar with translation rule id


	public static void close() {
		time.cancel();
		time.purge();
	}








}
