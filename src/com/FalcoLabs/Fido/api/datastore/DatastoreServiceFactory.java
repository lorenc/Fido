package com.FalcoLabs.Fido.api.datastore;

import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;

public class DatastoreServiceFactory {
	
	public static DatastoreService getDatastoreService() {		
		if (null == DataStore.KEYSPACE_NAME) {
			throw new DatastoreServiceException("You must call DataStore.setKeyspace before accessing the data store"); 
		}
		return new DatastoreService(new DatastoreClient());
	}
	
	public static Connection getConnection() {
		return new Connection();
	}
}
