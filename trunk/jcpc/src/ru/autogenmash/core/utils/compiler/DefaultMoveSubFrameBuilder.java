package ru.autogenmash.core.utils.compiler;

import java.util.Arrays;
import java.util.List;

import ru.autogenmash.auxiliary.StringPair;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.MTRUtils;

/**
 * @author Dymarchuk Dmitry
 * 23.03.2010 15:53:50
 */
public class DefaultMoveSubFrameBuilder implements ICpSubFrameBuilder
{

    /** Максимальное перемещение по X, в десятых долях мм. */
    public static final int MAX_X_VALUE = 10 * 1000 * 10;
    /** Максимальное перемещение по Y, в десятых долях мм. */
    public static final int MAX_Y_VALUE = 5 * 1000 * 10;
    
    public static DefaultMoveSubFrameBuilder _instance = new DefaultMoveSubFrameBuilder();
    
    public static DefaultMoveSubFrameBuilder getInstance()
    {
        return _instance;
    }
    
    private DefaultMoveSubFrameBuilder()
    {
    }

    public Character getAcceleratorChar()
    {
        // для данного подкадра может не быть "обязательного" символа
        return null;
    }
    
    public CompilerError build(List source, CPList cpList, CachedCpFrame frame, CpSubFrame subFrame, List warnings)
    {
        int g00_03Count = 0;
        int gData = -1;
        int x = 0, y = 0, i = 0, j = 0;
        
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
            
            String outOfRangeError = "значение управляющей команды \"" + 
                paramName + "\" находиться за допустимыми пределами";
            
            if (paramName.equals("X"))
            {
                x = value;
                source.set(p, null);
                if (x > MAX_X_VALUE)
                    return new CompilerError(outOfRangeError);
            }
            else if (paramName.equals("Y"))
            {
                y = value;
                source.set(p, null);
                if (y > MAX_Y_VALUE)
                    return new CompilerError(outOfRangeError);
            }
            else if (paramName.equals("I"))
            {
                i = value;
                source.set(p, null);
            }
            else if (paramName.equals("J"))
            {
                j = value;
                source.set(p, null);
            }
            else if (paramName.equals("G"))
            {
                if (Arrays.binarySearch(Compiler.CORRECT_G_COMMANDS, value) < 0)
                    return new CompilerError("\"G" + value + "\" недопустимая команда");
                
                if (value <= 3 && value >= 0)
                {
                    if (g00_03Count != 0)
                        return new CompilerError("слишком много подготовительных команд (G00 - G03)");
                    gData = value;
                    source.set(p, null);
                    g00_03Count++;
                }
            }
        }
        String wrongGCommand = "некорректно определена подготовительная команда";
        
        if (x == 0 && y == 0 && i == 0 && j == 0)
        {
            if (gData != -1)
                return new CompilerError(wrongGCommand);
            
            return null;
        }
        
        if (i == 0 && j == 0)
        {
            if (gData > 1)
                return new CompilerError(wrongGCommand);
            
            if (gData < 0)
            {
                subFrame.copy(MTRUtils.createLineSubFrame(1, x, y));
                subFrame.setType(CpSubFrame.RC_NULL);
            }
            else
            {
                subFrame.copy(MTRUtils.createLineSubFrame(gData, x, y));
                frame.setHasG00(gData == 0);
                frame.setHasG01(gData == 1);
            }
            
            frame.setType(CpFrame.FRAME_TYPE_LINE);
            frame.setHasX(x != 0);
            frame.setHasY(y != 0);
        }
        else
        {
            if (gData < 0)
            {
                subFrame.copy(MTRUtils.createArcSubFrame(2, x, y, i, j));
                subFrame.setType(CpSubFrame.RC_NULL);
            }
            else if (gData > 1)
            {
                subFrame.copy(MTRUtils.createArcSubFrame(gData, x, y, i, j, true));
                frame.setHasG02(gData == 2);
                frame.setHasG03(gData == 3);
            }
            else
                return new CompilerError(wrongGCommand);
            
            frame.setType(CpFrame.FRAME_TYPE_ARC);
            frame.setHasX(x != 0);
            frame.setHasY(y != 0);
            frame.setHasX(i != 0);
            frame.setHasY(j != 0);
        }
        
        return null;
    }

}












