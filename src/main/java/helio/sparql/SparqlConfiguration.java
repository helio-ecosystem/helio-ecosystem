package helio.sparql;

import java.util.Objects;

import helio.Utils;

public class SparqlConfiguration {


	private String sparqlQuery;

	private String sparqlUpdate;

	private String username;

	private String password;

	public SparqlConfiguration() {
		super();
	}

	public String getSparqlQuery() {
		return sparqlQuery;
	}


	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}


	public String getSparqlUpdate() {
		return sparqlUpdate;
	}


	public void setSparqlUpdate(String sparqlUpdate) {
		this.sparqlUpdate = sparqlUpdate;
	}


	public String getUsername() {
		return username;
	}


	public void setUsername(String username) {
		this.username = username;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	public static SparqlConfiguration fromJson(String json) {
		return Utils.GSON.fromJson(json, SparqlConfiguration.class);
	}

	public String toJson() {
		return Utils.GSON.toJson(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(password, sparqlQuery, sparqlUpdate, username);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if ((obj == null) || (getClass() != obj.getClass()))
			return false;
		SparqlConfiguration other = (SparqlConfiguration) obj;
		return Objects.equals(password, other.password) && Objects.equals(sparqlQuery, other.sparqlQuery)
				&& Objects.equals(sparqlUpdate, other.sparqlUpdate) && Objects.equals(username, other.username);
	}




}
