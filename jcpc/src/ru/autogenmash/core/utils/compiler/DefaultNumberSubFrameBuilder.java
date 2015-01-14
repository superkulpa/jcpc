/*$Id: DefaultNumberSubFrameBuilder.java,v 1.1 2010/04/27 10:40:43 Dymarchyk Exp $*/
package ru.autogenmash.core.utils.compiler;

import java.util.List;

import ru.autogenmash.auxiliary.StringPair;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpSubFrame;

/**
 * @author Dymarchuk Dmitry
 * 24.03.2010 16:49:31
 */
public class DefaultNumberSubFrameBuilder implements ICpSubFrameBuilder
{

    /** Максимальное количество кадров в уп. */
    public static final int MAX_N_VALUE = 10 * 1000;
    
    public static DefaultNumberSubFrameBuilder _instance = new DefaultNumberSubFrameBuilder();
    
    public static DefaultNumberSubFrameBuilder getInstance()
    {
        return _instance;
    }
    
    private DefaultNumberSubFrameBuilder()
    {
    }

    public Character getAcceleratorChar()
    {
        return new Character('N');
    }
    
    public CompilerError build(List source, CPList cpList, CachedCpFrame frame, CpSubFrame subFrame, List warnings)
    {
        for (int p = 0; p < source.size(); p++)
        {
            StringPair pair = (StringPair)source.get(p);
            String paramName = pair.getValue1();
            if (paramName == null)
                continue;

            else if (paramName.equals("N"))
            {
                source.set(p, null);
                return null;
            }
        }
        
        return null;
    }

}













