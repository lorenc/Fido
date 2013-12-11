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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.datastore.serializers.BinarySerializer;
import com.datastax.driver.core.*;

// Methods to get and set data from the Cassandra store
public class DatastoreClient {
	private static String LOG_TAG = DatastoreClient.class.getName();
	private Connection connection;
	
	private Connection getConnection() {
		if (null == this.connection) {
			this.connection = DatastoreServiceFactory.getConnection();
		}
		return this.connection;
	}
	
	private Session getSession() {
		return this.getConnection().getSession();
	}	
	
	/**
	 * Query the store for matching data
	 *
	 * @param cql the cql @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_using/about_cql_c.html">CQL</a>
	 * @param values the values
	 * @return the result set containing the results of the query
	 */
	public ResultSet query(String cql, Object ... values) {
		Log.v(LOG_TAG, " Query: " + cql);
		List<Object> cqlSafeValues = this.getCqlSafeValues(values);
		try {			
			PreparedStatement prepared = this.getSession().prepare(cql);
			BoundStatement boundStatement = new BoundStatement(prepared);
			ResultSet results;
			if (cqlSafeValues.size() > 0) {
				results = this.getSession().execute(boundStatement.bind(cqlSafeValues.toArray()));
			} else {
				results = this.getSession().execute(boundStatement);
			}
			return results;
		} catch (Exception e) {
			Log.e(LOG_TAG, e);
			throw new DatastoreServiceException(e);			
		}		
	}

	/**
	 * Delete.  Delete any items that match the query
	 *
	 * @param query the query @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_using/about_cql_c.html">CQL</a>
	 */
	public void delete(Query query) {
		query.prepare();
		String selectTarget = SchemaMapper.kindToColumnFamily(query.getKind());
		StringBuilder b = new StringBuilder();
		b.append("DELETE from \"");
		b.append(DataStore.KEYSPACE_NAME);
		b.append("\".\"");
		b.append(selectTarget);
		b.append("\"");
		b.append(query.getQuery());		
		this.query(b.toString(), query.getValues().toArray());		
	}
	
	/**
	 * Select.  Return any items that match the query
	 *
	 * @param query the query @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_using/about_cql_c.html">CQL</a>
	 * @return the list
	 */
	public List<DataStoreRow> select(Query query) {
		query.prepare();
		Schema.ensure(query);		
		String selectTarget = SchemaMapper.kindToColumnFamily(query.getKind());
		StringBuilder b = new StringBuilder();
		b.append("Select * from \"");
		b.append(DataStore.KEYSPACE_NAME);
		b.append("\".\"");
		b.append(selectTarget);
		b.append("\"");		
		b.append(query.getQuery());
		b.append(" ALLOW FILTERING;");
		ResultSet results = this.query(b.toString(), query.getValues().toArray());
		List<DataStoreRow> rows = new ArrayList<DataStoreRow>();
		for (Row row : results.all()) {
			DataStoreRow r = new DataStoreRow(row);
			rows.add(r);
		}
		rows = this.sort(query, rows);
		return rows;
	}	

	/**
	 * Select.
	 *
	 * @param columns the columns to return
	 * @param query the query @see <a href="http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_using/about_cql_c.html">CQL</a>
	 * @return the list
	 */
	public List<DataStoreRow> select(List<DataStoreColumn> columns, Query query) {
		query.prepare();
		Schema.ensure(query);		
		String selectTarget = SchemaMapper.kindToColumnFamily(query.getKind());
		StringBuilder b = new StringBuilder();
		b.append("Select ");
		int count = 0;
		for (DataStoreColumn column : columns) {
			if (count++ > 0) {
				b.append(",");
			}
			b.append("\"");
			b.append(column.getEncodedName());
			b.append("\"");
		}
		b.append(" from \"");
		b.append(DataStore.KEYSPACE_NAME);
		b.append("\".\"");
		b.append(selectTarget);
		b.append("\"");		
		b.append(query.getQuery());
		b.append(" ALLOW FILTERING;");
		ResultSet results = this.query(b.toString(), query.getValues().toArray());
		List<DataStoreRow> rows = new ArrayList<DataStoreRow>();
		for (Row row : results.all()) {
			DataStoreRow r = new DataStoreRow(row);
			rows.add(r);
		}
		rows = this.sort(query, rows);
		return rows;
	}	
	
