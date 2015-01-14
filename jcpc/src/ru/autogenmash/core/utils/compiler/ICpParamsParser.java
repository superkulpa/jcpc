package ru.autogenmash.core.utils.compiler;

import java.io.IOException;

import ru.autogenmash.core.CpParameters;

/**Интарфейс, определяющий парсер параметров УП.
 * @author Dymarchuk Dmitry
 * @version
 * 06.07.2007 9:25:18
 * <p>refactor 20.07.2007
 */
public interface ICpParamsParser
{

    /**Разобрать входной поток на наличие параметров.
     * Реализовать специфические особенности в дочерних классах.
     * @param paramsFile
     * @return
     */
    public abstract CpParameters parse(String paramsFileName) throws IOException;

    /**Получить имена параметров, спецефичных для конкретного типа УП
     * (например для трубореза, кометы, и др.).
     * @return
     */
    public abstract String[] getParamsNames();
}
