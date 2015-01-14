package ru.autogenmash.core.utils.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.StringUtils;

/**Параметры трубореза.
 * @author Dymarchuk Dmitry
 * @version
 * 06.07.2007 10:12:11
 */
public class PipeCutterParamsParser implements ICpParamsParser
{
    public static final String CP_PARAMETER_D_PIPE_NAME = "D";
    public static final String CP_PARAMETER_D_HOLE_NAME = "d";
    public static final String CP_PARAMETER_ALPHA = "Alpha";
    public static final String CP_PARAMETER_DY = "dY";
    public static final String CP_PARAMETER_THICKNESS = "thickness";
    public static final String CP_PARAMETER_LV_NAME = "VILET";
    public static final String CP_PARAMETER_LZ_NAME = "ZAZOR";
    public static final String CP_PARAMETER_TABLEF_FILE_NAME = "table";

    /** Параметры УП, специфичные для трубореза. */
    public final HashMap _paramsNames;

    /** Singleton. */
    protected static PipeCutterParamsParser _instance;

    static
    {
        _instance = new PipeCutterParamsParser();
    }

    protected PipeCutterParamsParser()
    {
        _paramsNames = new HashMap();
        _paramsNames.put("D", "Радиус трубы");
        _paramsNames.put("d", "Радиус отверстия");
        _paramsNames.put("Alpha", "Наклон 'вставляемой' трубы относительно вертикали");
        _paramsNames.put("dY", "Смещение 'вставляемой' трубы");
        _paramsNames.put("thickness", "Толщина листа");
        _paramsNames.put("VILET", "Вылет");
        _paramsNames.put("ZAZOR", "Зазор");
        _paramsNames.put("table", "Таблица корректировки скорости");
    }

    public static PipeCutterParamsParser getInstance()
    {
        return _instance;
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.ICpParamsParser#parse(java.io.FileInputStream)
     */
    public CpParameters parse(String paramsFileName) throws IOException
    {
        if (paramsFileName == null)
            return new CpParameters();

        File file = new File(paramsFileName);
        if (file.exists() == false)
            return new CpParameters();

        FileInputStream paramsFile = new FileInputStream(paramsFileName);

        CpParameters cpParameters = new CpParameters();

        boolean inParameterSection = false;

        String parameteredLine = "";

        int c;

        while((c = paramsFile.read()) != -1)
        {
            switch ((char)c)
            {
            case '(':
                inParameterSection = true;
                break;
            case ')':
                StringTokenizer st = new StringTokenizer(parameteredLine.trim(), " \n\r");

                while (st.hasMoreTokens())
                {
                    String token = st.nextToken();

                    for (int i = 0; i < getParamsNames().length; i++)
                    {
                        Object value = readParameterFromToken(token, getParamsNames()[i], '=');
                        if ( value != null)
                        {
                            if (cpParameters.add(
                                    getParamsNames()[i],
                                    (value instanceof Number == true ? new Long(((Number)value).longValue() * 100) : value),
                                    (String)_paramsNames.get(getParamsNames()[i])) == false)
                                return null;
                        }
                    }
                }

                inParameterSection = false;
                parameteredLine = "";
                break;
            default:
                if (inParameterSection == true)
                    parameteredLine += (char)c;
            }
        }

        paramsFile.close();

        //return cpParameters.getLength() == 0 ? null : cpParameters;
        return cpParameters;
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.ICpParamsParser#getParamsNames()
     */
    public String[] getParamsNames()
    {
        Object[] values = _paramsNames.keySet().toArray();
        String[] res = new String[values.length];
        for (int i = 0; i < values.length; i++)
        {
            res[i] = (String)values[i];
        }
        //return (String[])_paramsNames.keySet().toArray();
        return res;
    }

    /**Прочитать параметр с именем <b>paramName</b> из строки <b>sourceString</b>.
     * @param token
     * @param paramName
     * @param delimiter
     * @return значение параметра, null если такого параметра в строке нет.
     */
    public static Object readParameterFromToken(String token, String paramName, char delimiter)
    {
        Object data = null;
        if (token.startsWith(paramName))
        {
            String[] subTokens = StringUtils.split(token, String.valueOf(delimiter));
            if ( (subTokens.length == 2) && (subTokens[0].trim().length() == paramName.length()) )
                data = subTokens[1]./*replace('m', ' ').replace('м', ' ').*/trim();
        }

        return data;
    }

}












