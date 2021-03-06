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

import org.apache.commons.lang3.StringUtils;

// Represents a simple column with a name and a single value
public class SimpleColumn extends DataStoreColumn {
	public static String LOG_TAG = SimpleColumn.class.getName();

	/**
	 * Instantiates a new simple column.
	 *
	 * @param name the name
	 * @param type the type
	 * @param value the value
	 */
	public SimpleColumn(String name, Class<?> type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	/**
	 * Instantiates a new simple column.
	 *
	 * @param name the name
	 * @param value the value
	 */
	public SimpleColumn(String name, Object value) {
		this(name, value.getClass(), value);
	}
		
	/**
	 * Instantiates a new simple column.
	 *
	 * @param name the name
	 * @param type the type
	 */
	public SimpleColumn(String name, Class<?> type) {
		this(name, type, null);
	}
	
	/**
	 * Instantiates a new simple column.
	 *
	 * @param name the name
	 */
	public SimpleColumn(String name) {	
		switch (name) {
		case DataStore.ENTITY_PROPERTY_KEY:
		case DataStore.ENTITY_PROPERTY_PARENT:
			this.name = name;
			this.type = Key.class;
			return;
		default:
			if (!this.parseTypeFromName(name) || !DataStoreColumn.getIsStringSerializableType(this.type))
			{
				this.name = name;
				this.type = null;
			}
			break;
		}			
	}		
	
	private boolean parseTypeFromName(String name) {
		String[] parts = name.split("__");
		if (1 >= parts.length) {
			return false;
		}

		String typeName = parts[0].replaceAll("_", ".");

		try {
			synchronized(SimpleColumn.classStringMap) {
				if (SimpleColumn.classStringMap.containsKey(typeName)) {
					this.type = SimpleColumn.classStringMap.get(typeName);
				}
			}
			if (null == this.type) {
				this.type = Class.forName(typeName);
				synchronized(SimpleColumn.classStringMap) {
					if (!SimpleColumn.classStringMap.containsKey(typeName)) {
						SimpleColumn.classStringMap.put(typeName,  this.type);
					}
				}					
			}
		} catch(ClassNotFoundException cnfe) {
			return false;				
		}
		this.name = parts.length == 2 ? parts[1] : StringUtils.join(parts, ", ", 1, parts.length - 1);	
		return true;
	}
}
