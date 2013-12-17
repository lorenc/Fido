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
import java.util.List;

import com.FalcoLabs.Fido.api.datastore.exceptions.EntityNotFoundException;
import com.FalcoLabs.Fido.api.localization.messages;

// A Query against the datastore.  A query can be used to return a set of Entities that match the query search criteria.
public class Query {
	private String kind;
	private Key ancestor;
	private List<FilterPredicate> predicates;
	private String sortColumn;
	private SortDirection sortDirection = SortDirection.NONE;
	private String preparedQuery;
	private List<Object> values;
	
	// When sorting the results the sort order
	public enum SortDirection {
		DESCENDING,
		ASCENDING,
		NONE		
	}
	
	// Operator to apply when doing property filtering
	public enum FilterOperator {
		IN,
		EQUAL, 
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL;
	}
	
	/**
	 * Instantiates a new query with the specified kind.  The query will only return matches that have the same kind.
	 *
	 * @param kind the kind
	 */
	public Query(String kind) {
		this.kind = kind;
	}
	
	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public String getKind() {
		return kind;
	}
	
	/**
	 * Gets the ancestor.
	 *
	 * @return the ancestor
	 */
	public Key getAncestor() {
		return this.ancestor;
	}
	
	/**
	 * Gets the sort column.
	 *
	 * @return the sort column
	 */
	public String getSortColumn() {
		return this.sortColumn;
	}
	
	/**
	 * Gets the sort direction.
	 *
	 * @return the sort direction
	 */
	public SortDirection getSortDirection() {
		return this.sortDirection;
	}
	
	/**
	 * Sets the ancestor.
	 *
	 * @param k the k
	 * @return the query
	 */
	public Query setAncestor(Key k) {
		this.ancestor = k;
		return this;
	}

	/**
	 * Sets the filter.
	 *
	 * @param value the value
	 * @return the query
	 */
	public Query setFilter(FilterPredicate value) {
		this.addPredicate(value);
		return this;
	}

	/**
	 * Gets the predicates.
	 *
	 * @return the predicates
	 */
	public List<FilterPredicate> getPredicates() {
		return this.predicates;
	}
	
	/**
	 * Adds the predicate.
	 *
	 * @param value the value
	 */
	public void addPredicate(FilterPredicate value) {
		if (null == this.predicates) {
			this.predicates = new ArrayList<FilterPredicate>();
		}
		this.predicates.add(value);
	}	
	
	/**
	 * Adds the sort.
	 *
	 * @param column the column
	 * @param direction the direction
	 */
	public void addSort(String column, SortDirection direction) {
		this.sortColumn = column;
		this.sortDirection = direction;
	}

	/**
	 * Adds the filter.
	 *
	 * @param name the name
	 * @param operator the operator
	 * @param value the value
	 */
	public void addFilter(String name, FilterOperator operator, Object value) {
		this.addPredicate(new FilterPredicate(name, operator, value));
	}
	
	/**
	 * Gets the query.
	 *
	 * @return the query
	 */
	public String getQuery() {
		return this.preparedQuery;
	}
	
	/**
	 * Gets the values.
	 *
	 * @return the values
	 */
	public List<Object> getValues() {
		return this.values;
	}
	
	/**
	 * Prepare a query to be used in a search.
	 */
	public void prepare() {
		this.values = new ArrayList<Object>();
		StringBuilder b = new StringBuilder();
		
		List<FilterPredicate> predicates = this.getPredicates();
		if (null != predicates && predicates.size() > 0) {
			b.append(" WHERE ");
			int count = 0;
			for (FilterPredicate predicate : predicates) {
				if (count++ > 0) {
					b.append(" and ");
				}
				predicate.setQuery(this);
				b.append(predicate.getCql());
				if (!predicate.isComplex()) {
					if (predicate.getValue() instanceof List) {
						List<?> v = (List<?>)predicate.getValue();
						for (Object o : v) {
							this.values.add(o);
						}
					} else {
						this.values.add(predicate.getValue());
					}
				}
			}
		}

		if (null != this.getAncestor()) {
			if (b.length() > 0) {
				b.append(" and ");
			} else {
				b.append(" WHERE ");
			}
			b.append(" parent = ?");
			this.values.add(this.getAncestor().toString());
		}
		/*
		if (null != this.getSortColumn()) {
			b.append(" order by \"");
			b.append(this.getSortColumn().getEncodedName());
			b.append(this.getSortDirection() == Query.SortDirection.ASCENDING ? "\" asc " : " desc ");
		}
		*/
		
		this.preparedQuery = b.toString();		
	}
	
