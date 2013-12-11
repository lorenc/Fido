/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright 2013 Falco Labs LLC
 *
 */

package com.FalcoLabs.Fido.api.datastore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

// Represents an item from the datastore.  Entities contain a Key that describes the location of the item and a list of properties that contain the data for the item.
public class Entity implements Serializable {
	private static final long serialVersionUID = 8746836633059670261L;
	public static final String KEY_RESERVED_PROPERTY = "key";
	private Key key;
	private Map<String, Object> properties;
	
	/**
	 * Instantiates a new entity.
	 */
	protected Entity() {		
	}
	
	/**
	 * Instantiates a new entity with the specified key.
	 *
	 * @param k the k
	 */
	public Entity(Key k) {
		this.key = k;
	}

	/**
	 * Instantiates a new entity with the specified key and parent.
	 *
	 * @param parent the parent
	 * @param k the k
	 */
	public Entity(Key parent, Key k) {
		this(k);
		this.key.setParent(parent);
	}
	
	/**
	 * Instantiates a new entity with the specified kind, name and parent.
	 *
	 * @param kind the kind
	 * @param name the name
	 * @param parent the parent
	 */
	public Entity(String kind, String name, Key parent) {
		this.key = new Key(kind, name);
		this.key.setParent(parent);
	}

	/**
	 * Instantiates a new entity with the specified kind and name.
	 *
	 * @param kind the kind
	 * @param name the name
	 */
	public Entity(String kind, String name) {
		this.key = new Key(kind, name);
	}

	/**
	 * Sets the key.
	 *
	 * @param value the new key
	 */
	public void setKey(Key value) {
		this.key = value;
	}
	
	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public Key getKey() {
		return this.key;
	}

	/**
	 * Copy the properties from another entity to this instance.
	 *
	 * @param source the new properties from
	 */
	public void setPropertiesFrom(Entity source) {
		for (String key : source.getProperties().keySet()) {
			this.setProperty(key, source.getProperty(key));
		}
	}

	/**
	 * Sets the properties from an embedded entity onto this instance.
	 *
	 * @param embedded the new properties from
	 */
	public void setPropertiesFrom(EmbeddedEntity embedded) {
		this.setPropertiesFrom((Entity)embedded);
	}

	/**
	 * Gets the Key for the parent if there is one.
	 *
	 * @return the parent
	 */
	public Key getParent() {
		return (null != this.key && null != this.key.getParent()) ? this.key.getParent() : null;
	}

	/**
	 * Gets the value of a property on this instance.
	 *
	 * @param string the string
	 * @return the property
	 */
	public Object getProperty(String string) {
		return this.getProperties().get(string);
	}

	/**
	 * Sets a property onto this instance.  The property won't be persisted until the instance is saved via a call to DataStoreService.put.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public void setProperty(String name, Object value) {
		this.getProperties().put(name, value);
	}
	
	/**
	 * Gets the properties.
	 *
	 * @return the properties
	 */
	public synchronized Map<String, Object> getProperties() {
		if (null == this.properties) {
			this.properties = new HashMap<String, Object>();
		}
		return this.properties;
	}

	/**
	 * Sets a property on the Entity that won't be indexed.
	 *
	 * @param s the s
	 * @param value the value
	 */
	public void setUnindexedProperty(String s, Object value) {
		this.setProperty(s, value);
	}	
}
