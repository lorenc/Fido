package com.FalcoLabs.Fido.api.datastore.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.FalcoLabs.Fido.api.datastore.DataStore;
import com.FalcoLabs.Fido.api.datastore.DataStoreColumn;
import com.FalcoLabs.Fido.api.datastore.DatastoreService;
import com.FalcoLabs.Fido.api.datastore.DatastoreServiceFactory;
import com.FalcoLabs.Fido.api.datastore.EmbeddedEntity;
import com.FalcoLabs.Fido.api.datastore.Entity;
import com.FalcoLabs.Fido.api.datastore.FetchOptions;
import com.FalcoLabs.Fido.api.datastore.Key;
import com.FalcoLabs.Fido.api.datastore.Query;
import com.FalcoLabs.Fido.api.datastore.Schema;
import com.FalcoLabs.Fido.api.datastore.SchemaMapper;
import com.FalcoLabs.Fido.api.datastore.Query.FilterOperator;

@RunWith(JUnit4.class)
public class Tests {
	private static String LOG_TAG = Tests.class.getName();
	private static Random r = new Random();
	
	private static String getKeyspace() {
		return "testkeyspace" + r.nextInt(Integer.MAX_VALUE);
	}

	@Test
	public void testKeyParentParse() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Key k = Key.parse("com.FalcoLabs.FalcoWeb.DataObjects.Model.User(\"com.FalcoLabs.FalcoWeb.DataObjects.Model.User\")/com.FalcoLabs.FalcoWeb.DataObjects.Model.User(\"/users/74b905ce-4bde-4cda-b6a8-eb5745513d3d\")/com.FalcoWeb.doggiefan.DataObjects.Model.UserAnswerSet(\"com.FalcoWeb.doggiefan.Games.Rater.Rater-What breed is the most active?-/users/74b905ce-4bde-4cda-b6a8-eb5745513d3d\")");
			org.junit.Assert.assertNotNull(k);
			org.junit.Assert.assertNotNull(k.getParent());
			org.junit.Assert.assertNotNull(k.getParent().getParent());
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testKeyParentParse");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	/*
	@Test
	public void testGETINClause() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			List<DataStoreColumn> columns = new ArrayList<DataStoreColumn>();
			columns.add(DataStoreColumn.create("key", Key.class));
			columns.add(DataStoreColumn.create("value", String.class));
			columns.add(DataStoreColumn.create("parent", Key.class));			
			Schema.ensureTable(SchemaMapper.kindToColumnFamily("test"), columns);
			
			Entity e1 = new Entity(new Key("test", "e1"));
			e1.setProperty("value", "bbb");
			service.put(e1);
			
			List<String> filterValues = new ArrayList<String>();
			filterValues.add("aaa");
			filterValues.add("bbb");
			filterValues.add("ccc");

			Query q = new Query("test");
			q.addFilter("value", FilterOperator.IN, filterValues);			
			Entity e2 = service.prepare(q).asSingleEntity();
			org.junit.Assert.assertEquals(e2.getProperty("value"), e1.getProperty("value"));
			
			q = new Query("test");
			q.addFilter("xvalue", FilterOperator.IN, filterValues);			
			e2 = service.prepare(q).asSingleEntity();
			org.junit.Assert.assertNull(e2);		
			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testGETSETListKeys");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	*/
	
	@Test
	public void testGETSETKey() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Entity e1 = new Entity(new Key("test", "e1"));
			Key k = new Key("test", "a");
			e1.setProperty("keys", k);
			service.put(e1);
			
			Entity e2 = service.get(new Key("test", "e1"));
			Key k2 = (Key)e2.getProperty("keys");
			org.junit.Assert.assertEquals(k, k2);
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testGETSETListKeys");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testGETSETListKeys() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Entity e1 = new Entity(new Key("test", "e1"));
			List<Key> keys = new ArrayList<Key>();
			keys.add(new Key("test", "a"));
			keys.add(new Key("test", "b"));
			keys.add(new Key("test", "c"));
			e1.setProperty("keys", keys);
			service.put(e1);
			
