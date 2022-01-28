package helio;

import helio.sparql.SparqlConfiguration;

/**
 * This class contains all the configurable elements of the Helio materialiser software
 * @author Andrea Cimmino
 *
 */
public class Configuration extends AbstractIgnition{

	private SparqlConfiguration sparql;
	private String namespace;
	private int threads;

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

	public static Configuration createDefault() {
		Configuration configuration = new Configuration();
		configuration.setNamespace("http://helio.linkeddata.es/resource/");
		configuration.setThreads(30);

		SparqlConfiguration sparql = new SparqlConfiguration();
		sparql.setSparqlQuery("http://localhost:7200/repositories/app");
		sparql.setSparqlUpdate("http://localhost:7200/repositories/app/statements");
		configuration.setSparql(sparql);

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
