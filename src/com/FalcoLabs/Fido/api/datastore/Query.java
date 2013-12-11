package com.FalcoLabs.Fido.api.datastore;

import java.util.ArrayList;
import java.util.List;

import com.FalcoLabs.Fido.api.datastore.exceptions.EntityNotFoundException;
import com.FalcoLabs.Fido.api.localization.messages;

public class Query {
	private String kind;
	private Key ancestor;
	private List<FilterPredicate> predicates;
	private String sortColumn;
	private SortDirection sortDirection = SortDirection.NONE;
	private String preparedQuery;
	private List<Object> values;
	
	public enum SortDirection {
		DESCENDING,
		ASCENDING,
		NONE		
	}
	
	public enum FilterOperator {
		IN,
		EQUAL, 
		LESS_THAN,
		LESS_THAN_OR_EQUAL,
		GREATER_THAN,
		GREATER_THAN_OR_EQUAL;
	}
	
	public Query(String kind) {
		this.kind = kind;
	}
	
	public String getKind() {
		return kind;
	}
	
	public Key getAncestor() {
		return this.ancestor;
	}
	
	public String getSortColumn() {
		return this.sortColumn;
	}
	
	public SortDirection getSortDirection() {
		return this.sortDirection;
	}
	
	public Query setAncestor(Key k) {
		this.ancestor = k;
		return this;
	}

	public Query setFilter(FilterPredicate value) {
		this.addPredicate(value);
		return this;
	}

	public List<FilterPredicate> getPredicates() {
		return this.predicates;
	}
	
	public void addPredicate(FilterPredicate value) {
		if (null == this.predicates) {
			this.predicates = new ArrayList<FilterPredicate>();
		}
		this.predicates.add(value);
	}	
	
	public void addSort(String column, SortDirection direction) {
		this.sortColumn = column;
		this.sortDirection = direction;
	}

	public void addFilter(String name, FilterOperator operator, Object value) {
		this.addPredicate(new FilterPredicate(name, operator, value));
	}
	
	public String getQuery() {
		return this.preparedQuery;
	}
	
	public List<Object> getValues() {
		return this.values;
	}
	
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
	
	public static class FilterPredicate {
		private String name;
		private FilterOperator operator;
		private Object value;
		private Query query;
		private boolean complex = false;
		
		public FilterPredicate(String name, FilterOperator operator, Object value) {
			this.name = name;
			this.operator = operator;			
			this.value = value;
		}
	
		public void setQuery(Query value) {
			this.query = value;
		}
		
		public String getName() {
			return this.name;
		}
		
		public FilterOperator getOperator() {
			return this.operator;
		}
		
		public Object getValue() {
			return this.value;
		}
		
		public boolean isComplex() {
			return this.complex || this.isComplexProperty();
		}
		
		public SimpleColumn getColumn() {
			return new SimpleColumn(this.name, this.getValue().getClass(), this.value); 
		}
		
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
				for (Object o : values) {
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
