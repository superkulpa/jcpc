package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 20.09.2007 9:17:35
 */
public class AddCometFiltersAction extends StepActionBase
{
    public AddCometFiltersAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public static final String FILTER_LINE =
        MTRUtils.getAxisIndexStr("X") + "=LINE_X();" +
        MTRUtils.getAxisIndexStr("Y") + "=LINE_Y();" +
        MTRUtils.getAxisIndexStr("Z") + "=LINE_Z()";

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
//        CC cc = new CC(CC.FILTER, 0, FILTER_LINE);
//        CpSubFrame subFrame = new CpSubFrame(CpSubFrame.RC_FILTER, new CC[] {cc});
//        CachedCpFrame frame = new CachedCpFrame(CpFrame.FRAME_TYPE_UNKNOWN, new CpSubFrame[] {subFrame});
//        CachedCpFrame[] data = new CachedCpFrame[cpList.getLength() + 1];
//        data[0] = frame;
//        for (int i = 0; i < cpList.getLength(); i++)
//        {
//            data[i + 1] = cpList.getFrame(i);
//        }
//        cpList.setData(data);
//        return null;
        return new CompilerError("not implemented");
    }

    public String getDescription()
    {
        return "Фильтрование";
    }
}
