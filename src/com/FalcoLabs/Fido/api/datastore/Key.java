package com.FalcoLabs.Fido.api.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.FalcoLabs.Fido.api.localization.messages;

public class Key implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2291221485223228079L;
	private Key parent;
	private String kind;
	private String name;
	
	public static Key EmptyValue() {
		return new Key();
	}
	
	protected Key() {
		this.parent = null;
		this.name = "";
		this.kind = "";
	}
	
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
	
	public Key(Key parent, String kind, String name) {
		this.parent = parent;
		this.kind = kind;
		this.name = name;
	}

	public Key(String kind, String name) {
		this(null, kind, name);
	}

	public String getKind() {
		return this.kind;
	}

	public String getName() {
		return this.name;
	}

	public Key getParent() {
		return this.parent;
	}

	public void setParent(Key value) {
		this.parent = value;
	}
	
	public long getId() {
		return this.toString().hashCode();
	}
	
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
