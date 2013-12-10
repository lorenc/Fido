package com.FalcoLabs.Fido.api.datastore;

import java.io.Serializable;

public class EmbeddedEntity extends Entity  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7181998054463811332L;

	public EmbeddedEntity(Key parent, Key k) {
		super(parent, k);
	}

	public EmbeddedEntity(Key k) {
		super(k);
	}
	
	public EmbeddedEntity() {
		super(null);
	}
}
