package ru.autogenmash.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * @author Dymarchuk Dmitry
 * @version
 * 11.09.2007 13:52:38
 */
public class Utils
{
    private Utils()
    {
    }

    public static final String DATE_TIME_TEMPLATE = "dd.MM.yyyy hh:mm:ss";

    private static final String OS = System.getProperty("os.name");
    private static final String OS_WIN = "Win";
    private static final String OS_QNX = "QNX";

//    private static String WIN_CNC_DIR = ".\\";
//    private static String WIN_TMP_DIR = "D:/CNC/tmp";
//    private static String WIN_CPS_DIR = "..\\cps";
//    private static String WIN_SHARE = "D:/cps/net/updates";
//    private static String QNX_CNC_DIR = "/CNC";
//    private static String QNX_TMP_DIR = "/CNC/tmp";// "/home/ftp/pub/tmp";
//    private static String QNX_CPS_DIR = "/cps";
//    private static String QNX_SHARE = "/cps/net/updates";

    private static String TARGET_DIR = ".//";//текущая
    private static String INI_DIR = ".//jini";
    private static String LOGS_DIR = ".//logs";
    private static String DOCS_DIR = INI_DIR + "//docs";
    private static String UPDATE_DIR = "..//cps//net//updates";
    private static String CPS_DIR = "..//cps";
    private static String TMP_DIR = ".//tmp";
    
    private static Log _log = LogFactory.getLog(Utils.class);


    static
    {
        initProperties();
    }

    public static boolean createPath(String itemName, boolean isFile)
    {
        String inputDirectoryName = itemName;
        if (isFile)
            inputDirectoryName = StringUtils.getDirectory(itemName);
        if (inputDirectoryName != null)
        {
            File directory = new File(inputDirectoryName);
            if (directory.exists() == false)
                return directory.mkdirs();
        }
        return false;
    }

    public static boolean isQnx()
    {
        return OS.equals(OS_QNX);
    }

    public static boolean isWin()
    {
        return OS.startsWith(OS_WIN);
    }

    public static void traceTime(long time, String text)
    {
        System.out.println(text + ": " + (double)(new Date().getTime() - time) / 1000);
    }

    public static void traceTime(Log log, long time, String text)
    {
        log.info(text + ": " + (double)(new Date().getTime() - time) / 1000);
    }

    public static void loadFileToWidget(final String fileName, Object widget)
    throws IOException, IllegalArgumentException, SecurityException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        String cpFile = "";

        BufferedReader source = new BufferedReader(new FileReader(fileName));

        int c;
        while ( (c = source.read()) != -1)
        {
            cpFile += (char)c;
        }

        String methodName = "setText";
        Class[] argTypes = { String.class };
        String[] args = { cpFile };
        widget.getClass().getMethod(methodName, argTypes).invoke(widget, args);

