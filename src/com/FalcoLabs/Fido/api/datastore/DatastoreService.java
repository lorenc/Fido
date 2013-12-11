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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.FalcoLabs.FalcoWeb.Logging.Log;
import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;
import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.datastore.exceptions.EntityNotFoundException;
import com.FalcoLabs.Fido.api.localization.messages;
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
			throw new DatastoreServiceException(messages.NO_QUERY_SET_ERROR);
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
				throw new DatastoreServiceException(messages.ALL_KEYS_MUST_BE_SAME_KIND_ERROR);
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
