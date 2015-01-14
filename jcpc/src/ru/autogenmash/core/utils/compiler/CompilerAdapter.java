package ru.autogenmash.core.utils.compiler;

//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Properties;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import ru.autogenmash.core.ISubscribable;
//import ru.autogenmash.core.net.Message;
//import ru.autogenmash.core.net.SimpleMessage;
//import ru.autogenmash.core.net.UDPListeningService;
//import ru.autogenmash.core.net.UDPSendingService;

/**
 * @author Dymarchuk Dmitry
 * 14.03.2008 10:21:18
 */
public class CompilerAdapter //implements ISubscribable
{}
//{
//    public static final int COMPILE_SUCCESSED = 0;
//    public static final int COMPILE_BREAKED = 1;
//    public static final int COMPILE_TIMEOUTED = 2;
//
//    private static Log _log = LogFactory.getLog(CompilerAdapter.class);
//
//    //private TCPClient _tcpClient;
//
//    private UDPListeningService _listeningService;
//    private UDPSendingService _sendingService;
//
//    private boolean _cpCompiled = false;
//    private boolean _cpBreaked = false;
//
//    /** Время ожидания выполнения компиляции, ротации, масштабирования и др.
//     * в секундах. */
//    private int _timeout = 30;
//
//    private void init()
//    {
//        InputStream iniPropertiesFile =
//            ClassLoader.getSystemResourceAsStream("kernel.properties");
//        Properties properties = new Properties();
//        if (iniPropertiesFile != null)
//        {
//            try
//            {
//                properties.load(iniPropertiesFile);
//                iniPropertiesFile.close();
//                _timeout = Integer.parseInt(properties.getProperty("compiler.timeout", "60"));
//            }
//            catch (Throwable e)
//            {
//                _log.warn("Параметры компиляции инициализированы по умолчанию", e);
//            }
//        }
//    }
//
////    public CompilerAdapter(TCPClient tcpClient)
////    {
////        init();
////        _tcpClient = tcpClient;
////        _tcpClient.subscribe(this);
////    }
//    public CompilerAdapter(UDPListeningService listeningService, UDPSendingService sendingService)
//    {
//        init();
//        _listeningService = listeningService;
//        _sendingService = sendingService;
//        _listeningService.subscribe(this);
//    }
//
//    public int compile(String cpFileName, String cpName,
//            int scale, int feed, int kerf)
//    throws InterruptedException, IOException
//    {
//        String message = cpFileName + " " + cpName + " " +
//            scale + " " + feed + " " + kerf;
//        return doAction(Message.MSG_SECTION_CP, Message.MSG_CMD_COMPILE, message);
//    }
//
//    public int rotate(int angle, boolean inverse)
//    throws InterruptedException, IOException
//    {
//        String message = angle + " " + (inverse == true ? 1 : 0);
//        return doAction(Message.MSG_SECTION_CP, Message.MSG_CMD_ROTATE, message);
//    }
//
//    private volatile boolean _isWaiting = false;
//
//    private synchronized int doAction(String section, String cmd, String message)
//    throws InterruptedException, IOException
//    {
////        _tcpClient.sendMessage(section, cmd, message);
//
//        String msg = section + SimpleMessage.DELIMITER + cmd +
//        SimpleMessage.DELIMITER + message + Message.DELIMITER;
//        _sendingService.sendMessage(msg);
//        //System.out.println("***** doAction() cmd = " + cmd);
//
//
//        _isWaiting = true;
//        wait(_timeout * 1000);
//        _isWaiting = false;
//
//        return checkResult();
//    }
//
//    private int checkResult()
//    {
//        if (_cpCompiled == false)
//            return COMPILE_TIMEOUTED;
//
//        if (_cpBreaked == true)
//        {
//            _cpBreaked = false;
//            return COMPILE_BREAKED;
//        }
//
//        _cpCompiled = false;
//
//        return COMPILE_SUCCESSED;
//    }
//
//    public synchronized void onReceive(Message message)
//    {
//        //System.out.println("***** raw " + message.toString());
//        if (message.getLength() != 1)
//            return;
//
//        SimpleMessage simpleMessage = message.getSimpleMessage(0);
//        String section = simpleMessage.getSection();
//        if (section.equalsIgnoreCase(Message.MSG_SECTION_CP))
//        {
//            if (simpleMessage.getParameter().equals(Message.MSG_STATE_CP_COMPILED))
//            {
//                _cpCompiled = true;
//                //System.out.println("***** compiled" );
//                if (_isWaiting == false)
//                    System.err.println("CA is no waiting but command received");
//                else
//                    notifyAll();
//            }
//            else if (simpleMessage.getParameter().equals(Message.MSG_STATE_CP_BREAKED))
//            {
//                _cpBreaked = true;
//                if (_isWaiting == false)
//                    System.err.println("CA is no waiting but command received");
//                else
//                    notifyAll();
//            }
//        }
//    }
//}