	// Represents a filter statement that is part of a query.  A query can have multiple FilterPredicates.
	public static class FilterPredicate {
		private String name;
		private FilterOperator operator;
		private Object value;
		private Query query;
		private boolean complex = false;
		
		/**
		 * Instantiates a new filter predicate.
		 *
		 * @param name the name
		 * @param operator the operator
		 * @param value the value
		 */
		public FilterPredicate(String name, FilterOperator operator, Object value) {
			this.name = name;
			this.operator = operator;			
			this.value = value;
		}
	
		/**
		 * Sets the query.
		 *
		 * @param value the new query
		 */
		public void setQuery(Query value) {
			this.query = value;
		}
		
		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}
		
		/**
		 * Gets the operator.
		 *
		 * @return the operator
		 */
		public FilterOperator getOperator() {
			return this.operator;
		}
		
		/**
		 * Gets the value.
		 *
		 * @return the value
		 */
		public Object getValue() {
			return this.value;
		}
		
		/**
		 * Checks if is complex.
		 *
		 * @return true, if is complex
		 */
		public boolean isComplex() {
			return this.complex || this.isComplexProperty();
		}
		
		/**
		 * Gets the column.
		 *
		 * @return the column
		 */
		public SimpleColumn getColumn() {
			return new SimpleColumn(this.name, this.getValue().getClass(), this.value); 
		}
		
		/**
		 * Gets the cql for this predicate.
		 *
		 * @return the cql
		 */
		public Object getCql() {			
			StringBuilder b = new StringBuilder();
			
			if (this.isComplexProperty()) {
				this.writeComplexProperty(b);
				this.complex = true;
				return b.toString();
			}
			
			SimpleColumn c = this.getColumn();
			
			b.append('"');			
			b.append(c.getEncodedName());
			b.append("\" ");
			switch (this.operator) {
			case IN:
				b.append(" IN ");
				b.append("(");
				List<?> values = (List<?>)this.value;
				int count = 0;
				for (@SuppressWarnings("unused") Object o : values) {
					if (count++ > 0) {
						b.append(',');
					}
					b.append('?');
				}
				b.append(")");
				break;
			case EQUAL:
				b.append(" = " );
				b.append("?");
				break;
			case LESS_THAN:
				b.append(" < ");
				b.append('?');
				break;
			case LESS_THAN_OR_EQUAL:
				b.append(" <= ");
				b.append('?');
				break;				
			case GREATER_THAN:
				b.append(" > ");
				b.append('?');
				break;
			case GREATER_THAN_OR_EQUAL:
				b.append(" >= ");
				b.append('?');
				break;								
			}
								
			return b.toString();
		}

		private void writeComplexProperty(StringBuilder b) {
			String propertyTableName = this.getComplexPropertyKind();
			DatastoreClient client = new DatastoreClient();
			Query propertyQuery = new Query(propertyTableName);
			propertyQuery.addFilter(this.name, this.operator, this.value);
			List<DataStoreRow> rows = client.select(propertyQuery);
			b.append( "key IN (");
			int count = 0;
			for (DataStoreRow row : rows) {
				if (count++ > 0) {
					b.append(",");
				}
				b.append("'");
				b.append(row.getColumn("key").getValue());
				b.append("'");
			}
			b.append(") ");
		}

		private String getComplexPropertyKind() {
			return ComplexInsertColumn.getSubTableName(this.query.getKind(), this.getColumn().getEncodedName());
		}
		
		private String getComplexPropertyColumnFamily() {
			return SchemaMapper.kindToColumnFamily(				
				this.getComplexPropertyKind());
		}
		
		private boolean isComplexProperty() {
			int count = 0;
			
			while (count++ < 2) {
				if (count > 1) {
					Schema.reloadSchema();
				}								
				if (true == Schema.haveSchemaItem(this.getComplexPropertyColumnFamily(), this.getColumn().getEncodedName(), null)) {
					return true;
				} else if (true == Schema.haveSchemaItem(SchemaMapper.kindToColumnFamily(this.query.getKind()), this.getColumn().getEncodedName(), null)) {
					return false;
				} 
			}
			// failed to find the schema for the comples and non complex property - property doesn't exist
			throw new EntityNotFoundException(messages.NO_COLUMN_FOUND_FOR_TYPE_ERROR + this.name);
		}	
	}	
}
