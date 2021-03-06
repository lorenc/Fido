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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

// Connection to a datasource
public class Connection {
	private static Cluster cluster;
	// session is threadsafe
	private static Session session;
	
	/**
	 * Instantiates a new connection.
	 */
	public Connection() {		
	}
		
	/**
	 * Gets the session associated with the connection.  
	 * @see <a href="http://www.datastax.com/drivers/java/2.0/apidocs/com/datastax/driver/core/Session.html">Cassandra Session</a>
	 *
	 * @return the session
	 */
	public synchronized Session getSession() {
		if (null == Connection.session) {
			if (-1 == DataStore.CONNECTION_PORT) {
				Connection.cluster = Cluster.builder()
						.addContactPoint(DataStore.CONNECTION_STRING).build();
			} else {
				Connection.cluster = Cluster.builder()
						.withPort(DataStore.CONNECTION_PORT)
						.addContactPoint(DataStore.CONNECTION_STRING).build();				
			}
			Connection.session = cluster.connect();			
		}
		return Connection.session;
	}
}
