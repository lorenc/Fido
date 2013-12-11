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

import java.util.HashMap;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

// Maps between Fido objects and the Cassandra schema
abstract class SchemaMapper {
	private static String LOG_TAG = SchemaMapper.class.getName();
	private static Map<String, String> kindToColumnFamily = new HashMap<String, String>();
	private static Map<String, String> columnFamilyToKind = new HashMap<String, String>();
	protected static String SchemaColumnFamilySuffix = "_schema";

	/**
	 * Reset.
	 */
	public static void reset() {
		SchemaMapper.kindToColumnFamily = new HashMap<String, String>();
		SchemaMapper.columnFamilyToKind = new HashMap<String, String>();
}
	
	/**
	 * Column family to kind.
	 *
	 * @param columnFamily the column family
	 * @return the string
	 */
	public static String columnFamilyToKind(String columnFamily) {
		if (SchemaMapper.columnFamilyToKind.containsKey(columnFamily)) {
			return SchemaMapper.columnFamilyToKind.get(columnFamily);
		}

		try {
			DatastoreClient client = new DatastoreClient();			
			ResultSet result = client.query(
					String.format("select * from \"%s\".\"%s%s\" where column_family = '%s' ALLOW FILTERING", DataStore.KEYSPACE_NAME, DataStore.KEYSPACE_NAME, SchemaMapper.SchemaColumnFamilySuffix, columnFamily));
			
			for (Row row : result.all()) {
				String kind = row.getString("kind");
				synchronized (SchemaMapper.columnFamilyToKind) {
					if (!SchemaMapper.columnFamilyToKind.containsKey(columnFamily)) {
						SchemaMapper.columnFamilyToKind.put(columnFamily, kind);
					}
				}
				return kind;
			}
		} catch (Exception e) {
			Log.v(LOG_TAG, e.toString());
		}
		// hash is only one way so can't get the column family w/out knowing the type
		return null;
	}

	/**
	 * Kind to column family.
	 *
	 * @param kind the kind
	 * @return the string
	 */
	public static String kindToColumnFamily(String kind) {
		if (SchemaMapper.kindToColumnFamily.containsKey(kind)) {
			return SchemaMapper.kindToColumnFamily.get(kind);
		}

		SchemaMapper.ensureMappingTable();
		String columnFamily = SchemaMapper.getColumnFamilyFromKind(kind);
		if (null == columnFamily) {
			columnFamily = SchemaMapper.addKindToColumnFamilyMappingTable(kind);
		}
		return columnFamily;
	}

	/**
	 * Hash kind to column family.
	 *
	 * @param kind the kind
	 * @return the string
	 */
	protected static String hashKindToColumnFamily(String kind) {
		int hash = kind.hashCode();
		if (hash < 0) {
			return "_" + Integer.toString(Math.abs(hash));
		} else {
			return Integer.toString(hash);
		}
	}

	/**
	 * Hash the value and store it in the mapping table - we store instead of
	 * just returning the hash so that we can hash back to the kind from the
	 * column family
	 * 
	 * @param kind
	 * @return column family that maps to kind
	 */
	private static String addKindToColumnFamilyMappingTable(String kind) {
		DatastoreClient client = new DatastoreClient();
		String columnFamily = SchemaMapper.hashKindToColumnFamily(kind);
		
		client.query( 				
				String.format("INSERT INTO \"%s\".\"%s%s\"(kind, column_family) VALUES(?, ?)", DataStore.KEYSPACE_NAME, DataStore.KEYSPACE_NAME, SchemaMapper.SchemaColumnFamilySuffix), 
				kind, columnFamily);
		
		synchronized (SchemaMapper.kindToColumnFamily) {
			SchemaMapper.kindToColumnFamily.put(kind, columnFamily);
		}
		return columnFamily;
	}

	private static String getColumnFamilyFromKind(String kind) {
		DatastoreClient client = new DatastoreClient();
		ResultSet result = client.query(
				String.format("select * from \"%s\".\"%s%s\" where kind = '%s' ALLOW FILTERING", DataStore.KEYSPACE_NAME, DataStore.KEYSPACE_NAME, SchemaMapper.SchemaColumnFamilySuffix, kind));
		Row r = result.one();
		if (null != r) {
			String columnFamily = r.getString("column_family");
			synchronized (SchemaMapper.kindToColumnFamily) {
				SchemaMapper.kindToColumnFamily.put(kind, columnFamily);
			}
			return columnFamily;
		}
		return null;
	}

	private static synchronized void ensureMappingTable() {
		if (Schema.haveSchemaItem(DataStore.KEYSPACE_NAME + SchemaMapper.SchemaColumnFamilySuffix, null, null)) {
			return;
		}
		try {
			DatastoreClient client = new DatastoreClient();
			client.query("CREATE TABLE \"" + DataStore.KEYSPACE_NAME + "\".\"" + DataStore.KEYSPACE_NAME + SchemaMapper.SchemaColumnFamilySuffix
					+ "\"" + "(kind varchar, column_family varchar, PRIMARY KEY(kind, column_family))");
		} catch (Exception e) {
			Log.v(LOG_TAG, "Mapping table may already exist: " + e.toString());
		}
		Schema.addSchemaItem(DataStore.KEYSPACE_NAME + SchemaMapper.SchemaColumnFamilySuffix, "kind", String.class.getName());
		Schema.addSchemaItem(DataStore.KEYSPACE_NAME + SchemaMapper.SchemaColumnFamilySuffix, "column_family", String.class.getName());
	}
}
