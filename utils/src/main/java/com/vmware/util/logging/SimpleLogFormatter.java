package com.vmware.util.logging;

import static com.vmware.util.StringUtils.NEW_LINE_CHAR;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * JDK Logger formatter. Used to format all log output.
 * For info and severe log level, the class and method name are not printed.
 * For all levels lower than info, the class and method name are printed.
 */
public class SimpleLogFormatter extends Formatter {


    public String format(LogRecord record) {

        Level logLevel = Logger.getLogger("com.vmware").getLevel();
        if (record.getMessage().isEmpty() && logLevel != Level.FINER && logLevel != Level.FINEST) {
            return NEW_LINE_CHAR;
        }

        // Create a StringBuilder to contain the formatted record
        StringBuilder sb = new StringBuilder();

        if (logLevel == Level.FINER || logLevel == Level.FINEST) {
            String className = record.getSourceClassName();
            if (className.contains(".")) {
                className = className.substring(className.lastIndexOf(".") + 1);
            }
            sb.append(className).append(".").append(record.getSourceMethodName()).append(" ");
        }

        if (logLevel != Level.INFO || (record.getLevel() == Level.WARNING || record.getLevel() == Level.SEVERE)) {
            // Get the level name and add it to the buffer
            String levelString = LogLevel.fromLevel(record.getLevel()).name();
            sb.append(levelString);
            sb.append(": ");
        }

        // Get the formatted message (includes localization
        // and substitution of parameters) and add it to the buffer
        sb.append(formatMessage(record));
        sb.append(NEW_LINE_CHAR);

        return sb.toString();
    }
}
