Fido
====

Fido is an easy to use Cassandra client library designed to simplify the process of using the Cassandra database.

Fido manages your column families and indexes for you freeing you up to spend your time on your design and not on syntax.

Fid is designed to be as thin as possible so that it doesn't impose any performance penalties.

Getting Started
-------------------------

1.  Install Cassandra by following the instructions at http://wiki.apache.org/cassandra/GettingStarted

2.  Build or Download the Fido libraries and include them in your project

3.  In your project tell Fido how to connect to the Cassandra database:
		
		DataStore.setContactPoint("127.0.0.1");
		DataStore.setReplicationFactor(1);
	 
	In this example our Cassandra database is running on the same machine as our java application.

4.  Define your keyspace.  The keyspace can be any value you want but must be 30 characters or less in length.

	DataStore.setKeyspace("mykeyspace");
	
Examples
-------------------------

Additional examples can be found at https://github.com/lorenc/Fido/tree/master/src/com/FalcoLabs/Fido/api/datastore/test

1.  Creating, saving, and retrieving a new object

	Every object used by Fido can be identified by a Key.  A key has two parts:
	
	1.  Kind
	2.  Name
	
	The Kind can be thought of as the name of the table or column family.  The Name is how the item is identified within the Kind.  Every object used by Fido has a unique Kind/Name combination. 
	
		Key k = new Key("dog", "sheepdog");
		Entity e = new Entity(k);
		e.setProperty("color", "brown");
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(e);
		Entity sheepDog = service.get(e);
	
2.  Searching for an object

		Key k = new Key("test", "testSimpleStringSearch");
		Entity e = new Entity(k);
		e.setProperty("search1", "value1");
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(e);
		
		Query q = new Query("test");
		q.addFilter("search1", FilterOperator.EQUAL, "value1");
		service.prepare(q);
		Entity e2 = service.asSingleEntity();

3.  Searching for multi-valued properties

Fido adds the ability to search for multi-valued properties that are of intrinsic types.  

	Key k = new Key("test", "testMultivaluedStringSearch");
	Entity e = new Entity(k);
	String propertyName = "searchmultistring";
	List<String> propertyValue = new ArrayList<String>();
	propertyValue.add("searchstring1");
	propertyValue.add("searchstring2");
	propertyValue.add("searchstring3");
	e.setProperty(propertyName, propertyValue);
	DatastoreService service = DatastoreServiceFactory.getDatastoreService();
	service.put(e);
	
	Query q = new Query("test");
	q.addFilter(propertyName, FilterOperator.EQUAL, "searchstring1");
	service.prepare(q);
	Entity e2 = service.asSingleEntity();
			
4.  Deleting an object
			
		Key k = new Key("dog", "pug");
		Entity e = new Entity(k);
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(e);
		service.delete(k);
		try {
			Entity e2 = service.get(e);
		} catch(EntityNotFoundException e) {
		}

			