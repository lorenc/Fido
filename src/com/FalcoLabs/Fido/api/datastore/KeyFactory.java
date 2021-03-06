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

// Factory to create Key instances.
public class KeyFactory {

	/**
	 * Creates a new Key object.
	 *
	 * @param key the key
	 * @param kind the kind
	 * @param name the name
	 * @return the key
	 */
	public static Key createKey(Key key, String kind, String name) {
		Key k = new Key(key, kind, name);
		return k;
	}

	/**
	 * Creates a new Key object.
	 *
	 * @param kind the kind
	 * @param name the name
	 * @return the key
	 */
	public static Key createKey(String kind, String name) {
		return new Key(kind, name);
	}

	/**
	 * String to key.
	 *
	 * @param keyString the key string
	 * @return the key
	 */
	public static Key stringToKey(String keyString) {
		Key key = Key.parse(keyString);
		return key;
	}

	/**
	 * Key to string.
	 *
	 * @param key the key
	 * @return the string
	 */
	public static String keyToString(Key key) {
		return key.toString();
	}

}
