package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;
import ru.autogenmash.core.utils.compiler.PipeCutterParamsParser;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:01:04
 */
public class FeedCorrectingAction extends StepActionBase
{

    protected int _lv;
    protected int _lz;
    protected int _thickness;
    protected String _tableFileName;

    public FeedCorrectingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.IStepAction#execute(ru.autogenmash.core.CPList, ru.autogenmash.core.CpParameters)
     */
    //Вставить коррекцию подачи на радиусах при резе с наклоненным резаком.
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
//        int feedCoreectionPercentage = 100;
//        int uAbs = 0;
//
//        String result = readParameters(cpParameters);
//        if (result != null)
//            return new CompilerError(result);
//
//        try
//        {
//            // покадровая подготовительная обработка CPList
//            for (int p = 0; p < cpList.getLength(); p++)
//            {
//                CpFrame frame = cpList.getFrame(p);
//
//                Integer U = frame.getDataByType(CC.U);
//                uAbs += (U == null ? 0 : U.intValue());
//
//                if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
//                {
//                    if (uAbs != 0)
//                    {
//                        Integer I = frame.getDataByType(CC.I);
//                        Integer J = frame.getDataByType(CC.J);
//                        int i = (I == null ? 0 : frame.getDataByType(CC.I).intValue());
//                        int j = (J == null ? 0 : frame.getDataByType(CC.J).intValue());
//                        double rArc = Math.sqrt(i*i + j*j);
//                        double k1 = 1 + MathUtils.sin(uAbs)*(_lv + _lz) / rArc;
//                        Object k2Raw = TableReader.read(_tableFileName).getValue(String.valueOf(uAbs), String.valueOf(_thickness));
//                        if (k2Raw == null)
//                            return new CompilerError("В таблице '" + _tableFileName +
//                                    "' отсутствуют поля '" + String.valueOf(uAbs) + "' и/или '" +
//                                    String.valueOf(_thickness) + "'");
//                        double k2;
//                        try
//                        {
//                            k2 = Double.parseDouble((String)k2Raw);
//                        }
//                        catch (NumberFormatException e)
//                        {
//                            e.printStackTrace();
//                            return new CompilerError("В таблице '" + _tableFileName +
//                                  "' у полей '" + String.valueOf(uAbs) + "' и/или '" +
//                                  String.valueOf(_thickness) + "' некорректно задано значение");
//
//                        }
//                        feedCoreectionPercentage = (int)Math.round(100 * (1 + Math.abs(k1 * k2)));
//                        if (feedCoreectionPercentage != 100)
//                        {
//                            frame.push(new CpSubFrame(CpSubFrame.RC_FEED_CORRECTION, new CC[] {new CC(CC.FCORRECTION,
//                                    feedCoreectionPercentage, "FCORR_ON")} ));
//                            frame.pushBack(new CpSubFrame(CpSubFrame.RC_FEED_CORRECTION, new CC[] {new CC(CC.FCORRECTION,
//                                    Math.round(100 * 100 / feedCoreectionPercentage), "FCORR_OFF")} ));
//                        }
//                    }
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//          if ( (e instanceof ParameterMissException) ||
//                  (e instanceof XmlFileIsNotSingleTableException) ||
//                  (e instanceof WrongColumnNameException) ||
//                  (e instanceof WrongColumnsCountException) )
//              return new CompilerError(e.getMessage());
//          else if (e instanceof IOException)
//              return new CompilerError("Ошибка при чтении файла таблицы '" + _tableFileName + "'");
//        }
//
//        return null;

        return new CompilerError("not implemented");
    }

    protected String readParameters(CpParameters cpParameters)
    {
        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_LV_NAME))
            _lv = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_LV_NAME));
        else
            return new String("Параметр " + PipeCutterParamsParser.getInstance().
                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_LV_NAME) + " отсутствует.");

        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME))
            _lz = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME));
        else
            return new String("Параметр " + PipeCutterParamsParser.getInstance().
                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME) + " отсутствует.");

        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_THICKNESS))
            _thickness = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_THICKNESS));
        else
            return new String("Параметр " + PipeCutterParamsParser.getInstance().
                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_THICKNESS) + " отсутствует.");

        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_TABLEF_FILE_NAME))
            _tableFileName = (String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_TABLEF_FILE_NAME);
        else
            return new String("Параметр " + PipeCutterParamsParser.getInstance().
                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_TABLEF_FILE_NAME) + " отсутствует.");

        return null;
    }

    public String getDescription()
    {
        return "Корректировка скорости";
    }

}
