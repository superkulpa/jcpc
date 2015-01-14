package ru.autogenmash.core.utils.compiler;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpSubFrame;

/**
 * @author Dymarchuk Dmitry
 * 23.03.2010 13:41:57
 */
public interface ICpSubFrameBuilder
{
    
    public CompilerError build(List source, CPList cpList, CachedCpFrame frame, 
            CpSubFrame subFrame, List warnings);
    
    /**Только верхний регистр
     * @return символы команд
     */
    public Character getAcceleratorChar();
}
