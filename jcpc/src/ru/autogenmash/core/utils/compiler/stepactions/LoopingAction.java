package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**Action обрабатывает циклы, путем копирования заданное число раз.
 * @author Dymarchuk Dmitry
 * 25.08.2008 13:54:53
 */
public class LoopingAction extends StepActionBase
{
    public static final String AUX_DATA_LOOPS = "Loops";
    
    public LoopingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        try
        {
            Boolean hasLoops = (Boolean)cpList.getAuxData(AUX_DATA_LOOPS);
            if (hasLoops != null && hasLoops.booleanValue() == false)
                return null;
        }
        catch (Throwable t)
        {
            final String err = "Ошибка при получении вспомогательной информации";
            _log.error(err, t);
            System.err.println("Warning: " + LoopingAction.class.getName() + " has some problems (for detail see logs)");
            return new CompilerError(err);
        }

        try
        {
            int hComandCount = 0;
            Vector newFrames = new Vector(100, 100);
            Vector cpListFrames = new Vector(100, 100);
            MTRUtils.addFrames(cpListFrames, cpList.getData());

            do
            {
                int loopCount = 0;
                hComandCount = 0;
                boolean inLoop = false;
                Vector loopFrames = new Vector();

                for (int i = 0; i < cpListFrames.size(); i++)
                {
                    CachedCpFrame frame = (CachedCpFrame)cpListFrames.get(i);
                    if (frame.getDataByType(CC.H) != null)
                    {
                        hComandCount++;
                        if (loopFrames.size() > 0)
                        {
                            CollectionUtils.addAll(newFrames, ((Vector)loopFrames.clone()).iterator());
                            loopFrames.clear();
                        }
                        inLoop = true;
                        loopCount = Utils.toInt(frame.getDataByType(CC.H));
//                        if (frame.getLength() > 1)
//                        {
//                            frame.removeSubFrameByType(CpSubFrame.RC_LOOP_COMMAND);
//                            newFrames.add(frame.clone());
//                        }
                        newFrames.add((CachedCpFrame)frame.clone());
                    }
                    else if ( (inLoop && frame.hasM() && frame.contains(CC.M, 20)) ||
                              (inLoop && i == cpListFrames.size() - 1))
                    {
                        if (frame.contains(CC.M, 20) == false)
                            loopFrames.add((CachedCpFrame)frame.clone());
                        
                        hComandCount--;
                        
                        CachedCpFrame lastFrame = (CachedCpFrame)newFrames.get(newFrames.size() - 1);
                        if (lastFrame.getSubFrameByType(CpSubFrame.RC_LOOP_COMMAND) != null)
                        {
                            if (lastFrame.getLength() == 1)
                                newFrames.remove(newFrames.size() - 1);
                            else
                                lastFrame.removeSubFrameByType(CpSubFrame.RC_LOOP_COMMAND);
                        }
                        
                        if (hComandCount < 0)
                            return new CompilerError("Команда \"M20\" не может быть использована без команды \"H\"");
                        for (int p = 0; p < loopCount; p++)
                        {
                            Iterator loopIterator = loopFrames.iterator();
                            while (loopIterator.hasNext())
                            {
                                CachedCpFrame loopFrame = (CachedCpFrame)loopIterator.next();
                                newFrames.add((CachedCpFrame)loopFrame.clone());
                            }
                        }
                        inLoop = false;
                        loopCount = 0;
                        loopFrames.clear();
                    }
                    else
                    {
                        if (inLoop)
                            loopFrames.add((CachedCpFrame)frame.clone());
                        else
                            newFrames.add((CachedCpFrame)frame.clone());
                    }
                }
                cpListFrames = (Vector)newFrames.clone();
                newFrames.clear();
            }
            while (hComandCount > 0);

            if (hComandCount != 0)
                return new CompilerError("Количество \"H\" команд не соответствует количеству \"M20\"");

            cpList.setData(cpListFrames);

            return null;
        }
        catch (Throwable t)
        {
            final String err = "Ошибка при обработке циклов";
            _log.error(err, t);
            System.err.println("Warning: " + LoopingAction.class.getName() + " has some problems (for detail see logs)");
            return new CompilerError(err);
        }
    }

    public String getDescription()
    {
        return "Обработка циклов";
    }

}
