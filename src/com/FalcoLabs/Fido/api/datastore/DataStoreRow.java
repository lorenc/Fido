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
import java.util.HashMap;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.serializers.BinarySerializer;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;

// Represents an object in the Cassandra store
public class DataStoreRow {
	private static String LOG_TAG = DataStoreRow.class.getName();
	private String kind;
	private Map<String, DataStoreColumn> columns = new HashMap<String, DataStoreColumn>();
	
	/**
	 * Instantiates a new data store row of the specified kind.
	 *
	 * @param kind the kind
	 */
	public DataStoreRow(String kind) {
		this.kind = kind;
	}
	
	/**
	 * Instantiates a new data store row from an existing entity.
	 *
	 * @param e the e
	 */
	public DataStoreRow(Entity e) {
		this.kind = e.getKey().getKind();
		Key k = e.getKey();		
		this.addColumn(DataStoreColumn.create(DataStore.ENTITY_PROPERTY_KEY, k));
		this.addColumn(DataStoreColumn.create(DataStore.ENTITY_PROPERTY_PARENT, null == k.getParent() ? Key.EmptyValue() : k.getParent()));
		for (String propertyName : e.getProperties().keySet()) {
			Object propertyValue = e.getProperties().get(propertyName);
			if (null != propertyValue) {
				DataStoreColumn c = DataStoreColumn.create(this, propertyName, propertyValue);
				if (null != c.getValue()) {
					this.columns.put(c.getName(), c);
				}
			}
		}
	}
	
	/**
	 * Instantiates a new data store row from a row @see <a href="http://www.datastax.com/doc-source/developer/java-apidocs/com/datastax/driver/core/class-use/Row.html">Row</a>
	 *
	 * @param row the row
	 */
	public DataStoreRow(Row row) {
		for (Definition column : row.getColumnDefinitions()) {
			DataStoreColumn c = DataStoreColumn.create(column.getName());
			c.setValue(DataStoreRow.getValueFromType(column.getType(), row, column, c.getType()));
			if (null != c.getValue()) {
				this.addColumn(c);
			}
		}
	}
	
	/**
	 * Gets the entity.  Returns an entity created from the row
	 *
	 * @return the entity
	 */
	public Entity getEntity() {
		Entity e = new Entity(this.getKey());
		for (Map.Entry<String, DataStoreColumn> entry : this.getColumns().entrySet()) {
			switch (entry.getKey()) {
			case DataStore.ENTITY_PROPERTY_KEY:
			case DataStore.ENTITY_PROPERTY_PARENT:
				break;
			default:
				e.setProperty(entry.getValue().getName(), entry.getValue().getValue());
				break;
			}			
		}
		return e;
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
	 * Gets the column family.
	 *
	 * @return the column family
	 */
	public String getColumnFamily() {
		return SchemaMapper.kindToColumnFamily(this.getKind());
	}
	
	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public Key getKey() {
		return (Key)(null != this.getColumn("key") ? this.getColumn("key").getValue() : null);
	}
	
	/**
	 * Gets the columns.
	 *
	 * @return the columns
	 */
	public Map<String, DataStoreColumn> getColumns() {
		return this.columns;
	}
	
	/**
	 * Gets the column.
	 *
	 * @param name the name
	 * @return the column
	 */
	public DataStoreColumn getColumn(String name) {
		return this.columns.get(name);
	}
	
	/**
	 * Adds the column.
	 *
	 * @param value the value
	 */
	public void addColumn(DataStoreColumn value) {
		this.columns.put(value.getName(), value);
	}
	
	private static Object getValueFromType(DataType type, Row row, Definition column, Class<?> columnType) {
		ByteBuffer rawValue = row.getBytesUnsafe(column.getName());
		if (null == rawValue) {
			return null;
		}			
		Object o = type.deserialize(rawValue);		
		
		if (Integer.class == o.getClass()) {
			return Long.valueOf((long)(Integer)o); // appengine smacks all Integers to Longs so do the same to remain compatibility
		} else if (String.class == o.getClass() && Key.class == columnType) {
			return Key.parse((String)o);
		} else if (o instanceof ByteBuffer) {
			try {
				BinarySerializer<Object> b = new BinarySerializer<Object>();
				return b.fromByteBuffer(rawValue);			
			} catch(Exception e) {	
				Log.e(LOG_TAG, e);
			}			
		}
		return o;
	}	
}
