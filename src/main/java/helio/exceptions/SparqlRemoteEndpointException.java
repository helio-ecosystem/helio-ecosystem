package helio.exceptions;

import helio.Utils;

public class SparqlRemoteEndpointException extends Exception{

	private static final long serialVersionUID = 4962317086573414887L;

	public SparqlRemoteEndpointException() {
		super();
	}

	public SparqlRemoteEndpointException(String msg) {
		super(msg);
	}

	public SparqlRemoteEndpointException(String ... msg) {
		super(Utils.concatenate(msg));
	}
}
