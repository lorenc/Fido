package com.FalcoLabs.Fido.api.datastore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;
import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.datastore.exceptions.EntityNotFoundException;
import com.datastax.driver.core.exceptions.InvalidTypeException;

public class DatastoreService {
	private static String LOG_TAG = DatastoreService.class.getName();
	private Query preparedQuery;
	private DatastoreClient client;	
	
	public DatastoreService(DatastoreClient client) {
		this.client = client;
	}
	
	public DatastoreClient getClient() {
		return this.client;
	}
	
	public DatastoreService prepare(Query q) {
		this.preparedQuery = q;
		return this;
	}

	public Entity asSingleEntity() {
		if (null == this.preparedQuery) {
			throw new DatastoreServiceException("no query set");
		}	
		try {
			List<DataStoreRow> rows = this.getClient().select(this.preparedQuery);
			if (rows.size() > 0) {
				Entity e = rows.get(0).getEntity();
				return e;
			}
		} catch (DatastoreServiceException dse) {
			if (InvalidTypeException.class != dse.getInner().getClass()) {
				Log.e(LOG_TAG, dse);
				throw dse;
			}			
		} catch(EntityNotFoundException enfe) {
			Log.v(LOG_TAG, enfe.toString());
		}
		return null;			
	}
	
	public int countEntities() {
		List<DataStoreRow> rows = this.getClient().select(this.preparedQuery);
		int counter = rows.size();
		return counter;
	}

	public List<Entity> asList(FetchOptions limit) {
		List<Entity> entities = new ArrayList<Entity>();
		try {
			List<DataStoreRow> rows = this.getClient().select(this.preparedQuery);
			for (DataStoreRow row : rows) {
				Entity e = row.getEntity();
				entities.add(e);
				if (entities.size() >= limit.getLimit()) {
					break;
				}
			}
		} catch(EntityNotFoundException enfe) {
			Log.e(LOG_TAG, enfe);			
		}
		return entities;
	}

	public void put(Entity e) {
		DataStoreRow r = new DataStoreRow(e);
		this.getClient().insert(r);
	}
	
	public Map<Key, Entity> get(List<Key> keys) {			
		String kind = null;
		String[] stringKeys = new String[keys.size()];
		for (int i=0; i<keys.size(); i++) {
			if (null == kind) {
				kind = keys.get(i).getKind();
			} else if (!kind.equals(keys.get(i).getKind())) {
				throw new DatastoreServiceException("All keys must be of the same kind");
			}
			stringKeys[i] = keys.get(i).toString();
		}
		Map<Key, Entity> map = new HashMap<Key, Entity>();
		for (Key k : keys) {
			try {
				Entity e = this.get(k);
				if (null != e) {
					map.put(e.getKey(), e);
				}
			} catch(EntityNotFoundException enfe) {
				
			}
		}
		/* 
		 * Use the slower but accurate path until the logic below is updated to handle
		 * when one or more keys aren't present but others are
		 * 
		Query query = new Query(stringKeys[0]);
		query.addFilter("key", FilterOperator.IN, stringKeys);
		ResultSet result = this.getClient().select(query);			
		for (Row row : result.all()) {
			DataStoreRow r = new DataStoreRow(row);
			Entity e = r.getEntity();
			map.put(e.getKey(), e);
		}
		*/						
		return map;
	}

	public Entity get(Key key) {
		Query query = new Query(key.getKind());
		query.addFilter("key", FilterOperator.EQUAL, key.toString());
		List<DataStoreRow> rows = this.getClient().select(query);
		if (0 == rows.size()) {
			throw new EntityNotFoundException(key);
		}			
		Entity e = rows.get(0).getEntity();
		return e;
	}

	public void delete(Key k) {
		this.getClient().delete(k);
	}

	public void delete(List<Key> keys) {
		this.getClient().delete(keys);
	}
	
	public void put(List<Entity> entities) {
		for (Entity e : entities) {
			this.put(e);
		}
	}	
}