        source.close();
    }

    public static void loadFileToWidget(String fileName, Object widget, String encoding)
    throws IOException, IllegalArgumentException, SecurityException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        loadFileToWidget(fileName, widget, encoding, -1);
    }

    public static void loadFileToWidget(String fileName, Object widget, String encoding, int endChar)
    throws IOException, IllegalArgumentException, SecurityException,
        IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        FileInputStream source = new FileInputStream(fileName);
        loadFileToWidget(source, widget, encoding, endChar);
    }

    public static void loadFileToWidget(InputStream inputFileStream,
            Object widget, String encoding, int endChar) throws IOException,
            IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        BufferedReader source = new BufferedReader(new InputStreamReader(inputFileStream, encoding));
        String cpFile = "";
        int c;
        while ( (c = source.read()) != -1)
        {
            if (endChar >= 0)
                if (endChar == c)
                    break;
            cpFile += (char)c;
        }

        String methodName = "setText";
        Class[] argTypes = { String.class };
        String[] args = { cpFile };
        widget.getClass().getMethod(methodName, argTypes).invoke(widget, args);

        source.close();
    }

    public static void loadSystemResourceToWidget(String fileName, Object widget)
    throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
    {
        loadFileToWidget(ClassLoader.getSystemResourceAsStream(fileName), widget, "UTF8", -1);
    }

    public static String fileAsString(String fileName, String encoding) throws IOException
    {
        FileInputStream source = new FileInputStream(fileName);
        String result = streamAsString(source, encoding);
        source.close();

        return result;
    }

    private static String streamAsString(InputStream stream, String encoding)
    throws IOException
    {
        BufferedReader source = new BufferedReader(new InputStreamReader(stream, encoding));

        String result = "";
        int c;
        while ( (c = source.read()) != -1)
        {
            result += (char)c;
        }

        return result;
    }

    public static void copyFiles(String sourceFile, String destinationFile)
    throws IOException
    {
        FileInputStream sourceStream = new FileInputStream(sourceFile);
        FileOutputStream destinationStream = new FileOutputStream(destinationFile);

        copyFiles(sourceStream, destinationStream);

        sourceStream.close();
        destinationStream.close();
    }

    public static void copyFiles(InputStream sourceFile, OutputStream destinationFile)
    throws IOException
    {
        copyFiles(sourceFile, destinationFile, 256);
    }

    public static void copyFiles(InputStream sourceFile, OutputStream destinationFile,
            int bufferSize) throws IOException
    {
        synchronized (sourceFile)
        {
            synchronized (destinationFile)
            {
                byte[] buffer = new byte[bufferSize];
                while (true)
                {
                    int bytesRead = sourceFile.read(buffer);
                    if (bytesRead == -1)
                        break;
                    destinationFile.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    public static void clearFile(String fileName) throws IOException
    {
        FileOutputStream file = new FileOutputStream(fileName);
        file.close();
    }

    public static int toInt(Integer value)
    {
        if (value == null)
            return 0;
        else
            return value.intValue();
    }

    public static double toDouble(Double value)
    {
        if (value == null)
            return 0;
        else
            return value.doubleValue();
    }

    public static boolean toBoolean(Boolean value)
    {
        if (value == null)
            return false;
        else
            return value.booleanValue();
    }

    /**Проверить имя УП на наличие недопустимых символов
     * (с кодами меньшими 33 и большими 126).
     * @param cpName
     * @return
     */
    public static boolean checkFileNameEn(String cpName)
    {
        byte[] cpNameBytes = cpName.getBytes();
        for (int i = 0; i < cpNameBytes.length; i++)
            if (cpNameBytes[i] < 33 || cpNameBytes[i] > 126)
                return false;

        return true;
    }

    public static void saveToTxtFile(String fileName, String text, String encoding)
    throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileName), encoding));
        writer.write(text);
        writer.close();
    }

    public static String getTargetDir()
    {
      return TARGET_DIR;
    }
    
    public static String getIniDir()
    {
      return INI_DIR;
    }
    
    public static String getLogsDir()
    {
      return LOGS_DIR;
    }
    
    public static String getDocsDir()
    {
      return DOCS_DIR;
    }
    
    public static String getCpsDir()
    {
      return CPS_DIR;
    }
    
    public static String getTmpDir()
    {
      return TMP_DIR;
    }
    
    public static String getUpdateDir()
    {
      return UPDATE_DIR;
    }
    
//    public static String getQnxCncDir()
//    {
//        return QNX_CNC_DIR;
//    }

//    public static String getQnxCpsDir()
//    {
//        return QNX_CPS_DIR;
//    }
//
//    public static String getQnxTmpDir()
//    {
//        return QNX_TMP_DIR;
//    }
//
//    public static String getWinCncDir()
//    {
//        return WIN_CNC_DIR;
//    }
//
//    public static String getWinCpsDir()
//    {
//        return WIN_CPS_DIR;
//    }
//
//    public static String getWinTmpDir()
//    {
//        return WIN_TMP_DIR;
//    }

