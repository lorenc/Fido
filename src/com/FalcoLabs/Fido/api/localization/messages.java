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
