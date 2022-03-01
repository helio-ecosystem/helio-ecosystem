package helio;

import java.io.ByteArrayOutputStream;

import org.apache.jena.sparql.resultset.ResultsFormat;

import helio.blueprints.mappings.Mapping;
import helio.configuration.Configuration;
import helio.exceptions.ConfigurationException;
import sparql.streamline.core.SparqlEndpoint;
import sparql.streamline.core.SparqlEndpointConfiguration;
import sparql.streamline.exception.SparqlConfigurationException;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public abstract class AbstractHelio {

	protected SparqlEndpoint endpoint;
	protected Configuration configuration;
	
	public void setSparqlEndpointConfiguration(SparqlEndpointConfiguration configuration) {
		endpoint = new SparqlEndpoint(configuration);
	}
	
	public SparqlEndpointConfiguration getSparqlEndpointConfiguration() {
		return endpoint.getConfiguration();
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		TranslationUnitImpl.threads = configuration.getThreads();
		this.configuration = configuration;
	}

	protected void checkValidity() throws SparqlConfigurationException, ConfigurationException {
		if(endpoint ==null)
			throw new SparqlConfigurationException("Provide a valid SparqlEndpointConfiguration for Helio");
		if(configuration==null)
			throw new ConfigurationException("Provide a valid Configuration for Helio");
	}
	
	
	public ByteArrayOutputStream query(String query, ResultsFormat format) throws SparqlConfigurationException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		if(endpoint ==null)
			throw new SparqlConfigurationException("Provide a valid SparqlEndpointConfiguration for Helio");
		return endpoint.query(query, format);
	}

	private static final String BULK_QUERY = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
	public ByteArrayOutputStream getRDF(ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		if(endpoint ==null)
			throw new SparqlConfigurationException("Provide a valid SparqlEndpointConfiguration for Helio");
		return endpoint.query(BULK_QUERY, format);
	}

	// Advanced methods:
	private static final String BULK_QUERY_2_1 = "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o . } filter(";
	private static final String BULK_QUERY_2_2 = ") }";
	private static final String BULK_QUERY_2_FILTER_1 = "|| STRENDS( str(?g), \"?rule=";
	private static final String BULK_QUERY_2_FILTER_2 ="\")";
	/**
	 * This method retrieves only RDF that is generated as result of a {@link Mapping}
	 * @param mapping a mapping
	 * @param format the format of the output
	 * @return the RDF associated to the mapping
	 * @throws SparqlQuerySyntaxException if the query has errors
	 * @throws SparqlRemoteEndpointException if there is a problem with the remote endpoint
	 * @throws SparqlConfigurationException 
	 */
	public ByteArrayOutputStream getRDF(Mapping mapping, ResultsFormat format) throws SparqlQuerySyntaxException, SparqlRemoteEndpointException, SparqlConfigurationException {
		if(endpoint ==null)
			throw new SparqlConfigurationException("Provide a valid SparqlEndpointConfiguration for Helio");
		
		StringBuilder builder = new StringBuilder(BULK_QUERY_2_1);
		mapping.getTranslationRules()
			.parallelStream().map(tr -> Utils.mapTranslationRulesId(tr.getId()))
			.map(id -> Utils.concatenate(BULK_QUERY_2_FILTER_1,id,BULK_QUERY_2_FILTER_2))
			.forEach(elem -> builder.append(elem));
		String query = builder.append(BULK_QUERY_2_2).toString();
		query = query.replaceAll("\\(\\|\\|", "(");
		return endpoint.query(query, format);
	}
}
