package ru.autogenmash.core.utils;

import java.io.IOException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author Dymarchuk Dmitry
 * 26.10.2009 14:20:12
 */
public class LogUtil
{
    private static FileAppender _fileAppender;

    static
    {
        initLogAppender();
    }

    public static Logger getLogger(Class clazz)
    {
        Logger logger = Logger.getLogger(clazz);
        logger.addAppender(_fileAppender);
        return logger;
    }

    private static void initLogAppender()
    {
        PatternLayout layout = new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L - %m%n");
        try
        {
            _fileAppender = new FileAppender(layout, Utils.getLogsDir() + "/jform.log");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            _fileAppender = new FileAppender();
        }
    }

    private LogUtil()
    {
        // its util
    }
}
