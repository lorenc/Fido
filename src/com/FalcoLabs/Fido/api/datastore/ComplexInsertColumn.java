package com.FalcoLabs.Fido.api.datastore;

import java.util.ArrayList;
import java.util.List;

import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;
import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;

public class ComplexInsertColumn extends DataStoreColumn {

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
				throw new DatastoreServiceException("mutlivalued properties must all be of the same type");				
			}
			if (null == subTable) {
				subTable = ComplexInsertColumn.getSubTableName(row.getKind(), DataStoreColumn.getEncodedName(this.name, this.type));
				List<DataStoreColumn> subTableColumns = new ArrayList<DataStoreColumn>();
				subTableColumns.add(DataStoreColumn.create("key", String.class));				
				subTableColumns.add(DataStoreColumn.create(name, this.type));	
				subTableColumns.add(DataStoreColumn.create("order", Long.class)); // order has to be the last column in the key to allow restricting by the value column
				Schema.ensureTable(SchemaMapper.kindToColumnFamily(subTable), subTableColumns);
				this.deleteExisting(subTable, row.getKey());
			}
			DataStoreRow subRow = new DataStoreRow(subTable);
			subRow.addColumn(DataStoreColumn.create(name, o));
			subRow.addColumn(DataStoreColumn.create("key", row.getKey()));
			subRow.addColumn(DataStoreColumn.create("order", order++));
			client.insert(subRow);
		}						
		this.value = String.format("%s,%s", subTable, row.getKey());	
	}
	
	private void deleteExisting(String subTable, Key key) {
		DatastoreClient client = new DatastoreClient();
		Query q = new Query(subTable);
		q.addFilter("key", FilterOperator.EQUAL, key);
		client.delete(q);
	}

	@Override
	public String getEncodedName() {
		return  this.isSearchableType(this.type) ? DataStoreColumn.getComplexNameFromSimpleName(super.getEncodedName()) : super.getEncodedName();
	}
	
	@Override
	public Class<?> getType() {
		return this.isSearchableType(this.type) ? String.class : this.type; // Column type is always string as we store a string pointer to the actual column family that has the typed value
	}

	public static String getSubTableName(String kind, String encodedName) {
		if (kind == null) {
			throw new DatastoreServiceException("kind must not be null when setting a multivalued property");
		}
		return String.format("%s_%s", kind, DataStoreColumn.getComplexNameFromSimpleName(encodedName));
	}	
	
	private Class<?> validateType(List<?> values) {
		Class<?> type = null;
		for (Object o : values) {						
			if (null == type) {
				type = o.getClass();
			} else if (type != o.getClass()) {
				throw new DatastoreServiceException("mutlivalued properties must all be of the same type");				
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
