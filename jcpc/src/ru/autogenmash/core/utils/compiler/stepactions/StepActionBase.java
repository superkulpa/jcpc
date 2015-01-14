package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;
import ru.autogenmash.core.utils.compiler.IStepAction;

/**
 * @author Dymarchuk Dmitry
 * 24.02.2009 10:07:57
 */
public abstract class StepActionBase implements IStepAction
{
    protected Log _log;

    protected List _warnings;

    private Compiler _compiler;

    public StepActionBase(Compiler compiler, List warnings)
    {
        _log = LogFactory.getLog(this.getClass());
        _compiler = compiler;
        _warnings = warnings;
    }

    public abstract CompilerError execute(CPList cpList, CpParameters cpParameters);

    public abstract String getDescription();

    protected Compiler getCompiler()
    {
        return _compiler;
    }
}
