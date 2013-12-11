package com.FalcoLabs.Fido.api.datastore;

import com.FalcoLabs.Fido.api.datastore.exceptions.DatastoreServiceException;
import com.FalcoLabs.Fido.api.localization.messages;

public class DatastoreServiceFactory {
	
	public static DatastoreService getDatastoreService() {		
		if (null == DataStore.KEYSPACE_NAME) {
			throw new DatastoreServiceException(messages.MUST_CALL_SETKEYSPACE_ERROR); 
		}
		return new DatastoreService(new DatastoreClient());
	}
	
	public static Connection getConnection() {
		return new Connection();
	}
}
