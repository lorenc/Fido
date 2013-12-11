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

import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.localization.messages;

public abstract class DataStore {
	protected static final String ENTITY_COMPLEX_PROPERTY_PREFIX = "6a7d235e20e111e385edf23c91aec05e";
	protected static final String ENTITY_PROPERTY_KEY = "key";
	protected static final String ENTITY_PROPERTY_PARENT = "parent";
	protected static String CONNECTION_POOL_NAME;
	protected static String CONNECTION_STRING;
	protected static int REPLICATION_FACTOR = 1;
	protected static String KEYSPACE_NAME;
	private static final int MAX_KEYSPACE_LENGTH = 30;	
	
	public static String getKeyspace() {
		return DataStore.KEYSPACE_NAME;
	}
	
	public static void setContactPoint(String value) {
		DataStore.CONNECTION_STRING = value;
	}
	
	public static void setKeyspace(String keyspace) {
		if (keyspace.length() > DataStore.MAX_KEYSPACE_LENGTH) {
			throw new DatastoreServiceException(messages.KEYSPACE_LENGTH_ERROR);
		}
		DataStore.KEYSPACE_NAME = keyspace;
		Schema.ensureKeyspace(keyspace);		
	}
	
	public static void dropKeyspace() {
		if (null == DataStore.KEYSPACE_NAME) {
			throw new DatastoreServiceException(messages.MUST_SET_KEYSPACE_ERROR);
		}
		Schema.dropKeyspace(DataStore.KEYSPACE_NAME);
		SchemaMapper.reset();
		Schema.reset();		
		DataStore.KEYSPACE_NAME = null;
	}

	public static void setReplicationFactor(int value) {
		DataStore.REPLICATION_FACTOR = value;
	}
}
