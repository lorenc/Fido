package com.FalcoLabs.Fido.api.datastore;

import org.apache.commons.lang3.StringUtils;

public class SimpleColumn extends DataStoreColumn {
	public static String LOG_TAG = SimpleColumn.class.getName();

	public SimpleColumn(String name, Class<?> type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}
	
	public SimpleColumn(String name, Object value) {
		this(name, value.getClass(), value);
	}
		
	public SimpleColumn(String name, Class<?> type) {
		this(name, type, null);
	}
	
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
