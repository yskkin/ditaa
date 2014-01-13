package yskkin.ascii2image.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Loggers {

	private static Handler handler;
	private static Level level;
	private static LogManager logManager = LogManager.getLogManager();

	public static Logger getLogger(Class<?> clazz) {
		Logger logger = Logger.getLogger(clazz.getName());
		logger.setUseParentHandlers(false);
		if (handler != null) {
			logger.addHandler(handler);
		}
		if (level != null) {
			logger.setLevel(level);
		}
		return logger;
	}

	public static void addFileOutputToAllLoggers(String logFileName) {
		Handler logHandler;
		if (logFileName != null) {
			try {
				logHandler = new FileHandler(logFileName);
				logHandler.setFormatter(new SimpleFormatter());
			} catch (SecurityException e) {
				System.err.println("Specified logfile is not writable. output to STDERR.");
				logHandler = new ConsoleHandler();
			} catch (IOException e) {
				System.err.println("IO error on specified logfile. output to STDERR.");
				logHandler = new ConsoleHandler();
			}
		} else {
			logHandler = new ConsoleHandler();
		}
		addHandlerToAllLoggers(logHandler);
	}

	public static void setLevelToAllLoggers(Level level) {
		Loggers.level = level;
		if (handler != null) {
			handler.setLevel(level);
		} else {
			Enumeration<String> loggerNames = logManager.getLoggerNames();
			while (loggerNames.hasMoreElements()) {
				logManager.getLogger(loggerNames.nextElement()).setLevel(level);
			}
		}
	}

	private static void addHandlerToAllLoggers(Handler handler) {
		Loggers.handler = handler;
		Enumeration<String> loggerNames = logManager.getLoggerNames();
		while (loggerNames.hasMoreElements()) {
			logManager.getLogger(loggerNames.nextElement()).addHandler(handler);
		}
	}
}
