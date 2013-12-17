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

// Provides access to properties on the Cassandra datastore
public abstract class DataStore {
	protected static final String ENTITY_COMPLEX_PROPERTY_PREFIX = "6a7d235e20e111e385edf23c91aec05e";
	protected static final String ENTITY_PROPERTY_KEY = "key";
	protected static final String ENTITY_PROPERTY_PARENT = "parent";
	protected static String CONNECTION_POOL_NAME;
	protected static String CONNECTION_STRING;
	protected static int CONNECTION_PORT = -1;
	protected static int REPLICATION_FACTOR = 1;
	protected static String KEYSPACE_NAME;
	private static final int MAX_KEYSPACE_LENGTH = 30;	
	
	/**
	 * Gets the keyspace.
	 *
	 * @return the current active keyspace
	 */
	public static String getKeyspace() {
		return DataStore.KEYSPACE_NAME;
	}
	
	/**
	 * Sets the contact point.
	 *
	 * @param value the new contact point.  This should be the network address of the Cassandra server
	 */
	public static void setContactPoint(String value) {
		DataStore.CONNECTION_STRING = value;
	}
	
	/**
	 * Sets the port used to connect
	 *
	 * @param value the new port.  This should be the port that Cassandra is listening on
	 */	
	public static void setPort(int value) {
		DataStore.CONNECTION_PORT = value;
	}
	
	/**
	 * Sets the keyspace.
	 * @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_using/create_keyspace_c.html">Cassandra Keyspace</a>
	 * @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_reference/create_keyspace_r.html">Create Keyspace</a>
	 * @param keyspace the new keyspace
	 */
	public static void setKeyspace(String keyspace) {
		if (keyspace.length() > DataStore.MAX_KEYSPACE_LENGTH) {
			throw new DatastoreServiceException(messages.KEYSPACE_LENGTH_ERROR);
		}
		DataStore.KEYSPACE_NAME = keyspace;
		Schema.ensureKeyspace(keyspace);		
	}
	
	/**
	 * Drop keyspace.  After dropping a keyspace all data that was stored in that keyspace will no longer be accessible
	 * @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_reference/drop_keyspace_r.html">Cassandra Keyspace</a>
	 */
	public static void dropKeyspace() {
		if (null == DataStore.KEYSPACE_NAME) {
			throw new DatastoreServiceException(messages.MUST_SET_KEYSPACE_ERROR);
		}
		Schema.dropKeyspace(DataStore.KEYSPACE_NAME);
		SchemaMapper.reset();
		Schema.reset();		
		DataStore.KEYSPACE_NAME = null;
	}

	/**
	 * Sets the replication factor.
	 * @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_reference/cql_storage_options_c.html">Cassandra Options</a>
	 * @param value the new replication factor
	 */
	public static void setReplicationFactor(int value) {
		DataStore.REPLICATION_FACTOR = value;
	}
}
