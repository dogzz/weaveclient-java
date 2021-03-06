package org.exfio.weave.util;

import org.apache.commons.logging.LogFactory;
import org.mozilla.gecko.background.common.log.Logger;

public class Log {

	protected static final String logtag = "weaveclient";
	private static boolean initLog = false;
	
	public static void init(String level) {
		initLog = true;
	
		//Initialise Apache commons logging
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showlogname", "true");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", level);

		//Explicitly set log level for our default logger 
		System.setProperty("org.apache.commons.logging.simplelog.log." + logtag, level);

		//Enable http logging if level is debug or trace
		if ( level.toLowerCase().matches("debug|trace") ) {			
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
			System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
		}
		
		//Enable mozilla logging
		Logger.init(level);
	}

	public static org.apache.commons.logging.Log getInstance() {
		return getInstance(logtag);
	}

	public static org.apache.commons.logging.Log getInstance(String context) {
		if ( !initLog ) {
			System.err.println("Log not initialised, setting default log level to warn");
			init("warn");
		}	
		return LogFactory.getLog(context);
	}

	public static void setLogLevel(String level) {
		System.setProperty("org.apache.commons.logging.simplelog.defaultlog", level);
		setLogLevel(logtag, level);
	}
	
	public static void setLogLevel(String logger, String level) {
		if ( !initLog ) {
			System.err.println("Log not initialised, setting default log level to warn");
			init("warn");
		}
		System.setProperty("org.apache.commons.logging.simplelog.log." + logger, level);
	}
	
	public static void trace(String tag, String message) { getInstance(tag).trace(message); }
	public static void debug(String tag, String message) { getInstance(tag).debug(message); }
	public static void info(String tag, String message)  { getInstance(tag).info(message); }
	public static void warn(String tag, String message)  { getInstance(tag).warn(message); }
	public static void error(String tag, String message) { getInstance(tag).error(message); }
	public static void fatal(String tag, String message) { getInstance(tag).fatal(message); }
	public static void t(String tag, String message)     { getInstance(tag).trace(message); }
	public static void d(String tag, String message)     { getInstance(tag).debug(message); }
	public static void i(String tag, String message)     { getInstance(tag).info(message); }
	public static void w(String tag, String message)     { getInstance(tag).warn(message); }
	public static void e(String tag, String message)     { getInstance(tag).error(message); }
	public static void wtf(String tag, String message)   { getInstance(tag).fatal(message); }	
}
