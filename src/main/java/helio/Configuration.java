package helio;

import org.apache.jena.shared.impl.JenaParameters;

import helio.sparql.SparqlConfiguration;

/**
 * This class contains all the configurable elements of the Helio materialiser software
 * @author Andrea Cimmino
 *
 */
public class Configuration {

	private SparqlConfiguration sparql;
	private String namespace;
	private int threads;
	private boolean silentAcceptanceOfUnknownDatatypes;
	private boolean eagerLiteralValidation;
	private boolean bNodeUIDGeneration;
	private boolean owlRuleOverOWLRuleWarnings;

	public Configuration() {
		super();
	}

	public SparqlConfiguration getSparql() {
		return sparql;
	}

	public void setSparql(SparqlConfiguration sparql) {
		this.sparql = sparql;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}



	public boolean isSilentAcceptanceOfUnknownDatatypes() {
		return silentAcceptanceOfUnknownDatatypes;
	}

	public void setSilentAcceptanceOfUnknownDatatypes(boolean silentAcceptanceOfUnknownDatatypes) {
		this.silentAcceptanceOfUnknownDatatypes = silentAcceptanceOfUnknownDatatypes;
		JenaParameters.enableSilentAcceptanceOfUnknownDatatypes=this.silentAcceptanceOfUnknownDatatypes;
	}

	public boolean isEagerLiteralValidation() {
		return eagerLiteralValidation;
	}

	public void setEagerLiteralValidation(boolean eagerLiteralValidation) {
		this.eagerLiteralValidation = eagerLiteralValidation;
		JenaParameters.enableEagerLiteralValidation = this.eagerLiteralValidation;
	}

	public boolean isbNodeUIDGeneration() {
		return bNodeUIDGeneration;
	}

	public void setbNodeUIDGeneration(boolean bNodeUIDGeneration) {
		this.bNodeUIDGeneration = bNodeUIDGeneration;
		JenaParameters.disableBNodeUIDGeneration=this.bNodeUIDGeneration;
	}

	public boolean isOwlRuleOverOWLRuleWarnings() {
		return owlRuleOverOWLRuleWarnings;
	}

	public void setOwlRuleOverOWLRuleWarnings(boolean owlRuleOverOWLRuleWarnings) {
		this.owlRuleOverOWLRuleWarnings = owlRuleOverOWLRuleWarnings;
		JenaParameters.enableOWLRuleOverOWLRuleWarnings=this.owlRuleOverOWLRuleWarnings;
	}

	public static Configuration createDefault() {
		Configuration configuration = new Configuration();
		configuration.setNamespace("http://helio.linkeddata.es/resource/");
		configuration.setThreads(30);

		SparqlConfiguration sparql = new SparqlConfiguration();
		sparql.setSparqlQuery("http://localhost:7200/repositories/app");
		sparql.setSparqlUpdate("http://localhost:7200/repositories/app/statements");
		configuration.setSparql(sparql);

		configuration.setSilentAcceptanceOfUnknownDatatypes(true);
		configuration.setEagerLiteralValidation(false);
		configuration.setbNodeUIDGeneration(true);
		configuration.setOwlRuleOverOWLRuleWarnings(true);

		 return configuration;
	}



	public static Configuration fromJson(String json) {
		return Utils.GSON.fromJson(json, Configuration.class);
	}

	public String toJson() {
		return Utils.GSON.toJson(this);
	}

	@Override
	public String toString() {
		return toJson();
	}





}
