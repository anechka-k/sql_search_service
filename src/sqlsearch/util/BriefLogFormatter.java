package sqlsearch.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A Java logging formatter that writes more compact output than the default
 */
public class BriefLogFormatter extends Formatter {

    /** Format used for log messages */
    private static final ThreadLocal<MessageFormat> messageFormat = new ThreadLocal<MessageFormat>() {
        @Override
        protected MessageFormat initialValue() {
            return new MessageFormat("{0,date,yyyy-MM-dd HH:mm:ss} {1}: {2}\n{3}");
        }
    };

    /** Logger instance at the top of the name tree */
    private static final Logger logger = Logger.getLogger("");

    /** singleton BriefLogFormatter instance */
    private static final BriefLogFormatter briefLogFormatter = new BriefLogFormatter();

    /**
     * Configures JDK logging to use this class for everything
     */
    static void init() {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers)
            handler.setFormatter(briefLogFormatter);
    }

    /**
     * Format the log record as follows:
     *
     *     Date Level Message ExceptionTrace
     *
     * @param       logRecord       The log record
     * @return                      The formatted string
     */
    @Override
    public String format(LogRecord logRecord) {
        Object[] arguments = new Object[4];
        arguments[0] = new Date(logRecord.getMillis());
        arguments[1] = logRecord.getLevel().getName();
        arguments[2] = logRecord.getMessage();
        Throwable exc = logRecord.getThrown();
        if (exc != null) {
            Writer result = new StringWriter();
            exc.printStackTrace(new PrintWriter(result));
            arguments[3] = result.toString();
        } else {
            arguments[3] = "";
        }
        return messageFormat.get().format(arguments);
    }

    private BriefLogFormatter() {}

}
