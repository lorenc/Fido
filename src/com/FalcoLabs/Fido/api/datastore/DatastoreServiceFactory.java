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

import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.localization.messages;

// Factory used to get DataStoreService instances
public class DatastoreServiceFactory {
	
	/**
	 * Gets the datastore service.
	 *
	 * @return the datastore service
	 */
	public static DatastoreService getDatastoreService() {		
		if (null == DataStore.KEYSPACE_NAME) {
			throw new DatastoreServiceException(messages.MUST_CALL_SETKEYSPACE_ERROR); 
		}
		return new DatastoreService(new DatastoreClient());
	}
	
	/**
	 * Gets a new connection instance.
	 *
	 * @return the connection
	 */
	public static Connection getConnection() {
		return new Connection();
	}
}
