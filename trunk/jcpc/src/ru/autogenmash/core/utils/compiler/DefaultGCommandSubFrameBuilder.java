package ru.autogenmash.core.utils.compiler;

import java.util.Arrays;
import java.util.List;

import ru.autogenmash.auxiliary.StringPair;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpSubFrame;

/**
 * @author Dymarchuk Dmitry
 * 25.03.2010 11:40:54
 */
public class DefaultGCommandSubFrameBuilder implements ICpSubFrameBuilder
{

    public static DefaultGCommandSubFrameBuilder _instance = new DefaultGCommandSubFrameBuilder();
    
    public static DefaultGCommandSubFrameBuilder getInstance()
    {
        return _instance;
    }
    
    private DefaultGCommandSubFrameBuilder()
    {
    }

    public Character getAcceleratorChar()
    {
        return new Character('G');
    }
    
    public CompilerError build(List source, CPList cpList, CachedCpFrame frame, CpSubFrame subFrame, List warnings)
    {
        // TODO доделать
        for (int p = 0; p < source.size(); p++)
        {
            StringPair pair = (StringPair)source.get(p);
            if (pair == null)
                continue;

            String paramName = pair.getValue1();
            String valueStr = pair.getValue2();
            int value;
            try
            {
                value = Integer.parseInt(valueStr);
            }
            catch (NumberFormatException e)
            {
                return new CompilerError("некорректно определено значение " +
                        "управляющей команды \"" + paramName + "\"");
            }
            
            if (paramName.equals("G"))
            {
                if (Arrays.binarySearch(Compiler.CORRECT_G_COMMANDS, value) < 0)
                    return new CompilerError("\"G" + value + "\" недопустимая команда");

                source.set(p, null);
            }
        }
        
        return null;
    }

}










