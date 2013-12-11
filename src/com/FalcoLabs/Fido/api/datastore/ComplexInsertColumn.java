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

import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;
import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.localization.messages;

// Does an insert of complex (multivalued) column data.  For example a property that consists of an array of strings
class ComplexInsertColumn extends DataStoreColumn {

	/**
	 * Instantiates a new complex insert column.
	 *
	 * @param row the row containing the data to insert
	 * @param name the name of the property tha will contain the data
	 * @param values the values to associate with the name
	 */
	public ComplexInsertColumn(DataStoreRow row, String name, List<?> values) {
		this.name = name;
		String subTable = null;
		
		if (0 == values.size()) {
			this.value = null;
			return;
		}
		
		this.type = this.validateType(values);
		if (!this.isSearchableType(type)) {
			this.value = values;
			this.type = values.getClass();
			return;
		}
		
		DatastoreClient client = new DatastoreClient();
		long order = 0;
		for (Object o : values) {						
			if (null == this.type) {
				this.type = o.getClass();
			} else if (this.type != o.getClass()) {
				throw new DatastoreServiceException(messages.MULTIVALID_PROPS_SAME_TYPE_ERROR);				
			}
			if (null == subTable) {
				subTable = this.deleteExisting(row);
			}
			DataStoreRow subRow = new DataStoreRow(subTable);
			subRow.addColumn(DataStoreColumn.create(name, o));
			subRow.addColumn(DataStoreColumn.create("key", row.getKey()));
			subRow.addColumn(DataStoreColumn.create("order", order++));
			client.insert(subRow);
		}						
		this.value = String.format("%s,%s", subTable, row.getKey());	
	}

	private String deleteExisting(DataStoreRow row) {
		String subTable = ComplexInsertColumn.getSubTableName(row.getKind(), DataStoreColumn.getEncodedName(this.name, this.type));
		List<DataStoreColumn> subTableColumns = new ArrayList<DataStoreColumn>();
		subTableColumns.add(DataStoreColumn.create("key", String.class));				
		subTableColumns.add(DataStoreColumn.create(name, this.type));	
		subTableColumns.add(DataStoreColumn.create("order", Long.class)); // order has to be the last column in the key to allow restricting by the value column
		Schema.ensureTable(SchemaMapper.kindToColumnFamily(subTable), subTableColumns);
		
		DatastoreClient client = new DatastoreClient();
		Query q = new Query(subTable);
		q.addFilter("key", FilterOperator.EQUAL, row.getKey());
		client.delete(q);
		return subTable;
	}

	/* (non-Javadoc)
	 * @see com.FalcoLabs.Fido.api.datastore.DataStoreColumn#getEncodedName()
	 */
	@Override
	public String getEncodedName() {
		return  this.isSearchableType(this.type) ? DataStoreColumn.getComplexNameFromSimpleName(super.getEncodedName()) : super.getEncodedName();
	}
	
	/* (non-Javadoc)
	 * @see com.FalcoLabs.Fido.api.datastore.DataStoreColumn#getType()
	 */
	@Override
	public Class<?> getType() {
		return this.isSearchableType(this.type) ? String.class : this.type; // Column type is always string as we store a string pointer to the actual column family that has the typed value
	}

	protected static String getSubTableName(String kind, String encodedName) {
		if (kind == null) {
			throw new DatastoreServiceException(messages.KIND_REQUIRED_FOR_MULTIVALUED_PROP_ERROR);
		}
		return String.format("%s_%s", kind, DataStoreColumn.getComplexNameFromSimpleName(encodedName));
	}	
	
	private Class<?> validateType(List<?> values) {
		Class<?> type = null;
		for (Object o : values) {						
			if (null == type) {
				type = o.getClass();
			} else if (type != o.getClass()) {
				throw new DatastoreServiceException(messages.MULTIVALID_PROPS_SAME_TYPE_ERROR);				
			}		
		}
		return type;
	}
	
	private boolean isSearchableType(Class<?> type) {
		if (type == EmbeddedEntity.class ||
				type == Entity.class ||
				type == ArrayList.class) {
			return false;
		} else {
			return true;
		}
	}
}
