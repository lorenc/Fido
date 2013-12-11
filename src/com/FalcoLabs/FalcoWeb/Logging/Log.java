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

package com.FalcoLabs.FalcoWeb.Logging;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.FalcoLabs.Fido.api.localization.messages;

public class Log {
	 
	private static boolean enabled = true;
	private static String LOG_TAG = Log.class.getName();
	private static Hashtable<String, Logger> loggers = new Hashtable<String, Logger>();		

	public static void setEnabled(boolean value) {
		Log.enabled = value;
	}

	public static void v(String tag, String s, Object ... params) {
		Log.log(tag, s, Level.INFO, params);
	}

	public static void w(String tag, String s, Object ... params) {
		Log.log(tag, s, Level.WARNING, params);
	}

	public static void e(String tag, String s, Object ... params) {
		Log.log(tag, s, Level.SEVERE, params);
	}

	public static void e(String tag, Exception e) {
		if (!Log.enabled) {
			return;
		}		
		try {
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));		
			Log.e(tag, e.toString() + "\r\n" + stringWriter.toString());
		} catch(Exception e2) {
			Log.e(LOG_TAG, messages.get(messages.FAILED_TO_GET_STACKTRACE_ERROR) + e2.toString() + "\r\n" + e.toString());
		}
	}	
	
	private static Logger getLogger(String tag) {
		synchronized (Log.loggers) {
			if (!Log.loggers.containsKey(tag)) {
				Log.loggers.put(tag, Logger.getLogger(tag));
			}
			return Log.loggers.get(tag);
		}
	}
	
	private static void log(String tag, String s, Level l, Object ... params) {
		if (!Log.enabled) {
			return;
		}		
		try {
			Log.getLogger(tag).log(l, String.format(s, params));
		} catch(Exception e) {
			Log.getLogger(LOG_TAG).log(Level.SEVERE, s);
		}	
	}
}
