Fido
====

Fido is an easy to use Cassandra client library designed to simplify the process of using the Cassandra database.

Fido manages your column families and indexes for you freeing you up to spend your time on your design and not on syntax.

Fid is designed to be as thin as possible so that it doesn't impose any performance penalties.

Getting Started
-------------------------

1.  Install Cassandra by following the instructions at http://wiki.apache.org/cassandra/GettingStarted.  Fido requires Cassandra version 2.0.0 or later.

2.  Build or Download the Fido libraries and include them in your project.  You can download v1 of the Fido library from https://github.com/lorenc/Fido/releases.  If building your own binaries Fido is built using maven.  

3.  You will also need to include a few support libraries including:
	- datastax java driver at https://github.com/datastax/java-driver 
	- slf4j-api-1.7.5.jar from http://www.slf4j.org/download.html
	- cassandra-all-[version].jar from your cassandra installation
	- guava-15.0.jar from https://code.google.com/p/guava-libraries/wiki/Release15
	- jackson-all-1.8.2.jar from http://jackson.codehaus.org/
	- libthrift-0.7.0.jar from http://repo1.maven.org/maven2/org/apache/thrift/libthrift/
	- metrics-core-2.2.0.jar from http://mvnrepository.com/artifact/com.yammer.metrics/metrics-core/2.0.0-BETA19
	- netty-3.8.0.Final.jar from http://netty.io/downloads.html
	- snappy-java-1.1.1.jar from https://code.google.com/p/snappy-java/
	- commons-lang3-3.1.jar from http://commons.apache.org/proper/commons-lang/download_lang.cgi
	- Java 1.7 libraries

4.  In your project tell Fido how to connect to the Cassandra database:
		
		DataStore.setContactPoint("127.0.0.1");
		DataStore.setPort(9042);
		DataStore.setReplicationFactor(1);
	 
	In this example our Cassandra database is running on the same machine as our java application using the default port.

5.  Define your keyspace.  The keyspace can be any value you want but must be 30 characters or less in length.

	DataStore.setKeyspace("mykeyspace");
	
Examples
-------------------------

Javadoc can be found at: http://lorenc.github.io/Fido/

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
		Entity sheepDog = service.get(k);
	
2.  Searching for an object

	You can search for an object by it's key or by any of the properties defined on the object.

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

	Objects can be deleted by using their associated key.
			
		Key k = new Key("dog", "pug");
		Entity e = new Entity(k);
		DatastoreService service = DatastoreServiceFactory.getDatastoreService();
		service.put(e);
		service.delete(k);
		try {
			Entity e2 = service.get(e.getKey());
		} catch(EntityNotFoundException enfe) {
		}

		
License
-------

Copyright 2015, Falco Labs LLC.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.			