			Entity e2 = service.get(new Key("test", "e1"));
			List<Key> keys2 = (List<Key>)e2.getProperty("keys");
			org.junit.Assert.assertEquals(keys.size(), keys2.size());
			for (int i=0; i<keys2.size(); i++) {
				Key k1 = keys.get(i);
				Key k2 = keys2.get(i);
				org.junit.Assert.assertEquals(k1, k2);	
			}			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testGETSETListKeys");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testIntToLongPropertyStorage() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Entity e1 = new Entity(new Key("test", "e1"));
			e1.setProperty("value", 1);
			service.put(e1);
			
			Entity e2 = service.get(new Key("test", "e1"));
			org.junit.Assert.assertEquals(e2.getProperty("value").getClass(), Long.class);						
			org.junit.Assert.assertEquals(e2.getProperty("value"), 1L);
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testIntToLongPropertyStorage");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testMultiValuedIntPropOrder() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Entity e1 = new Entity(new Key("test", "e1"));
			ArrayList<Long> value = new ArrayList<Long>();
			value.add(3L);
			value.add(-9324L);
			value.add(2343243L);
			value.add(234L);
			value.add(343L);
			e1.setProperty("value", value);
			service.put(e1);
			
			Entity e2 = service.get(new Key("test", "e1"));
			List<Integer> value2 = (List<Integer>) e2.getProperty("value");
			
			org.junit.Assert.assertEquals(value2.size(), value.size());						
			for (int i=0; i<value.size(); i++) {
				org.junit.Assert.assertEquals(value2.get(i), value.get(i));
			}
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultiValuedIntPropOrder");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testSortIntOrder() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			
			Entity e1 = new Entity(new Key("test", "e1"));
			e1.setProperty("searchprop", "123");
			e1.setProperty("sortprop", 1L);
			service.put(e1);
			
			Entity e2 = new Entity(new Key("test", "e2"));
			e2.setProperty("searchprop", "123");
			e2.setProperty("sortprop", 2L);
			service.put(e2);
			
			Entity e3 = new Entity(new Key("test", "e3"));
			e3.setProperty("searchprop", "123");
			e3.setProperty("sortprop", 3L);
			service.put(e3);
			
