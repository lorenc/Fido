package com.FalcoLabs.Fido.api.datastore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DataStoreColumn {	
	private static String ComplexColumnPrefix = "[]";
	protected static Map<String, Class<?>> classStringMap = new HashMap<String, Class<?>>();
	protected String name;
	protected Class<?> type = null;
	protected Object value;
	
	public static DataStoreColumn create(String name) {
		if (DataStoreColumn.getIsComplexName(name)) {
			return new ComplexSelectColumn(name);
		} else {
			return new SimpleColumn(name);
		}	
	}
	
	public static DataStoreColumn create(String name, Class<?> type) {
		return new SimpleColumn(name, type);
	}
	
	public static DataStoreColumn create(String name, Object value) {
		return DataStoreColumn.create(null,  name, value);
	}
	
	public static DataStoreColumn create(DataStoreRow row, String name, Object value) {
		if (null != row && DataStoreColumn.getIsComplexType(value) && !DataStoreColumn.getIsComplexName(name)) {
			return new ComplexInsertColumn(row, name, (List<?>)value);
		} else if (!DataStoreColumn.getIsComplexType(value) && DataStoreColumn.getIsComplexName(name) && value != null && value instanceof String) {
			return new ComplexSelectColumn(name, (String)value);
		} else {
			return new SimpleColumn(name, value);
		}
	}
		
	protected static boolean getIsComplexName(String name) {
		return name.startsWith(DataStoreColumn.ComplexColumnPrefix);
	}
	
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
	
	public Object getValue() {
		return this.value;
	}
				
	public String getName() {
		return this.name;
	}
	
	public Class<?> getType() {
		return this.type;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getEncodedName() {
		return DataStoreColumn.getEncodedName(this.getName(), this.getType());
	}	
	
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
	
	protected static String decodedName(String name) {
		int i = name.indexOf("__");
		if (i > 0) {
			return name.substring(i + 2);
		}
		return name;		
	}
	
	public static String getSimpleNameFromComplexName(String name) {
		return name.substring(DataStoreColumn.ComplexColumnPrefix.length());
	}

	public static String getComplexNameFromSimpleName(String name) {
		return (DataStoreColumn.ComplexColumnPrefix + name);
	}
	
	protected static boolean getIsStringSerializableType(Class<?> type) {
		if (type == Key.class) {
			return true;
		}
		return false;
	}
}
