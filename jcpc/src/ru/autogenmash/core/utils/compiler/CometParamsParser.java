package ru.autogenmash.core.utils.compiler;

import ru.autogenmash.core.CpParameters;

/**Параметры кометы, метеора и т.п..
 * @author Dymarchuk Dmitry
 * @version
 * 06.07.2007 12:59:17
 */
public class CometParamsParser implements ICpParamsParser
{
    /** Параметры УП, специфичные для кометы, метеора и т.п.. */
    protected final String[] _paramsNames = { };

    /** Singleton. */
    protected static CometParamsParser _instance;

    static
    {
        _instance = new CometParamsParser();
    }

    protected CometParamsParser()
    {

    }

    public static CometParamsParser getInstance()
    {
        return _instance;
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.ICpParamsParser#parse(java.io.FileInputStream)
     */
    public CpParameters parse(String paramsFileName)
    {
        return new CpParameters();
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.ICpParamsParser#getParamsNames()
     */
    public String[] getParamsNames()
    {
        return _paramsNames;
    }

}












