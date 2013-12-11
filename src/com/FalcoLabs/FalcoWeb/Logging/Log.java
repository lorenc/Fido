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
