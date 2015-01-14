package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:00:17
 */
public class ErrorCheckingAction extends StepActionBase
{

    public ErrorCheckingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);

            String res = checkFrameForError(frame);
            if (res != null)
                return new CompilerError(i + 1, res);
        }

        return null;
    }

    /**Проверить кадр <b>frame</b> на наличие ошибок.
     * @param frame
     * @return
     */
    protected String checkFrameForError(final CachedCpFrame frame)
    {
        int frameType = frame.getType();

        // проверка на дублирование команд
        String[] uniqCommands = Compiler.UNIQ_COMMANDS;
        for (int i = 0; i < uniqCommands.length; i++)
        {
            int commandType = ((Integer)CC.getPublicFieldsMap().get(uniqCommands[i])).intValue();

            if (frame.getCommandCount(commandType) > 1)
                return "команда '" + uniqCommands[i] + "' уже представлена в кадре";
        }

        if (frameType == CpFrame.FRAME_TYPE_LINE)
        {
            if ((frame.hasG00() | frame.hasG01()) == false)
                return "неопределен режим отработки линейного перемещения";
//            if ((frame.hasG02() | frame.hasG03()) == true)
//                return "неверно определен режим отработки линейного перемещения";
        }

        if (frameType == CpFrame.FRAME_TYPE_ARC)
        {
            if ((frame.hasG02() | frame.hasG03()) == false)
                return "неопределено направление обхода дуги";
//            if ((frame.hasG00() | frame.hasG01()) == true)
//                return "неверно определен режим отработки дугового перемещения";

            String result = MTRUtils.correctFrameArcCenter(frame);
            if (result != null)
            {
                _log.error(result);
                return result;
            }
        }

        return null;
    }

    public String getDescription()
    {
        return "Проверка на ошибки";
    }

}













