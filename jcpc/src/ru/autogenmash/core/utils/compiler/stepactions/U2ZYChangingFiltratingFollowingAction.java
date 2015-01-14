package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;


/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:02:00
 */
public class U2ZYChangingFiltratingFollowingAction extends StepActionBase
{

    public U2ZYChangingFiltratingFollowingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        return new CompilerError("not implemented");
    }

    public String getDescription()
    {
        return "Фильтрование, слежение, преобразование по осям";
    }

//    public static final int FILTER_NULL = 0;
//    public static final int FILTER_INIT = 1;
//    public static final int FILTER_W = 2;
//    public static final int FILTER_U = 3;
//    public static final int FILTER_C = 4;
//    public static final int FILTER_PARK = 5;
//
//    protected int _lv;
//    protected int _lz;
//    protected int _R;
//
//    protected String _filterUString;
//    protected String _filterWString;
//
//
//    protected String init(CpParameters cpParameters)
//    {
//        String _result = readParameters(cpParameters);
//        if (_result != null)
//            return _result;
//
//        _filterUString = MTRUtils.getAxisIndexStr("X") + "=LINE_X;" +
//        MTRUtils.getAxisIndexStr("Y") + "=LINE_Y;" +
//        MTRUtils.getAxisIndexStr("Z") + "=LINE_Z;" +
//        MTRUtils.getAxisIndexStr("U") + "=ARCSIN_Y(L=" + (_lv + _lz) + ")";
//
//        _filterWString = MTRUtils.getAxisIndexStr("X") + "=LINE_X;" +
//        MTRUtils.getAxisIndexStr("Z") + "=LINE_Z;" +
//        MTRUtils.getAxisIndexStr("W") + "=ARC_Y(R=" + _R + ")";
//
//        return null;
//    }
//
//    /* (non-Javadoc)
//     * @see ru.autogenmash.core.utils.compiler.IStepAction#execute(ru.autogenmash.core.CPList, ru.autogenmash.core.CpParameters)
//     */
//    public String execute(CPList cpList, CpParameters cpParameters)
//    {
//        int currentFilter = FILTER_NULL;
//        Integer cValue = null;
//        Integer gFollowingValue = null;
//
//        String _result = init(cpParameters);
//        if (_result != null)
//            return _result;
//
//        // инициализируем фильтра
//        if (_rawCPList.getLength() > 0)
//        {
//            _outputCPList.pushBack(new CC(CC.FILTER, 3, FILTER_INIT_STRING));
//            _outputCPList.pushBack(new CC(CC.FRAME, 0, "ENDL"));
//            currentFilter = FILTER_INIT;
//        }
//
//        Long X = null;
//
//        for(int i = 0; i < _rawCPList.getLength(); i++)
//        {
//            switch (_rawCPList.getCCType(i))
//            {
//            case CC.U:
//                int U = (int)_rawCPList.getData(i);
//                if (currentFilter != FILTER_U)
//                {
//                    _outputCPList.pushBack(new CC(CC.FILTER, 1, addFollowingToFilter(FILTER_U_STRING, gFollowingValue, cValue)));
//                    currentFilter = FILTER_U;
//                }
//                _outputCPList.pushBack(new CC(CC.G, 1, "G01"));
//                _outputCPList.pushBack(new CC(CC.Y, Math.round((Lv + Lz)*MathUtils.sin(U)), "it's U"));
//                _outputCPList.pushBack(new CC(CC.Z, Math.round((Lv + Lz)*(1 - MathUtils.cos(U))), "it's U"));
//                if (X != null)
//                {
//                    _outputCPList.pushBack(new CC(CC.X, X.intValue(), "X"));
//                    X = null;
//                }
//                break;
//            case CC.X:
//            case CC.Y:
//            case CC.Z:
//                // если встречается кадр, в котором есть "чистые" XYZ перемещения, то включить Line фильтры
//                if ( (_rawCPList.getCurrentFrame(i).hasCommand(CC.W) == false)
//                        && (_rawCPList.getCurrentFrame(i).hasCommand(CC.U) == false) )
//                    if (currentFilter != FILTER_INIT)
//                    {
//                        _outputCPList.pushBack(new CC(CC.FILTER, 7, addFollowingToFilter(FILTER_INIT_STRING, gFollowingValue, cValue)));
//                        currentFilter = FILTER_INIT;
//                    }
//
//                if (_rawCPList.getCCType(i) == CC.X)
//                {
//                    X = new Long(_rawCPList.getData(i));
//                    break;
//                }
//
//                // если в кадре есть U команда, то игнорировать Y и Z команды
//                if (_rawCPList.getCurrentFrame(i).hasCommand(CC.U) == false)
//                    _outputCPList.pushBack(_rawCPList.getFrame(i));
//                break;
//            case CC.W:
//                if (currentFilter != FILTER_W)
//                {
//                    _outputCPList.pushBack(new CC(CC.FILTER, 4, addFollowingToFilter(FILTER_W_STRING, gFollowingValue, cValue)));
//                    currentFilter = FILTER_W;
////                    _outputCPList.pushBack(new CC(CC.FRAME, 0, "ENDL")); // TODO раскоментить в случае если фильтры должны быть в отдельном кадре
//                }
//
//                if (X != null)
//                {
//                    _outputCPList.pushBack(new CC(CC.X, X.intValue(), "X"));
//                    X = null;
//                }
//                _outputCPList.pushBack(new CC(CC.Y, _rawCPList.getData(i), "it's W"));
//                break;
//            case CC.C:
//                cValue = new Integer((int)_rawCPList.getData(i));
//                _outputCPList.pushBack(_rawCPList.getFrame(i));
//                break;
//            case CC.G:
//                int data = (int)_rawCPList.getData(i);
//                if ( (data >= 10) && (data <= 13) )
//                    gFollowingValue = new Integer(data);
//                _outputCPList.pushBack(_rawCPList.getFrame(i));
//                break;
//            case CC.FRAME:
//                if (X != null)
//                {
//                    _outputCPList.pushBack(new CC(CC.X, X.intValue(), "X"));
//                    X = null;
//                }
//                _outputCPList.pushBack(new CC(CC.FRAME, 7, "ENDL"));
//                break;
//            default:
//                _outputCPList.pushBack(_rawCPList.getFrame(i));
//            }
//        }
//
//        return null;
//    }
//
//    protected String readParameters(CpParameters cpParameters)
//    {
//        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_LV_NAME))
//            _lv = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_LV_NAME));
//        else
//            return new String("Параметр " + PipeCutterParamsParser.getInstance().
//                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_LV_NAME) + " отсутствует.");
//
//        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME))
//            _lz = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME));
//        else
//            return new String("Параметр " + PipeCutterParamsParser.getInstance().
//                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_LZ_NAME) + " отсутствует.");
//
//        if (cpParameters.hasParam(PipeCutterParamsParser.CP_PARAMETER_D_PIPE_NAME))
//            _R = Integer.parseInt((String)cpParameters.getValue(PipeCutterParamsParser.CP_PARAMETER_D_PIPE_NAME)) / 2;
//        else
//            return new String("Параметр " + PipeCutterParamsParser.getInstance().
//                    _paramsNames.get(PipeCutterParamsParser.CP_PARAMETER_D_PIPE_NAME) + " отсутствует.");
//
//        return null;
//    }
}
