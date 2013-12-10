package com.FalcoLabs.Fido.api.datastore;

import java.io.Serializable;

public class Text implements Serializable {
	private static final long serialVersionUID = -2355969109775209171L;
	private String value;
	
	public Text(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
