package helio.exceptions;

import helio.Utils;

public class SparqlQuerySyntaxException extends Exception{

	private static final long serialVersionUID = 3539284096573634755L;

	public SparqlQuerySyntaxException() {
		super();
	}

	public SparqlQuerySyntaxException(String msg) {
		super(msg);
	}

	public SparqlQuerySyntaxException(String ... msg) {
		super(Utils.concatenate(msg));
	}
}
