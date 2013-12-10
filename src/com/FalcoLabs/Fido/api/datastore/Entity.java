package com.FalcoLabs.Fido.api.datastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Entity implements Serializable {
	private static final long serialVersionUID = 8746836633059670261L;
	public static final String KEY_RESERVED_PROPERTY = "key";
	private Key key;
	private Map<String, Object> properties;
	
	protected Entity() {		
	}
	
	public Entity(Key k) {
		this.key = k;
	}

	public Entity(Key parent, Key k) {
		this(k);
		this.key.setParent(parent);
	}
	
	public Entity(String kind, String name, Key parent) {
		this.key = new Key(kind, name);
		this.key.setParent(parent);
	}

	public Entity(String kind, String name) {
		this.key = new Key(kind, name);
	}

	public void setKey(Key value) {
		this.key = value;
	}
	
	public Key getKey() {
		return this.key;
	}

	public void setPropertiesFrom(Entity source) {
		for (String key : source.getProperties().keySet()) {
			this.setProperty(key, source.getProperty(key));
		}
	}

	public void setPropertiesFrom(EmbeddedEntity embedded) {
		this.setPropertiesFrom((Entity)embedded);
	}

	public Key getParent() {
		return (null != this.key && null != this.key.getParent()) ? this.key.getParent() : null;
	}

	public Object getProperty(String string) {
		return this.getProperties().get(string);
	}

	public void setProperty(String name, Object value) {
		this.getProperties().put(name, value);
	}
	
	public synchronized Map<String, Object> getProperties() {
		if (null == this.properties) {
			this.properties = new HashMap<String, Object>();
		}
		return this.properties;
	}

	public void setUnindexedProperty(String s, Object value) {
		this.setProperty(s, value);
	}	
}
