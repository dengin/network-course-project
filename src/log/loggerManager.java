package log;

import org.apache.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.status.StatusLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

public class loggerManager
{
    private static HashMap<Class<?>, Logger> loggers = null;

    public static Logger getInstance(Class<?> cls)
    {
        if (loggers == null)
        {
            StatusLogger statusLogger = StatusLogger.getLogger();
            Level statusLoggerLevel = statusLogger.getLevel();
            statusLogger.setLevel(Level.OFF);
            LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
            try
            {
                context.setConfigLocation(new URI("log/log4j2.xml"));
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
            statusLogger.setLevel(statusLoggerLevel);
            loggers = new HashMap<Class<?>, Logger>();
        }

        Logger logger = loggers.get(cls);
        if (logger == null)
        {
            logger = Logger.getLogger(cls);
            loggers.put(cls, logger);
        }
        return logger;
    }
}
