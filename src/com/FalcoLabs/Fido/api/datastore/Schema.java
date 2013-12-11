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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.Query.FilterPredicate;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public abstract class Schema {	
	private static String LOG_TAG = Schema.class.getName();
	private static Map<String, Map<String, String>> schemaMap = new HashMap<String, Map<String, String>>();
	private static boolean schemaLoaded = false;
	
	public static void reset() {
		Schema.schemaLoaded = false;
		Schema.schemaMap = new HashMap<String, Map<String, String>>();
	}
	
	public static void reloadSchema() {
		Schema.schemaLoaded = false;
		Schema.getSchemaItems();
	}
	
	private synchronized static void getSchemaItems() {
		if (Schema.schemaLoaded) {
			return;
		}
		Schema.schemaLoaded = true;
		
		DatastoreClient client = new DatastoreClient();
		ResultSet results = client.query(
				"SELECT columnfamily_name, column_name, index_name from system.schema_columns where keyspace_name = '" + DataStore.KEYSPACE_NAME + "'");
		for (Row row : results.all()) {
			String columnFamily = row.getString("columnfamily_name");
			String column = row.getString("column_name");
			String index = row.getString("index_name");
			Schema.addSchemaItem(columnFamily, column, index);
		}
	}
	
	protected synchronized static void addSchemaItem(String table, String column, String index) {
		if (null != table) {
			if (!Schema.schemaMap.containsKey(table)) {
				Schema.schemaMap.put(table, new HashMap<String, String>());
			}
			if (null != column) {
				Schema.schemaMap.get(table).put(column, index);
			}
		}		
	}
	
	public static boolean haveSchemaItem(String table, String column, String index) {
		Schema.getSchemaItems();
		if (!Schema.schemaMap.containsKey(table)) {
			return false;
		}
		if (null == column) {
			return true;
		}		
		if (!Schema.schemaMap.get(table).containsKey(column)) {
			return false;
		}		
		if (null == index || column.equals("key")) {
			return true;
		}
		return Schema.schemaMap.get(table).get(column) != null;
	}
	
	public static void dropKeyspace(String keyspace) {
		try {
			DatastoreClient client = new DatastoreClient();
			client.query("DROP KEYSPACE \"" + keyspace + "\";");
		} catch(Exception e) {
			if (-1 == e.toString().indexOf("Cannot drop keyspace")) {
				Log.v(LOG_TAG, e.toString());
			}
		}
	}	
	
	public static void ensureKeyspace(String keyspace) {
		try {
			DatastoreClient client = new DatastoreClient();
			client.query("CREATE KEYSPACE IF NOT EXISTS \"" + keyspace + "\" WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor': " + Integer.toString(DataStore.REPLICATION_FACTOR) + "};");
		} catch(Exception e) {
			Log.v(LOG_TAG, e.toString());
		}
	}
	
	private static void ensureModelTable(String name) {
		Schema.ensureTable(name, "parent varchar, key varchar, PRIMARY KEY(key, parent)");
	}
		
	public static void ensureTable(String table, List<DataStoreColumn> columns) {
		if (Schema.haveSchemaItem(table, null, null)) {
			return;
		}		
		StringBuilder b = new StringBuilder();
		for (int i=0; i<columns.size(); i++) {
			DataStoreColumn c = columns.get(i);
			b.append("\"");
			b.append(c.getEncodedName());
			b.append("\"");
			b.append(' ');
			b.append(Schema.columnTypeFromClassType(c.getType()));
			b.append(", ");
		}
		b.append(" PRIMARY KEY(");
		for (int i=0; i<columns.size(); i++) {
			DataStoreColumn c = columns.get(i);
			if (i > 0) {
				b.append(", ");
			}
			b.append("\"");
			b.append(c.getEncodedName());
			b.append("\"");
		}
		b.append(")");
		Schema.ensureTable(table, b.toString());
		for (int i=0; i<columns.size(); i++) {
			DataStoreColumn c = columns.get(i);
			Schema.addSchemaItem(table, c.getEncodedName(), c.getType().getName());
		}
	}
	
	private static void ensureTable(String table, String columns) {
		if (Schema.haveSchemaItem(table, null, null)) {
			return;
		}
		DatastoreClient client = new DatastoreClient();
		try {
			client.query("CREATE TABLE \"" + DataStore.KEYSPACE_NAME + "\".\"" + table + "\" (" +
					columns +
					")");				
		} catch(Exception e) {
			if (-1 == e.toString().indexOf("Cannot add already existing column family")) {
				Log.v(LOG_TAG, e.toString());
			}
		}		
		Schema.addSchemaItem(table, null, null);
	}
	
	public static void ensureColumn(String table, DataStoreColumn column) {
		if (Schema.haveSchemaItem(table, column.getEncodedName(), null)) {
			return;
		}
		Schema.addColumn(table, column);
	}

	private static void addColumn(String table, DataStoreColumn column) {
		if (column.getName().equals(DataStore.ENTITY_PROPERTY_KEY) ||
				column.getName().equals(DataStore.ENTITY_PROPERTY_PARENT) ||
				column.getName().startsWith(DataStore.ENTITY_COMPLEX_PROPERTY_PREFIX)) {
			return;
		}
		if (Schema.haveSchemaItem(table, column.getEncodedName(), null)) {
			return;
		}
		
		DatastoreClient client = new DatastoreClient();
		try {
			client.query("ALTER TABLE \"" + DataStore.KEYSPACE_NAME + "\".\"" + table + "\" ADD \"" +
					column.getEncodedName() +
					"\" " +
					Schema.columnTypeFromClassType(column.getType())); 				
		} catch(Exception e) {
			Log.v(LOG_TAG, e.toString());
		}	
		Schema.addSchemaItem(table, column.getEncodedName(), null);
	}

	public static Object columnTypeFromClassType(Class<?> columnClass) {
		if (columnClass == Integer.class) {
			return "int";
		} else if (columnClass == Long.class) {
			return "bigint";
		} else if (columnClass == String.class) {
			return "varchar";
		} else if (columnClass == Entity.class) {
			return "blob";
		} else if (columnClass == EmbeddedEntity.class) {
			return "blob";	
		} else if (columnClass == Date.class) {
			return "timestamp";
		} else if (columnClass == Boolean.class){
			return "boolean";
		} else if (columnClass == List.class){
			return "blob";
		} else if (columnClass == ArrayList.class){
			return "blob";
		} else if (columnClass == Key.class) {
			return "varchar";
		} else {
			throw new IllegalArgumentException();
		}			
	}

	public static void ensureIndex(String table, DataStoreColumn column) {
		if (Schema.haveSchemaItem(table, column.getEncodedName(), column.getType().getName()) || column.getName().equals("key")) {
			return;
		}
				
		DatastoreClient client = new DatastoreClient();		
		try {
			//String indexName = "index_" + table + column.getEncodedName() + "_index";
			client.query("CREATE INDEX ON \"" + // IF NOT EXISTS
					DataStore.KEYSPACE_NAME + "\".\"" + table + "\"(\"" +
					column.getEncodedName() +
					"\")");			
			
			// if the index was really created pause for a few seconds to give the index time to populate before it is
			// used.  This is a huge ugly hack that exists only because querying an index right after creating it doesn't always
			// work against Cassandra despite what the documentation may lead you to believe...
			Thread.sleep(5000);
			
		} catch(Exception e) {
			Log.v(LOG_TAG, e.toString());
		}
		Schema.addSchemaItem(table, column.getEncodedName(), column.getType().getName());
	}	
	
	public static void ensureIndexedColumn(String kind, DataStoreColumn column) {
		String columnFamily = SchemaMapper.kindToColumnFamily(kind);
		Schema.ensureColumn(columnFamily, column);
		Schema.ensureIndex(columnFamily, column);		
	}

	public static void ensure(DataStoreRow row) {
		String columnFamily = SchemaMapper.kindToColumnFamily(row.getKind());
		Schema.ensureModelTable(columnFamily);
		for (Map.Entry<String, DataStoreColumn> entry : row.getColumns().entrySet()) {
			Schema.ensureColumn(columnFamily, entry.getValue());
		}
	}

	public static void ensure(Query query) {
		String columnFamily = SchemaMapper.kindToColumnFamily(query.getKind());
		Schema.ensureModelTable(columnFamily);
		if (null != query.getPredicates()) {
			for (FilterPredicate predicate : query.getPredicates()) {
				if (!predicate.isComplex()) {
					Schema.ensureColumn(columnFamily, predicate.getColumn());
					Schema.ensureIndex(columnFamily, predicate.getColumn());
				}
			}
		}
	}
}
