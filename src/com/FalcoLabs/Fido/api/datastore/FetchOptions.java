package com.FalcoLabs.Fido.api.datastore;

public class FetchOptions {

	private int limit = Integer.MAX_VALUE;
	private int offset = 0;
	
	public static final class Builder {

		public static FetchOptions withOffset(int offset) {
			return new FetchOptions().offset(offset);
		}

		public static FetchOptions withDefaults() {
			return new FetchOptions();
		}
		
	}
	
	public FetchOptions offset(int value) {
		this.offset = value;
		return this;
	}

	public FetchOptions limit(int value)
	{
		this.limit = value;
		return this;
	}
	
	public int getLimit() {
		return this.limit;
	}
	
	public int getOffset() {
		return this.offset;
	}
}
