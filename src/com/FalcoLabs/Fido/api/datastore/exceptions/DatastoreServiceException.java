package com.FalcoLabs.Fido.api.datastore.exceptions;

import com.FalcoLabs.Fido.api.localization.messages;

public class DatastoreServiceException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6436169057895006675L;
	private Exception innerException;
	
	public DatastoreServiceException(Exception e) {
		super(e);
		this.innerException = e;
	}

	public DatastoreServiceException(String s) {
		super(messages.get(s));
	}
	
	public Exception getInner() {
		return this.innerException;
	}
}
