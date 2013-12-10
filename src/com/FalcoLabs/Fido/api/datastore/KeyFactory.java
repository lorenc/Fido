package com.FalcoLabs.Fido.api.datastore;

public class KeyFactory {

	public static Key createKey(Key key, String kind, String name) {
		Key k = new Key(key, kind, name);
		return k;
	}

	public static Key createKey(String kind, String name) {
		return new Key(kind, name);
	}

	public static Key stringToKey(String keyString) {
		Key key = Key.parse(keyString);
		return key;
	}

	public static String keyToString(Key key) {
		return key.toString();
	}

}
