package com.FalcoLabs.Fido.api.localization;

import java.util.Locale;
import java.util.ResourceBundle;

public class messages {
	public static final String KEYSPACE_LENGTH_ERROR = "KEYSPACE_LENGTH_ERROR";
	public static final String MUST_SET_KEYSPACE_ERROR = "MUST_SET_KEYSPACE_ERROR";
	public static final String FAILED_TO_GET_STACKTRACE_ERROR = "FAILED_TO_GET_STACKTRACE_ERROR";
	public static final String MULTIVALID_PROPS_SAME_TYPE_ERROR = "MULTIVALID_PROPS_SAME_TYPE_ERROR";
	public static final String KIND_REQUIRED_FOR_MULTIVALUED_PROP_ERROR = "KIND_REQUIRED_FOR_MULTIVALUED_PROP_ERROR";
	public static final String NO_QUERY_SET_ERROR = "NO_QUERY_SET_ERROR";
	public static final String ALL_KEYS_MUST_BE_SAME_KIND_ERROR = "ALL_KEYS_MUST_BE_SAME_KIND_ERROR";
	public static final String MUST_CALL_SETKEYSPACE_ERROR = "MUST_CALL_SETKEYSPACE_ERROR";
	public static final String INVALID_KEY_NAME_ERROR = "INVALID_KEY_NAME_ERROR";
	public static final String NO_COLUMN_FOUND_FOR_TYPE_ERROR = "NO_COLUMN_FOUND_FOR_TYPE_ERROR";
	
	public static String get(String key) {
		ResourceBundle messages = ResourceBundle.getBundle("messages", Locale.getDefault());
		String value = messages.getString(key);
		return value;
	}
}
