package com.FalcoLabs.Fido.api.datastore;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.serializers.BinarySerializer;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Row;

public class DataStoreRow {
	private static String LOG_TAG = DataStoreRow.class.getName();
	private String kind;
	private Map<String, DataStoreColumn> columns = new HashMap<String, DataStoreColumn>();
	
	public DataStoreRow(String kind) {
		this.kind = kind;
	}
	
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
	
	public DataStoreRow(Row row) {
		for (Definition column : row.getColumnDefinitions()) {
			DataStoreColumn c = DataStoreColumn.create(column.getName());
			c.setValue(DataStoreRow.getValueFromType(column.getType(), row, column, c.getType()));
			if (null != c.getValue()) {
				this.addColumn(c);
			}
		}
	}
	
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
	
	public String getKind() {
		return this.kind;
	}
	
	public String getColumnFamily() {
		return SchemaMapper.kindToColumnFamily(this.getKind());
	}
	
	public Key getKey() {
		return (Key)(null != this.getColumn("key") ? this.getColumn("key").getValue() : null);
	}
	
	public Map<String, DataStoreColumn> getColumns() {
		return this.columns;
	}
	
	public DataStoreColumn getColumn(String name) {
		return this.columns.get(name);
	}
	
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
