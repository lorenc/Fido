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
import java.util.List;
import java.util.Map;

// Represents a column on an item in the datastore
public abstract class DataStoreColumn {	
	private static String ComplexColumnPrefix = "[]";
	protected static Map<String, Class<?>> classStringMap = new HashMap<String, Class<?>>();
	protected String name;
	protected Class<?> type = null;
	protected Object value;
	
	/**
	 * Creates a new column with the specified name
	 *
	 * @param name the name
	 * @return the data store column
	 */
	public static DataStoreColumn create(String name) {
		if (DataStoreColumn.getIsComplexName(name)) {
			return new ComplexSelectColumn(name);
		} else {
			return new SimpleColumn(name);
		}	
	}
	
	/**
	 * Creates a new column with the specified name and type
	 *
	 * @param name the name
	 * @param type the type
	 * @return the data store column
	 */
	public static DataStoreColumn create(String name, Class<?> type) {
		return new SimpleColumn(name, type);
	}
	
	/**
	 * Creates a new column with the specified name and value
	 *
	 * @param name the name
	 * @param value the value
	 * @return the data store column
	 */
	public static DataStoreColumn create(String name, Object value) {
		return DataStoreColumn.create(null,  name, value);
	}
	
	/**
	 * Creates a new column using the data from the row
	 *
	 * @param row the row
	 * @param name the name
	 * @param value the value
	 * @return the data store column
	 */
	public static DataStoreColumn create(DataStoreRow row, String name, Object value) {
		if (null != row && DataStoreColumn.getIsComplexType(value) && !DataStoreColumn.getIsComplexName(name)) {
			return new ComplexInsertColumn(row, name, (List<?>)value);
		} else if (!DataStoreColumn.getIsComplexType(value) && DataStoreColumn.getIsComplexName(name) && value != null && value instanceof String) {
			return new ComplexSelectColumn(name, (String)value);
		} else {
			return new SimpleColumn(name, value);
		}
	}
		
	/**
	 * Gets the checks if is complex name.  A complex name means that the column contains multivalued data
	 *
	 * @param name the name
	 * @return the checks if is complex name
	 */
	protected static boolean getIsComplexName(String name) {
		return name.startsWith(DataStoreColumn.ComplexColumnPrefix);
	}
	
	/**
	 * Gets the checks if is complex type.  A complex type means that the column contains multivalued data
	 *
	 * @param value the value
	 * @return the checks if is complex type
	 */
	protected static boolean getIsComplexType(Object value) {
		if (null == value) {
			return false;
		}
        if (value instanceof List) {
                return true;
        } else {
                return false;
        }
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return this.value;
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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Class<?> getType() {
		return this.type;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the new value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * Gets the encoded name.  The encoded name is the same as the column name for normal properties.  For multivalued properties the encoded name contains the property name and the sub table that holds the multiple values.
	 *
	 * @return the encoded name
	 */
	public String getEncodedName() {
		return DataStoreColumn.getEncodedName(this.getName(), this.getType());
	}	
	
	/**
	 * Gets the encoded name.
	 *
	 * @param name the name
	 * @param type the type
	 * @return the encoded name
	 */
	protected static String getEncodedName(String name, Class<?> type) {
		switch (name) {
		case DataStore.ENTITY_PROPERTY_KEY:
		case DataStore.ENTITY_PROPERTY_PARENT:
			return name;
		default:
			if (DataStoreColumn.getIsStringSerializableType(type)) {
				StringBuilder b = new StringBuilder();
				b.append(type.getName().replaceAll("\\.", "_"));
				b.append("__");
				b.append(name);
				return b.toString();
			} else {
				return name;
			}
		}		
	}
	
	/**
	 * Decoded name.
	 *
	 * @param name the name
	 * @return the string
	 */
	protected static String decodedName(String name) {
		int i = name.indexOf("__");
		if (i > 0) {
			return name.substring(i + 2);
		}
		return name;		
	}
	
	/**
	 * Gets the simple name from complex name.
	 *
	 * @param name the name
	 * @return the simple name from complex name
	 */
	protected static String getSimpleNameFromComplexName(String name) {
		return name.substring(DataStoreColumn.ComplexColumnPrefix.length());
	}

	/**
	 * Gets the complex name from simple name.
	 *
	 * @param name the name
	 * @return the complex name from simple name
	 */
	protected static String getComplexNameFromSimpleName(String name) {
		return (DataStoreColumn.ComplexColumnPrefix + name);
	}
	
	/**
	 * Gets the checks if is string serializable type.
	 *
	 * @param type the type
	 * @return the checks if is string serializable type
	 */
	protected static boolean getIsStringSerializableType(Class<?> type) {
		if (type == Key.class) {
			return true;
		}
		return false;
	}
}
