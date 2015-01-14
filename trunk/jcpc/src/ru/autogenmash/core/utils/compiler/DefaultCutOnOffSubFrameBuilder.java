package ru.autogenmash.core.utils.compiler;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import ru.autogenmash.auxiliary.StringPair;
import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpSubFrame;

/**
 * @author Dymarchuk Dmitry
 * 24.03.2010 18:56:31
 */
public class DefaultCutOnOffSubFrameBuilder implements ICpSubFrameBuilder
{

    public static DefaultCutOnOffSubFrameBuilder _instance = new DefaultCutOnOffSubFrameBuilder();
    
    public static DefaultCutOnOffSubFrameBuilder getInstance()
    {
        return _instance;
    }
    
    private DefaultCutOnOffSubFrameBuilder()
    {
    }

    public Character getAcceleratorChar()
    {
        return new Character('M');
    }
    
    public CompilerError build(List source, CPList cpList, CachedCpFrame frame, CpSubFrame subFrame, List warnings)
    {
        int m71_85Count = 0;
        int m71_85Data = -1;
        Vector tData = new Vector(2);
        
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
            
            if (paramName.equals("M"))
            {
                if (Arrays.binarySearch(Compiler.CORRECT_M_COMMANDS, value) < 0)
                    return new CompilerError("\"M" + value + "\" недопустимая команда");
                if (value <= 85 && value >= 71)
                {
                    if (m71_85Count != 0)
                        return new CompilerError("слишком много команд включения/выключения реза (M71 - M83)");
                    m71_85Data = value;
                    m71_85Count++;
                    source.set(p, null);
                }
            }
            else if (paramName.equals("T"))
            {
                tData.add(new Integer(value));
                source.set(p, null);
            }
        }
        
        if (m71_85Data > 0)
        {
            CC[] commands = new CC[tData.size() + 1];
            
            commands[0] = new CC(CC.M, m71_85Data, "M");
            if (tData.size() > 0 )
                for (int it = 0; it < tData.size(); it++)
                    commands[it + 1] = new CC(CC.T, ((Integer)tData.get(it)).intValue(), "T");
            
            subFrame.setData(commands);
            frame.setHasM(true);
        }
        
        return null;
    }

}