			Query q = new Query("test");
			q.addSort("sortprop", Query.SortDirection.DESCENDING);
			q.addFilter("searchprop", FilterOperator.EQUAL, "123");
			List<Entity> result = service.prepare(q).asList(new FetchOptions());
			org.junit.Assert.assertEquals(3, result.size());
			org.junit.Assert.assertEquals(3L, result.get(0).getProperty("sortprop"));
			org.junit.Assert.assertEquals(2L, result.get(1).getProperty("sortprop"));
			org.junit.Assert.assertEquals(1L, result.get(2).getProperty("sortprop"));			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSortIntOrder");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testSetNonSearchableComplexProp() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			List<EmbeddedEntity> values = new ArrayList<EmbeddedEntity>();
			Key k = new Key("test2", "nonsearchablecomplex");
			EmbeddedEntity embedded = new EmbeddedEntity(new Key("test3", "embedded"));
			values.add(embedded);
			values.add(embedded);			
			Entity e = new Entity(k);
			String propName = "nonsearchablecomplex";
			e.setProperty(propName, values);
			service.put(e);
			Entity e2 = service.get(k);			
			org.junit.Assert.assertEquals(((List<EmbeddedEntity>)e.getProperty(propName)).size(), ((List<EmbeddedEntity>)e2.getProperty(propName)).size());
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSetNonSearchableComplexProp");
			org.junit.Assert.fail();
		}		
		finally {
			DataStore.dropKeyspace();
		}
	}
	
	@Test
	public void testSetKeyPropertyWithParent() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			Key k = new Key("test", "root");
			Entity p = new Entity(k);
			service.put(p);
			
			Entity c = new Entity(new Key(k, "test", "child"));
			c.setProperty("child", true);
			service.put(c);;
			
			Entity e = service.get(c.getKey());
			org.junit.Assert.assertEquals(c.getProperty("child"), e.getProperty("child"));
		}
		catch(Exception e) {			
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultivaluedIntSearch");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testMultivaluedIntSearch() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testMultivaluedIntSearch");
			Entity e = new Entity(k);
			String propertyName = "searchmultiint";
			List<Integer> propertyValue = new ArrayList<Integer>();
			propertyValue.add(1);
			propertyValue.add(2);
			propertyValue.add(3);
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			
			Query q = new Query("test");
			q.addFilter(propertyName, FilterOperator.EQUAL, 2);
			service.prepare(q);
			Entity e2 = service.asSingleEntity();
			org.junit.Assert.assertNotNull(e2);

			q = new Query("test");
			q.addFilter(propertyName, FilterOperator.EQUAL, "searchstringXXXX");
			service.prepare(q);
			Entity e3 = service.asSingleEntity();
			org.junit.Assert.assertNull(e3);
			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultivaluedIntSearch");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}	
	
	@Test
	public void testMultivaluedStringSearch() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
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
			org.junit.Assert.assertNotNull(e2);

			q = new Query("test");
			q.addFilter(propertyName, FilterOperator.EQUAL, "searchstringXXXX");
			service.prepare(q);
			Entity e3 = service.asSingleEntity();
			org.junit.Assert.assertNull(e3);
			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultivaluedStringSearch");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}	

	@Test
	public void testPropertyUpdate() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testPropertyUpdate");
			Entity e = new Entity(k);
			e.setProperty("property", "aaa");
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			
			e = new Entity(k);
			e.setProperty("property", "bbb");
			service.put(e);
			
			Query q = new Query("test");
			q.addFilter("property", FilterOperator.EQUAL, "aaa");
			service.prepare(q);
			Entity e2 = service.asSingleEntity();
			org.junit.Assert.assertNull(e2);
			
			q = new Query("test");
			q.addFilter("property", FilterOperator.EQUAL, "bbb");
			service.prepare(q);
			Entity e3 = service.asSingleEntity();
			org.junit.Assert.assertEquals("bbb", e3.getProperty("property"));
			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testPropertyUpdate");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testSimpleStringSearch() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testSimpleStringSearch");
			Entity e = new Entity(k);
			e.setProperty("search1", "value1");
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			
			Query q = new Query("test");
			q.addFilter("search1", FilterOperator.EQUAL, "value1");
			service.prepare(q);
			Entity e2 = service.asSingleEntity();
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", e.getProperty("search1"), e2.getProperty("search1"));
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultiMultivaluedProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testMultiMultivaluedProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testMultiMultivaluedProperty");
			Entity e = new Entity(k);
			
			String propertyName2 = "multstring2";
			List<String> propertyValue2 = new ArrayList<String>();
			propertyValue2.add("c");
			propertyValue2.add("b");
			propertyValue2.add("a");
			e.setProperty(propertyName2, propertyValue2);
			
			String propertyName1 = "multiint1";
			List<Integer> propertyValue1 = new ArrayList<Integer>();
			propertyValue1.add(1);
			propertyValue1.add(2);
			propertyValue1.add(3);
			e.setProperty(propertyName1, propertyValue1);
			
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((List<Integer>)e.getProperty(propertyName1)).size(), ((List<Integer>)e2.getProperty(propertyName1)).size());			
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((List<String>)e.getProperty(propertyName2)).size(), ((List<String>)e2.getProperty(propertyName2)).size());			
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultiMultivaluedProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testMultivaluedIntProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testMultivaluedIntProperty");
			Entity e = new Entity(k);
			String propertyName = "multiint1";
			List<Long> propertyValue = new ArrayList<Long>();
			propertyValue.add(1L);
			propertyValue.add(2L);
			propertyValue.add(3L);
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((List<Integer>)e.getProperty(propertyName)).size(), ((List<Integer>)e2.getProperty(propertyName)).size());
			for (Map.Entry<String, Object> p : e.getProperties().entrySet()) {
				org.junit.Assert.assertEquals("failure - Property Values Don't Match", p.getValue(), e2.getProperty(p.getKey()));
			}
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultivaluedIntProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultivaluedStringProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testMultivaluedStringProperty");
			Entity e = new Entity(k);
			String propertyName = "multistring1";
			List<String> propertyValue = new ArrayList<String>();
			propertyValue.add("string1");
			propertyValue.add("string2");
			propertyValue.add("string3");
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((List<String>)e.getProperty(propertyName)).size(), ((List<String>)e2.getProperty(propertyName)).size());
			for (Map.Entry<String, Object> p : e.getProperties().entrySet()) {
				org.junit.Assert.assertEquals("failure - Property Values Don't Match", p.getValue(), e2.getProperty(p.getKey()));
			}
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testMultivaluedStringProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
		
	@Test
	public void testSetGetIntProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testSetGetIntProperty");
			Entity e = new Entity(k);
			String propertyName = "int1";
			long propertyValue = 123456;
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", e.getProperty(propertyName), e2.getProperty(propertyName));
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSetGetIntProperty");
		}
	}
	
	@Test
	public void testSetGetListOfEmbeddedObjectProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testSetGetListOfEmbeddedObjectProperty");
			Entity e = new Entity(k);
			e.setProperty("string1", "stringvalue1");
			String propertyName = "embedded1";
			EmbeddedEntity propertyValue = new EmbeddedEntity();
			propertyValue.setProperty("embeddedname", "embeddedvalue");
			List<EmbeddedEntity> listOfValues = new ArrayList<EmbeddedEntity>();
			listOfValues.add(propertyValue);
			listOfValues.add(propertyValue);
			listOfValues.add(propertyValue);
			e.setProperty(propertyName, listOfValues);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((List<?>)e.getProperty(propertyName)).size(), ((List<?>)e2.getProperty(propertyName)).size());
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSetGetListOfEmbeddedObjectProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testSetGetEmbeddedObjectProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("test", "testSetGetEmbeddedObjectProperty");
			Entity e = new Entity(k);
			e.setProperty("string1", "stringvalue1");
			String propertyName = "embedded1";
			EmbeddedEntity propertyValue = new EmbeddedEntity();
			propertyValue.setProperty("embeddedname", "embeddedvalue");
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", ((EmbeddedEntity)e.getProperty(propertyName)).getProperty("embeddedname"), ((EmbeddedEntity)e2.getProperty(propertyName)).getProperty("embeddedname"));
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSetGetEmbeddedObjectProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
	
	@Test
	public void testSetGetStringProperty() {
		try {
			DataStore.setKeyspace(Tests.getKeyspace());
			Key k = new Key("testSetGetStringProperty", "testSetGetStringProperty");
			Entity e = new Entity(k);
			String propertyName = "string1";
			String propertyValue = "stringvalue1";
			e.setProperty(propertyName, propertyValue);
			DatastoreService service = DatastoreServiceFactory.getDatastoreService();
			service.put(e);
			Entity e2 = service.get(k);
			org.junit.Assert.assertEquals("failure - Property Values Don't Match", e.getProperty(propertyName), e2.getProperty(propertyName));
		} catch(Exception e) {
			Logger.getLogger(LOG_TAG).log(Level.SEVERE, "Failed - testSetGetStringProperty");
			org.junit.Assert.fail();
		}
		finally {
			DataStore.dropKeyspace();
		}		
	}
}
