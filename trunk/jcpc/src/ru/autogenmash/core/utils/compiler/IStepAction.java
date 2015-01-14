package ru.autogenmash.core.utils.compiler;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 23.07.2007 9:53:14
 */
public interface IStepAction
{
    /**Выполнить шаг компиляции.
     * @param cpList УП.
     * @param cpParameters параметры УП.
     * @return null в случае успеха, иначе ошибку.
     */
    public CompilerError execute(final CPList cpList, final CpParameters cpParameters);

    public String getDescription();
}
