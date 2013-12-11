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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.FalcoLabs.Fido.api.localization.messages;

// Represents an item in the datastore.  Each item in the datastore has a unique key.  Key's can optionally have parents.
public class Key implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2291221485223228079L;
	private Key parent;
	private String kind;
	private String name;
	
	/**
	 * Empty value.
	 *
	 * @return the key
	 */
	public static Key EmptyValue() {
		return new Key();
	}
	
	/**
	 * Instantiates a new key.
	 */
	protected Key() {
		this.parent = null;
		this.name = "";
		this.kind = "";
	}
	
	/**
	 * Parses a key and returns the parsed instance.
	 *
	 * @param s the s
	 * @return the key
	 */
	public static Key parse(String s) {
		if (null == s || 0 == s.length()) {
			return Key.EmptyValue();
		}
		Pattern pattern = Pattern.compile(".*?\\(\\\".*?\\\"\\)");
		Matcher matcher = pattern.matcher(s);
		if (matcher == null) {
			throw new UnsupportedOperationException(messages.get(messages.INVALID_KEY_NAME_ERROR));
		}		
		List<Key> keys = new ArrayList<Key>();
		while (matcher.find()) {
			keys.add(new Key(matcher.group()));
		}
		if (keys.size() == 0) {
			throw new UnsupportedOperationException(messages.get(messages.INVALID_KEY_NAME_ERROR));
		}
		// build the chain of keys
		Key lastKey = null;
		for (Key k : keys) {
			if (null != lastKey) {
				k.setParent(lastKey);				
			}
			lastKey = k;
		}
		return lastKey;
	}
	
	/**
	 * Instantiates a new key.
	 *
	 * @param s the s
	 */
	public Key(String s) {
		int beginIndex = 0;
		if (s.startsWith("/")) {
			beginIndex++;
		}
		int split = s.indexOf("(\"");
		if (-1 == split) {
			throw new UnsupportedOperationException(messages.get(messages.INVALID_KEY_NAME_ERROR));
		}
		this.kind = this.unescape(s.substring(beginIndex, split));
		split += 2;
		this.name = this.unescape(s.substring(split, s.length() - 2));
	}
	
	/**
	 * Instantiates a new key.
	 *
	 * @param parent the parent
	 * @param kind the kind
	 * @param name the name
	 */
	public Key(Key parent, String kind, String name) {
		this.parent = parent;
		this.kind = kind;
		this.name = name;
	}

	/**
	 * Instantiates a new key.
	 *
	 * @param kind the kind
	 * @param name the name
	 */
	public Key(String kind, String name) {
		this(null, kind, name);
	}

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public String getKind() {
		return this.kind;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	public Key getParent() {
		return this.parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param value the new parent
	 */
	public void setParent(Key value) {
		this.parent = value;
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId() {
		return this.toString().hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (this.name == null || this.name.length() == 0) {
			return "";
		}
		
		StringBuilder b = new StringBuilder();
		if (null != this.parent) {
			b.append(this.parent.toString());
			b.append('/');
		}
		b.append(this.kind);
		b.append("(\"");
		b.append(this.escape(this.name));
		b.append("\")");
		return b.toString();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Key)) {
			return false;
		}
		Key k = (Key)o;
		if (this.kind == null && k.getKind() != null) {
			return false;
		} else if (this.kind != null && k.getKind() == null) {
			return false;
		} else if (this.kind != null && k.getKind() != null && !this.kind.equals(k.getKind())) {
			return false;
		} else {
			return this.name.equals(k.getName());
		}		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	private String escape(String s) {
		s = s.replaceAll("\\\"", "%22");
		return s;
	}
	
	private String unescape(String s) {
		s = s.replaceAll("%22", "\"");
		return s;
	}	
}