	/**
	 * Insert.  Add a new row to the datastore
	 *
	 * @param row the row
	 */
	public void insert(DataStoreRow row) {
		Schema.ensure(row);
		String insertTarget = SchemaMapper.kindToColumnFamily(row.getKind());
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO \"");
		query.append(DataStore.KEYSPACE_NAME);
		query.append("\".\"");
		query.append(insertTarget);
		query.append("\"(");
		
		int count = 0;
		for (Map.Entry<String, DataStoreColumn> entry : row.getColumns().entrySet()) {
			if (count++ > 0) {
				query.append(",");
			}
			query.append('"');
			query.append(entry.getValue().getEncodedName());
			query.append('"');			
		}
		
		query.append(") VALUES(");
		List<Object> values = new ArrayList<Object>();		
		count = 0;
		for (Map.Entry<String, DataStoreColumn> entry : row.getColumns().entrySet()) {
			values.add(entry.getValue().getValue());
			if (count++ > 0) {
				query.append(",");
			}
			query.append("?");
		}
		query.append(");");
		this.query(query.toString(), values.toArray());			
	}

	/**
	 * Delete.  Delete the item with the matching key.
	 *
	 * @param k the key to delete
	 */
	public void delete(Key k) {
		String target = SchemaMapper.kindToColumnFamily(k.getKind());
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM \"");
		query.append(DataStore.KEYSPACE_NAME);
		query.append("\".\"");
		query.append(target);
		query.append("\" WHERE key = ?");
		this.query(query.toString(), k.toString());
	}

	/**
	 * Delete.  Delete the items with the matching keys.
	 *
	 * @param keys the keys
	 */
	public void delete(List<Key> keys) {
		for (Key k : keys) {
			this.delete(k);
		}
	}	
	
	private List<Object> getCqlSafeValues(Object[] values) {
		List<Object> safeValues = new ArrayList<Object>();
		for (Object o : values) {
			if (o instanceof Key) {
				safeValues.add(((Key)o).toString());
			} else if (o instanceof EmbeddedEntity) {
				BinarySerializer<EmbeddedEntity> b = new BinarySerializer<EmbeddedEntity>();
				safeValues.add(b.toByteBuffer((EmbeddedEntity)o));
			} else if (o instanceof Entity) {
				BinarySerializer<Entity> b = new BinarySerializer<Entity>();
				safeValues.add(b.toByteBuffer((Entity)o));
			} else if (o instanceof ArrayList) {
				BinarySerializer<ArrayList<?>> b = new BinarySerializer<ArrayList<?>>();
				safeValues.add(b.toByteBuffer((ArrayList<?>)o));				
			} else {
				safeValues.add(o);
			}
		}
		return safeValues;
	}	
	
	private List<DataStoreRow> sort(final Query query, List<DataStoreRow> rows) {
		if (null != query.getSortColumn()) {
			// sorting in cassandra is limited - being able to sort a column family depends on the relation of the sorty property to the primary key.  For now
			// we are stuck doing our own sorting in order to provide generic sort access across all properties			
			Collections.sort(rows, new Comparator<DataStoreRow>() {

				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				public int compare(DataStoreRow left, DataStoreRow right) {
					DataStoreColumn leftColumn = left.getColumn(query.getSortColumn());
					DataStoreColumn rightColumn = right.getColumn(query.getSortColumn());
					Object leftValue = null != leftColumn ? leftColumn.getValue() : null;
					Object rightValue = null != rightColumn ? rightColumn.getValue() : null;
					if (leftValue == null && rightValue == null) {
						return 0;
					} else if (leftValue == null) {
						return 1;
					} else if (rightValue == null) {
						return -1;
					} else {
						if (query.getSortDirection() == Query.SortDirection.ASCENDING) {
							return ((Comparable)leftValue).compareTo(((Comparable)rightValue));
						} else {
							return ((Comparable)rightValue).compareTo(((Comparable)leftValue));
						}
					}
				}
			
			});
		}
		return rows;
	}	
}
