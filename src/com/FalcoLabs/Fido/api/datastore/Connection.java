package com.FalcoLabs.Fido.api.datastore;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class Connection {
	private static Cluster cluster;
	// session is threadsafe
	private static Session session;
	
	public Connection() {		
	}
		
	public synchronized Session getSession() {
		if (null == Connection.session) {
			Connection.cluster = Cluster.builder()
					//.withPort(DataStore.CONNECTION_PORT)
					.addContactPoint(DataStore.CONNECTION_STRING).build();
			Connection.session = cluster.connect();			
		}
		return Connection.session;
	}
}