//    public static String getCncDir()
//    {
//        if (isWin())
//            return WIN_CNC_DIR;
//        else
//            return QNX_CNC_DIR;
//    }
//
//    public static String getCpsDir()
//    {
//        if (isWin())
//            return WIN_CPS_DIR;
//        else
//            return QNX_CPS_DIR;
//    }
//
//    public static String getTmpDir()
//    {
//        if (isWin())
//            return WIN_TMP_DIR;
//        else
//            return QNX_TMP_DIR;
//    }
//
//    public static String getShareDir()
//    {
//        if (isWin())
//            return WIN_SHARE;
//        else
//            return QNX_SHARE;
//    }

    private static void initProperties()
    {
        String warnMsg = "Инициализация параметров ядра провалена. Значения инициализированы по умолчанию";
        String warnSysMsg = "kernel properties initialization failed. Defaults values in use";

        InputStream iniPropertiesFile = ClassLoader.getSystemResourceAsStream("kernel.properties");
        Properties properties = new Properties();
        if (iniPropertiesFile != null)
        {
            try
            {
                properties.load(iniPropertiesFile);
                iniPropertiesFile.close();
            }
            catch (IOException e)
            {
                System.err.println(warnSysMsg);
                _log.warn(warnMsg);
            }
            
            TARGET_DIR = properties.getProperty("dirs.target", TARGET_DIR);
            INI_DIR = properties.getProperty("dirs.ini", INI_DIR);
            CPS_DIR = properties.getProperty("dirs.cps", CPS_DIR);
            UPDATE_DIR = properties.getProperty("dirs.update", UPDATE_DIR);
            TMP_DIR = properties.getProperty("dirs.tmp", TMP_DIR);
            
//            WIN_CNC_DIR = properties.getProperty("dirs.win.CNC", WIN_CNC_DIR);
//            WIN_TMP_DIR = properties.getProperty("dirs.win.CNC.tmp", WIN_TMP_DIR);
//            WIN_CPS_DIR = properties.getProperty("dirs.win.cps", WIN_CPS_DIR);
//            WIN_SHARE = properties.getProperty("dirs.win.share", WIN_SHARE);
//            QNX_CNC_DIR = properties.getProperty("dirs.qnx.CNC", QNX_CNC_DIR);
//            QNX_TMP_DIR = properties.getProperty("dirs.qnx.CNC.tmp", QNX_TMP_DIR);
//            QNX_CPS_DIR = properties.getProperty("dirs.qnx.cps", QNX_CPS_DIR);
//            QNX_SHARE = properties.getProperty("dirs.qnx.share", QNX_SHARE);
        }
        else
        {
            System.err.println(warnSysMsg);
            _log.warn(warnMsg);
        }
    }

    public static void touch(String fileName)
    throws IOException, InterruptedException
    {
        if (fileName == null || fileName.trim().equals(""))
            throw new IllegalArgumentException("File name must be correct");
       
				Writer w = new BufferedWriter(
										new OutputStreamWriter(
											new FileOutputStream(fileName), "UTF8") );
				w.close();
        
//        if (isQnx() == false)
//            return;
//        
//        Process process = Runtime.getRuntime().exec("touch " + fileName);
//        process.waitFor();
    }

    protected static String userGroup;
    
    public static void setUserGroup(String _group)
    {
      userGroup = _group;
    }
    /**
     * @return true если текущий пользователь привилегированный
     */
    public static boolean checkUser()
    {
        String[] privilegedGroups = new String[] {"root", "customers"};
        
        String groupName = "operators";
        
        groupName = userGroup;
        
        for (int i = 0; i < privilegedGroups.length; i++)
        {
            String group = privilegedGroups[i];
            if (groupName.equals(group))
            {
                return true;
            }
        }
        
        return false;
    }
}

















