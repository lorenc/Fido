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
import java.util.List;
import java.util.Map;

import org.apache.cassandra.db.Column;
import org.apache.commons.lang3.StringUtils;

import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;
import com.FalcoLabs.Fido.api.datastore.Query.SortDirection;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class ComplexSelectColumn extends DataStoreColumn {

	public ComplexSelectColumn(String name) {
		this.name = DataStoreColumn.getSimpleNameFromComplexName(name); 
		this.type = String.class; // start out as string to read the pointer to the sub table
	}
	
	public ComplexSelectColumn(String name, String value) {
		this(name);
		this.setValue(value);
	}	
	
	@Override
	public void setValue(Object value) {
		if (value == null) {
			this.value = null;
			return;
		}
		String[] valueParts = ((String)value).split(",");
		Query query = new Query(valueParts[0]);
		query.addFilter("key", FilterOperator.EQUAL, StringUtils.join(valueParts, ',', 1, valueParts.length));
		query.addSort("order", SortDirection.ASCENDING);
		DatastoreClient client = new DatastoreClient();
		List<Object> values = new ArrayList<Object>();
		List<DataStoreRow> rows = client.select(query);
		for (DataStoreRow row : rows) {
			for (Map.Entry<String, DataStoreColumn> entry : row.getColumns().entrySet()) {
				switch (entry.getValue().getName()) {
				case DataStore.ENTITY_PROPERTY_KEY:
				case DataStore.ENTITY_PROPERTY_PARENT:
					break;
				case "order":
					break;
				default:
					values.add(entry.getValue().getValue());
				}
			}
		}
		if (values.size() > 0) {
			this.type = values.get(0).getClass();
		}
		this.value = values;	
		this.name = DataStoreColumn.decodedName(this.name);
	}
}
