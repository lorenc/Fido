package com.FalcoLabs.Fido.api.datastore.exceptions;

import com.FalcoLabs.Fido.api.datastore.Key;
import com.FalcoLabs.Fido.api.localization.messages;

public class EntityNotFoundException extends RuntimeException {

	public EntityNotFoundException() {
	}
	
	public EntityNotFoundException(String s) {
		super(messages.get(s));
	}
	
	public EntityNotFoundException(Key key) {
		super(key.toString());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
