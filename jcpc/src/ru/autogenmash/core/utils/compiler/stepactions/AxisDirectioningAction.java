package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:00:41
 */
public class AxisDirectioningAction extends StepActionBase
{

    public AxisDirectioningAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.IStepAction#execute(ru.autogenmash.core.CPList, ru.autogenmash.core.CpParameters)
     */
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        return new CompilerError("not implemented");
    }

    public String getDescription()
    {
        return "Настройка направления осей";
    }

}
