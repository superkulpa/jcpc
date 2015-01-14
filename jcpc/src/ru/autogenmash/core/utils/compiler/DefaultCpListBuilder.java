package ru.autogenmash.core.utils.compiler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import ru.autogenmash.auxiliary.StringPair;
import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;

/**Билдер для обычной ISO управляющей программы. Для обычной МТР (комета).
 * @author Dymarchuk Dmitry
 * 23.03.2010 13:20:05
 */
public class DefaultCpListBuilder implements ICPListBuilder
{
    /** Количество кадров (при инициализации). */
    public static final int СAPACITY_FRAMES = 100;
    /** Количество подкадров в кадре (при инициализации). */
    public static final int СAPACITY_SUBFRAMES = 4;
    /** Количество управляющих команд в подкадре (при инициализации). */
    public static final int СAPACITY_COMMANDS = 5;

    public static final String[] COMMAND_NAMES;

    public static final String KEY_LOOPS = "Loops";
    public static final String KEY_SUBPROGRAMS = "Subprograms";
    public static final String KEY_MARKING = "Marking";
    public static final String KEY_FULLARCS = "Fullarcs";

    private Vector _frames = new Vector(СAPACITY_FRAMES, СAPACITY_FRAMES);
    private Vector _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);

    private static DefaultCpListBuilder _instance = new DefaultCpListBuilder();
    
    public void SetCpParameters(CpParameters _parameters) {};
    
    static
    {
        Field[] fields = CC.class.getFields();

        Vector tmpCommandNames = new Vector(40);
        for (int i = 0; i < fields.length; i++)
        {
            int value = 0;
            try
            {
                value = fields[i].getInt(null);
            }
            catch (Throwable t)
            {
                continue;
            }
            if (value < 100 || value > 200)
                continue;
            else
                tmpCommandNames.add(fields[i].getName());
        }

        COMMAND_NAMES = (String[])tmpCommandNames.toArray(new String[0]);

        Arrays.sort(COMMAND_NAMES);
    }


    public static DefaultCpListBuilder getInstance()
    {
        return _instance;
    }
    
    private DefaultCpListBuilder()
    {
    }

    public String getDelimiterChars()
    {
        return "NnGgXxYyIiJjFfMmTtDdHhLlRrKkUu";
    }
    
    public CompilerError build(List source, CPList cpList, List warnings)
    {
        ICpSubFrameBuilder[] frameBuilders = getFrameBuilders();

        for (int iterator = 0; iterator < source.size(); iterator++)
        {
            Vector commandLineStorage = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
            String sourceLine = ((String)source.get(iterator)).trim();
            
            CompilerError result = fillCommandLineStorage(sourceLine, commandLineStorage);
            if (result != null)
            {
                result.setFrameNumber(iterator + 1);
                return result;
            }
                
            CachedCpFrame frame = new CachedCpFrame(CpFrame.FRAME_TYPE_UNKNOWN, new CpSubFrame[] {});
            
            for (int i = 0; i < frameBuilders.length; i++)
            {
                ICpSubFrameBuilder frameBuilder = frameBuilders[i];
                CpSubFrame subFrame = new CpSubFrame(CpSubFrame.RC_NULL, new CC[] {});
                
                if (hasAcceleratorChar(commandLineStorage, frameBuilder.getAcceleratorChar()) == false)
                    continue;
                
                result = frameBuilder.build(commandLineStorage, cpList, frame, subFrame, warnings);
                if (result != null)
                {
                    result.setFrameNumber(iterator + 1);
                    return result;
                }
                
                if (subFrame.getLength() != 0)
                    _subFrames.add(subFrame);
                
                if (calculateRealSize(commandLineStorage) == 0)
                {
                    if (_subFrames.size() == 0)
                        return new CompilerError(iterator + 1, "кадр не содержит подкадров");
                    
                    frame.setData((CpSubFrame[])_subFrames.toArray(new CpSubFrame[0]));
                    _subFrames.clear();
                    break;
                }
            }
            
            if (calculateRealSize(commandLineStorage) != 0)
                return new CompilerError(iterator + 1);
            
            _frames.add(frame);
        }
        
        cpList.setData(_frames);
        
        return null;
    }

    private int calculateRealSize(List list)
    {
        int size = 0;
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) == null)
                continue;
            else
                size++;
        }
        
        return size;
    }
    
    private CompilerError fillCommandLineStorage(String sourceLine, List storage)
    {
        StringTokenizer st = new StringTokenizer(sourceLine, getDelimiterChars(), true);
        while (st.hasMoreElements())
        {
            String commandName = st.nextToken().trim().toUpperCase();
            String valueStr = null;
            
            if (Arrays.binarySearch(COMMAND_NAMES, commandName) < 0)
            {
                return new CompilerError("управляющая команда \"" + commandName + "\" не распознана");
            }
            try
            {
                valueStr = st.nextToken().trim();
            }
            catch (NoSuchElementException e)
            {
                return new CompilerError("не определено значение управляющей команды \"" + commandName + "\"");
            }
            
            storage.add(new StringPair(commandName, valueStr));
        }
        
        return null;
    }
    
    private boolean hasAcceleratorChar(List storage, Character acceleratorChar)
    {
        if (acceleratorChar == null)
            return true;
            
        Iterator iterator = storage.iterator();
        while (iterator.hasNext())
        {
            StringPair pair = (StringPair)iterator.next();
            if (pair == null)
                continue;
            
            if (pair.getValue1().equals(String.valueOf(acceleratorChar.charValue())))
                return true;
        }
        
        return false;
    }
    
    public ICpSubFrameBuilder[] getFrameBuilders()
    {
        return new ICpSubFrameBuilder[] {
                DefaultMoveSubFrameBuilder.getInstance(),
                DefaultNumberSubFrameBuilder.getInstance(),
                DefaultCutOnOffSubFrameBuilder.getInstance()
        };
    }

}



