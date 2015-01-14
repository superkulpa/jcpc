package ru.autogenmash.core.utils.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.exceptions.ParameterMissException;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.StringUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.XmlUtils;
import ru.autogenmash.core.utils.compiler.stepactions.ErrorCheckingAction;
import ru.autogenmash.core.utils.compiler.stepactions.InnerAnglesRoundingAction;
import ru.autogenmash.core.utils.compiler.stepactions.KerfMaker;
//import ru.autogenmash.core.utils.compiler.stepactions.KerfingAction;
import ru.autogenmash.core.utils.compiler.stepactions.LoopingAction;
import ru.autogenmash.core.utils.compiler.stepactions.MarkiningAction;
import ru.autogenmash.core.utils.compiler.stepactions.OptimizationAction;
import ru.autogenmash.core.utils.compiler.stepactions.PrepearingAction;
import ru.autogenmash.core.utils.compiler.stepactions.ScaleRotateInverseAction;
import ru.autogenmash.core.utils.compiler.stepactions.SubProgramInsertingAction;
import ru.autogenmash.core.utils.compiler.stepactions.TwoMCommandsBrakeAction;
import ru.autogenmash.core.utils.compiler.stepactions.WaveringMovementsTolerantSmoothAction;
import ru.autogenmash.core.utils.compiler.stepactions.CheckG59CommandAction;


/**
 * @author Dymarchuk Dmitry
 * @version
 * 23.07.2007 16:19:59
 */
public class Compiler
{
    public static final String PARAM_COMMON_CP_NAME = "Common.CpName";
    public static final String PARAM_COMMON_SUB_CP = "Common.SubCp";
    public static final String PARAM_COMMON_OUTPUT_INFO = "Common.OutputInfo";
    public static final String PARAM_COMMON_OUTPUT_ERROR = "Common.OutputError";
    public static final String PARAM_COMMON_OUTPUT_WARNINGS = "Common.OutputWarnings";
    public static final String PARAM_COMMON_OUTPUT_CP_NAME_KERF = "Common.OutputCpNameKerf";
    public static final String PARAM_COMMON_OUTPUT_CP_NAME = "Common.OutputCpName";
    public static final String PARAM_COMMON_FEED = "Common.Feed";
    
    public static final String PARAM_COMMON_TYPE_COMPILER = "Common.TypeCompiler";
    
    public static final String PARAM_COMMON_OUTPUT_RUN_CP_NAME = "Common.OutputRunCpName";
    
    /**формат данных геометрических перемещений*/
    public static final String PARAM_PRECISION_GEO_VALUES = "CP.GeoPrecision";
    
    public static final String PARAM_OPTIMIZATION_MIN_MOVEMENT_LENGTH = "Optimization.MinMovementLength";
    public static final String PARAM_OPTIMIZATION_ANGLE_COMPARE_ACCURACY = "Optimization.AngleCompareAccuracy";
    public static final String PARAM_OPTIMIZATION_COMBINE_SIMILAR_DIRECTION_ARCS = "Optimization.CombineSimilarDirectionArcs";
    
    public static final String PARAM_KERFING_MIN_AUX_ARC_LENGTH = "Kerfing.MinAuxArcLength";
    public static final String PARAM_KERFING_APPROXIMATE_LEADS_BY_LINES = "Kerfing.ApproximateLeadsByLines";
    public static final String PARAM_KERFING_D = "Kerfing.D";
    public static final String PARAM_KERFING_K = "Kerfing.K";

    public static final String PARAM_RSI_ROTATION_ANGLE = "RSI.RotationAngle";
    public static final String PARAM_RSI_SCALE = "RSI.Scale";
    public static final String PARAM_RSI_INVERSE = "RSI.Inverse";
    
    public static final String PARAM_SMOOTHING_K_STEP_ACCURACY = "Smoothing.KStepAccuracy";
    public static final String PARAM_SMOOTHING_MIN_MOVEMENT_LENGTH = "Smoothing.MinMovementLength";
    /** Сглаживать сплайном весь контур или только "колеблющиеся" участки. */
    public static final String PARAM_SMOOTHING_FULL_SHAPE_SPLINE = "Smoothing.FullShapeSpline";
    public static final String PARAM_SMOOTHING_USE_NURBS = "Smoothing.UseNURBS";
    public static final String PARAM_SMOOTHING_SPLINE_INCLUDE_LINES_MAX_LENGTH = "Smoothing.SplineIncludeMaxLength";
    public static final String PARAM_SMOOTHING_SPLINE_STEP_LENGTH = "Smoothing.SplineStepLength";
    
    public static final String PARAM_MARKING_SUPPORT_DRIFT = "Marking.SupportDrift";
    /** Скругляются два гео кадра, т.е. между ними строится дуга, а они уменьшаются в длине. 
     * Если получившаяся длина одного из этих двух перемещений меньше заданного значения, 
     * то сопряжение не скругляется.
     **/
    public static final String PARAM_ROUNDING_MIN_GEO_LENGTH_AFTER_REDUCING = "Rounding.MinGeoLengthAfterReducing";
    public static final String PARAM_ROUNDING_MIN_ANGLE_TO_ROUND = "Rounding.MinAngleToRound";
    public static final String PARAM_ROUNDING_MIN_MOVEMENT_LENGTH = "Rounding.MinMovementLength";
    public static final String PARAM_ROUNDING_ACCURACY = "Rounding.Accuracy";

    public static final String MSG_COMPILER_ERROR = "Внутренняя ошибка компилятора";

    public static final boolean DEBUG = false;

    /** Коэффициент перевода десяток в юниты. */
    public static final int SIZE_TRANSFORMATION_RATIO = 100;

    /** Входной настроечный файл компилятора. */
    public static final String COMPILER_CFG_FILE_NAME = "compiler.cfg";

    /** Буфер чтения файла при очистке от коментариев. */
    public static final int CLEAR_READER_BUFFER = 1024;

    /** Точность сравнения радиусов дугового интерполятора (в юнитах).
     * Если расхождение радиусов не попадает в данный диапозон,
     * то будет сгенерирована ошибка несовпадения радиусов.
     * Иначе будет произведен пересчет центра окружности
     *  */
    public static final int RADIUS_COMPARABLE_ACCURACY = 5000;

    /** Количество строк, которое будет прочитано при разборе параметров УП. */
    public static final int PARAMS_READING_LINES_LIMIT = 10; // FIXME реализовать функциональность

    public static final String CP_INFO_BURN_COUNT = "Количество пробивок";
    public static final String CP_INFO_BURN_LENGTH = "Длина реза";
    public static final String CP_INFO_CP_NAME = "Имя УП";
    public static final String CP_INFO_DELIMITER = ":";
    public static final String CP_INFO_DIMENSION_MEASURE = "мм";
    public static final String CP_INFO_DIMENSION_X = "Габарит X";
    public static final String CP_INFO_DIMENSION_Y = "Габарит Y";
    
    public static final String CP_ERROR = "Error";
    
//    public static final String CP_INFO_DIMENSION_Z = "Габарит Z";
    public static final String CP_INFO_MOVE_LENGTH = "Длина перемещений";

    public static final String[] UNIQ_COMMANDS = {"X", "Y", "I", "J", "M", "H", "L"};
    public static final TreeMap UNIQ_COMMANDS_MAP = new TreeMap();

    /** 33, 34 - маркировщик. */
    public static final int[] CORRECT_M_COMMANDS = {0, 2, 17, 19, 20, 28, 29, 30, 33, 34, 45, 46, 70, 71, 72, 73,
        74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 90, 91, 92, 93, 94, 95, 96, 98, 700 };
    public static final int[] CORRECT_G_COMMANDS = {-1, 0, 1, 2, 3, 9,
        10, 11, 12, 13, 30, 40, 41, 42, 59, 90, 91, 900, 100, 101};

    private static Log _log = LogFactory.getLog(Compiler.class);

    public static final List DEFAULT_COMPILER_STEPS = new ArrayList();

    static
    {
        DEFAULT_COMPILER_STEPS.add(SubProgramInsertingAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(LoopingAction.class.getName());
        //DEFAULT_COMPILER_STEPS.add(MarkiningAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(PrepearingAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(ErrorCheckingAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(OptimizationAction.class.getName());
        //DEFAULT_COMPILER_STEPS.add(KerfingAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(KerfMaker.class.getName());
        DEFAULT_COMPILER_STEPS.add(ScaleRotateInverseAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(OptimizationAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(WaveringMovementsTolerantSmoothAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(InnerAnglesRoundingAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(MarkiningAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(TwoMCommandsBrakeAction.class.getName());
        DEFAULT_COMPILER_STEPS.add(CheckG59CommandAction.class.getName());

        Arrays.sort(CORRECT_M_COMMANDS);
        Arrays.sort(CORRECT_G_COMMANDS);
    }

    private List _compilerSteps = DEFAULT_COMPILER_STEPS;

    /** Файл компилированной УП. */
    private OutputStreamWriter _ccp;
    private String _ccpFileName;

    /** Файл компилированной УП с учетом эквидистанты. */
    private OutputStreamWriter _ccpKerf;
    private String _ccpKerfFileName;

    /** Имя файла исходной УП. */
    private String _cpFileName;

    /** Файл информации компилированной УП. */
    private String _cpInfoFileName;

    /** Файл ошибок компилированной УП. */
    private String _cpErrorFileName;

    /** Файл предупреждений компилятора. */
    private String _cpWarningsFileName;

    private String _cpName;

    /** Очищенная от коментариев УП. */
    private Vector _filtratedCP;

    /** Лист управляющих команд (обычный). */
    private CPList _cpList;

    /** Лист управляющих команд (эквидистантный). */
    private CPList _cpListKerf = null;

    /** Параметры УП, требуемые при компиляции. */
    private CpParameters _cpParams;

    private int _burnCount;
    private long _burnLength;
    private long _moveLength;
    private long _minX;
    private long _maxX;
    private long _minY;
    private long _maxY;
//    private long _minZ;
//    private long _maxZ;

    private CompilerError _error = null;
    private Vector _warnings = new Vector();


    public static String[] getSubCps(String cpFileName)
    throws FileNotFoundException, IOException
    {
        File file = new File(cpFileName);
        if (file.exists() == false || file.isFile() == false)
            return null;

        ArrayList subCps = new ArrayList();

        RandomAccessFile cp = new RandomAccessFile(file, "r");

        boolean inLineComment = false;
        boolean inMultiLineComment = false;

        boolean inCPName = false;

        String cpLine = "";

        char c;

        byte[] inputData = new byte[CLEAR_READER_BUFFER];
        while(cp.read(inputData) != -1)
        {
            for (int i = 0; i < inputData.length; i++)
            {
                c = (char)inputData[i];
                switch (c)
                {
                case '/':
                case '#':
                    inLineComment = true;
                    break;
                case '(':
                    inMultiLineComment = true;
                    break;
                case ')':
                    inMultiLineComment = false;
                    break;
                case '%':
                    if (inCPName == false)
                        cpLine = "";
                    else
                        cpLine += (char)c;
                    inCPName = true;
                    break;
                case '\r':
                    break;
                case '\n':
                    inLineComment = false;
                    if (inCPName)
                    {
                        inCPName = false;
                        String trimedCpLine = cpLine.trim();
                        if (trimedCpLine.length() > 0)
                            subCps.add(trimedCpLine);
                    }
                    cpLine = "";
                    break;
                default:
                    if ( (inLineComment == false) && (inMultiLineComment == false) )
                        cpLine += (char)c;
                }
            }
        }

        cp.close();

        if (subCps.size() == 0)
            return null;

        return (String[])subCps.toArray(new String[0]);
    }

    public Compiler(String cfgFileName) throws Throwable
    {
        long time = new Date().getTime();

        if (cfgFileName == null)
            throw new  IllegalArgumentException("cfgFileName can not be null");
        if ((new File(cfgFileName)).exists() == false)
            throw new  FileNotFoundException(cfgFileName);

        HashMap inputParams = new HashMap();
        XmlUtils.readParameters2(cfgFileName, inputParams);

        _cpParams = new CpParameters();
        
        String cpFileName = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_CP_NAME, "Файл УП");
        String outputCpFileName = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_OUTPUT_CP_NAME, "Файл компилированной УП");
        String outputCpFileNameKerf = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_OUTPUT_CP_NAME_KERF, "Файл компилированной УП с эквидистантой");
        String outputRunCPFileName = (String)checkAndAddParameter(inputParams, 
        				PARAM_COMMON_OUTPUT_RUN_CP_NAME, "Файл компилированной УП с эквидистантой для ядра управления");
        
        String outputInfoFileName = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_OUTPUT_INFO, "Файл информации об УП");
        String outputErrorFileName = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_OUTPUT_ERROR, "Файл ошибок УП");
        String outputWarningsFileName = (String)checkAndAddParameter(inputParams, 
                PARAM_COMMON_OUTPUT_WARNINGS, "Файл предупреждений УП");
        
        try{
          checkAndAddParameter(inputParams,PARAM_COMMON_TYPE_COMPILER, "Вид компилятора");
        }catch(Throwable e) {};
        _cpFileName = cpFileName;
        
        if (outputCpFileName == null)
            outputCpFileName = Utils.getTmpDir() + "/list.ccp";
        
        Utils.createPath(outputCpFileName, true);
        _ccp = new OutputStreamWriter(new FileOutputStream(outputCpFileName), "UTF8");
        _ccpFileName = outputCpFileName;

        if (outputCpFileNameKerf == null)
            outputCpFileNameKerf = Utils.getTmpDir() + "/list.kerf.ccp";
            
        Utils.createPath(outputCpFileNameKerf, true);
        _ccpKerf = new OutputStreamWriter(new FileOutputStream(outputCpFileNameKerf), "UTF8");
        _ccpKerfFileName = outputCpFileNameKerf;

        if (outputRunCPFileName == null)
        	outputRunCPFileName = "/CNC/tmp/cpRun.ccp";
        Utils.createPath(outputRunCPFileName, true);
        
        Utils.createPath(outputInfoFileName, true);
        _cpInfoFileName = outputInfoFileName;
        _cpErrorFileName = outputErrorFileName;
        _cpWarningsFileName = outputWarningsFileName;
        
        checkAndAddParameter(inputParams, PARAM_COMMON_SUB_CP, "Имя подпрограммы");
        checkAndAddParameter(inputParams, PARAM_COMMON_FEED, "Скорость реза, в мм/мин");
        
        //checkAndAddParameter(inputParams, PARAM_PRECISION_GEO_VALUES, "Точность задания после запятой");
        
        checkAndAddParameter(inputParams, PARAM_MARKING_SUPPORT_DRIFT, "Смещение рабочего органа маркировщика, в десятых мм, формат: x, y");
        
        checkAndAddParameter(inputParams, PARAM_OPTIMIZATION_ANGLE_COMPARE_ACCURACY, "Точность сравнения углов, в десятых градуса");
        checkAndAddParameter(inputParams, PARAM_OPTIMIZATION_MIN_MOVEMENT_LENGTH, "Минимальное допустимое перемещение, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_OPTIMIZATION_COMBINE_SIMILAR_DIRECTION_ARCS, "Объединять 'колинеарные' дуги, true или false");
        
        checkAndAddParameter(inputParams, PARAM_KERFING_D, "Ширина реза, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_KERFING_K, "Количество шагов при удалении петель");
        checkAndAddParameter(inputParams, PARAM_KERFING_MIN_AUX_ARC_LENGTH, "Минимальная длина вспомогательной дуги при построении эквидистанты, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_KERFING_APPROXIMATE_LEADS_BY_LINES, "Замена дуговых подходов/отходов линейными, true или false");

        checkAndAddParameter(inputParams, PARAM_RSI_SCALE, "Масштаб УП, в %");
        checkAndAddParameter(inputParams, PARAM_RSI_ROTATION_ANGLE, "Угол поворота УП, в десятых градуса");
        checkAndAddParameter(inputParams, PARAM_RSI_INVERSE, "Переворот УП, true или false");
        
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_K_STEP_ACCURACY, "Максимальное отклонение от исходной траектории при сглаживании, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_MIN_MOVEMENT_LENGTH, "Минимальное допустимое перемещение, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_FULL_SHAPE_SPLINE, "Сглаживать весь контур или колеблящиеся участки, true или false");
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_USE_NURBS, "Использовать NURBS при сплайн-сглаживании, true или false");
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_SPLINE_INCLUDE_LINES_MAX_LENGTH, "Максимальная длина шага при поиске колеблющихся участков, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_SMOOTHING_SPLINE_STEP_LENGTH, "Длина шага сплайна при сглаживании, в десятых мм");
        
        checkAndAddParameter(inputParams, PARAM_ROUNDING_ACCURACY, "Точность скругления (максимальное отклонение от исходной траектории), в десятых мм");
        checkAndAddParameter(inputParams, PARAM_ROUNDING_MIN_GEO_LENGTH_AFTER_REDUCING, "Минимальная остаточная длина при скруглении, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_ROUNDING_MIN_ANGLE_TO_ROUND, "Минимальный скругляемый угол, в десятых мм");
        checkAndAddParameter(inputParams, PARAM_ROUNDING_MIN_MOVEMENT_LENGTH, "Минимальная длина перемещений, в десятых мм");


        _filtratedCP = new Vector();
        _cpList = new CPList();

        long compileTime = new Date().getTime();
        compile();
        Utils.traceTime(_log, compileTime, "compiling time");
        Utils.traceTime(_log, time, "full time");
    }

    public ICPListBuilder getCpListBuilder(int _type)
    {
      switch(_type) {
        case 0:
        return DefaultCPFactory.getInstance(_cpParams);
        case 1:
        return BurnyCPFactory.getInstance(_cpParams);
        case 2:
        return PhoenixCPFactory.getInstance(_cpParams);
      }
      return null;  
    }
    
    private Object checkAndAddParameter(HashMap inputParams, String paramName, String description)
    throws ParameterMissException
    {
        if (inputParams.containsKey(paramName) == false)
            throw new ParameterMissException("Параметр \"" + paramName + 
                "\" (" + description + ") отстутствует в конфигурационном файле");
    
        Object value = inputParams.get(paramName);
        addParameter(paramName, value, description);
        
        return value;
    }

    /**Построить CpList согласно указанному билдеру.
     */
    private void buildCpList(int _type)
    {
        _error = getCpListBuilder(_type).build(_filtratedCP, _cpList, _warnings);
    }

    /**Stepping
     */
    private void doSteps()
    {
        long time;

        IStepAction stepAction;

        for (int i = 0; i < _compilerSteps.size(); i++)
        {
            String stepActionClassName = (String)_compilerSteps.get(i);

            try
            {
                Class stepActionClass = Class.forName(stepActionClassName);
                Constructor stepActionConstructor = stepActionClass.getConstructor(new Class[] {Compiler.class, List.class});
                Object stepActionObject = stepActionConstructor.newInstance(new Object[] {this, _warnings});

                if (stepActionObject instanceof IStepAction == false)
                {
                    _log.error("\"" + stepActionClassName + "\" не реализует интерфейс \"" +
                            IStepAction.class.getName() + "\"");
                    _error = new CompilerError(MSG_COMPILER_ERROR);
                    return;
                }
                stepAction = (IStepAction)stepActionObject;

                time = new Date().getTime();

                String prefix = ".After" + StringUtils.getClassName(stepActionClass);

                if (_cpListKerf == null && stepAction instanceof OptimizationAction)
                    _cpListKerf = (CPList)_cpList.clone();

                if (_cpListKerf != null)
                {
                    if (executeAction(_cpListKerf, _cpParams, stepAction, "debugInfo.cpList." + (i+1) + prefix + ".kerf.txt") < 0)
                        return;
                    
                    if (stepAction instanceof ScaleRotateInverseAction)
                        if (executeAction(_cpList, _cpParams, stepAction, "debugInfo.cpList." + (i+1) + prefix + ".txt") < 0)
                            return;
                }
                else
                {
                    if (executeAction(_cpList, _cpParams, stepAction, "debugInfo.cpList." + (i+1) + prefix + ".txt") < 0)
                        return;
                }

                Utils.traceTime(_log, time, stepActionClassName);

            }
            catch (Throwable e)
            {
                _log.error(e.getLocalizedMessage(), e);
                _error = new CompilerError(MSG_COMPILER_ERROR);
            }
        }
    }

    private int executeAction(CPList cpList, CpParameters cpParams,
            IStepAction stepAction, String debugFileName)
    throws FileNotFoundException, IOException
    {
        CompilerError result = stepAction.execute(cpList, cpParams);
        if (DEBUG == true)
            cpList.printToFile(debugFileName);
        if (result != null)
        {
            _error = result;
            return -1;
        }

        return 0;
    }

    protected void beforeCompile() throws Throwable
    {

    }

    protected void afterCompile() throws Throwable
    {

    }

    /**Очистить входной УП файл от комментариев и от другой "не нужной"
     * информации.
     * @throws IOException
     */
    public static CompilerError filtrateSource(String cpFileName, String subCpName, Vector result, List warnings)
    throws IOException
    {
        //result = new Vector(100, 100);
        int frameNumber = 1;
        Vector subCps = new Vector();
        
        RandomAccessFile cp = new RandomAccessFile(cpFileName, "r");

        if (subCpName.trim().equals("") || subCpName.trim().equals("noname"))
            subCpName = null;

        boolean inLineComment = false;
        boolean inMultiLineComment = false;
        int multiLineCommentCount = 0;

        boolean inCPName = false;

        boolean inSubCp = false;

        String cpLine = "";
        String currentSubCpName = null;

        int c;

        byte[] inputData = new byte[1024];
        while(cp.read(inputData) != -1)
        {
            for (int i = 0; i < inputData.length; i++)
            {
                c = inputData[i];
                switch ((char)c)
                {
                case '/':
                case '#':
                    inLineComment = true;
                    break;
                case '(':
                    inMultiLineComment = true;
                    multiLineCommentCount++;
                    break;
                case ')':
                    inMultiLineComment = false;
                    multiLineCommentCount--;
                    break;
                case '%':
                    if (inMultiLineComment == true || inLineComment == true)
                        break;
                    cpLine = ""; // если есть другие команды, то игнорировать их
                    if ( (!cpLine.trim().equals("") && subCpName == null) ||
                            (result.size() > 0 && subCpName == null) ||
                            inSubCp)
                    {
                        cp.close();
                        if (cpLine.trim().equals("") == false)
                            warnings.add(new CompilerError(frameNumber, "Название подпрограммы (%имя) должно быть единственной командой в кадре"));
                        return null;
                    }
                    inCPName = true;
                    break;
                case '\r':
                case '\n':
                    frameNumber++;
                    inLineComment = false;
                    if (cpLine.trim().equals("") == false)
                    {
                        if (inCPName)
                        {
                            inCPName = false;
                            currentSubCpName = cpLine.trim();
                            subCps.add(currentSubCpName);

                            if ( (currentSubCpName.equalsIgnoreCase(subCpName) || subCpName == null) /*&& _cpName == null*/)
                            {
                                //_cpName = currentSubCpName;
                                result.clear();
                                inSubCp = true;
                            }
                        }
                        else
                        {
                            if (currentSubCpName == null ||
                                    (subCpName == null && subCps.size() < 2) ||
                                    currentSubCpName.equalsIgnoreCase(subCpName))
                                result.add(cpLine);
                        }
                    }
//                    else
//                        warnings.add(new CompilerError("Присутствуют пустые строки"));
                    cpLine = "";
                    break;
                default:
                    if ( (inLineComment == false) && (inMultiLineComment == false) )
                        cpLine += (char)c;
                }
            }
            inputData = new byte[1024];
        }

        if (cpLine.trim().equals("") == false)
            result.add(cpLine);

        cp.close();

        if (inSubCp == false && subCpName != null)
        {
            final String warnMsg = "В управляющей программе \"" +
            cpFileName + "\" отсутствует подпрограмма \"" + subCpName + "\"";
            warnings.add(new CompilerError(warnMsg));
            //throw new IllegalArgumentException(warnMsg);
        }

        return null;
    }

    protected void generateCCP(Writer ccp, CPList cpList) throws IOException
    {
        ccp.write("cp\n");
        //if (Utils.isQnx())
        {
            if (_cpName == null)
                _cpName = "noname";
            
            ccp.write(_cpFileName + "\n");
            ccp.write(_cpName + "\n");
        }

        cpList.generateCCP(ccp);

        // для cp_info файла
        int burnCount = 0; // количество пробивок
        boolean burn = false; // режет или нет
        long burnLength = 0; // путь реза
        long moveLength = 0; // путь движения
        long minX = 0, maxX = 0, currentX = 0;
        long minY = 0, maxY = 0, currentY = 0;
        //long minZ = 0, maxZ = 0, currentZ = 0;

        // для ccp файла
        Long X = null;
        Long Y = null;
        //Long Z = null;

        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);

//            if ( (frame.hasM() | frame.hasG00() | frame.hasG01() |
//                    frame.hasG02() | frame.hasG03()) == false )
//                continue;
            if (frame.hasM()) {
              if (Arrays.binarySearch(CC.CUT_ON_COMMANDS, frame.getCCByType(CC.M).getData()) >= 0)
              {
                  burnCount++;
                  burn = true;
              }
              else if (Arrays.binarySearch(CC.CUT_OFF_COMMANDS, frame.getCCByType(CC.M).getData()) >= 0)
              {
                  burn = false;
              }
            };
            if (frame.hasG00() | frame.hasG01()) {
            	if(frame.getCCByType(CC.X) != null) {
                X = new Long(frame.getCCByType(CC.X).getData());
                moveLength += Math.abs(X.intValue());
                currentX += X.intValue();
                if (currentX < minX) minX = currentX;
                if (currentX > maxX) maxX = currentX;
                if (burn)
                    burnLength += Math.abs(X.intValue());
            	};
            	if (frame.getCCByType(CC.Y) != null) {
                Y = new Long(frame.getCCByType(CC.Y).getData());
                moveLength += Math.abs(Y.intValue());
                currentY += Y.intValue();
                if (currentY < minY) minY = currentY;
                if (currentY > maxY) maxY = currentY;
                if (burn)
                    burnLength += Math.abs(Y.intValue());
                
            	};
            };
            if (frame.hasG02() | frame.hasG03()) {
            	int aX, aY, aI, aJ;
            	aX = aY = aI = aJ = 0;
            	if (frame.getCCByType(CC.X) != null) aX = frame.getCCByType(CC.X).getData();
            	if (frame.getCCByType(CC.Y) != null) aY = frame.getCCByType(CC.Y).getData();
            	if (frame.getCCByType(CC.I) != null) aI = frame.getCCByType(CC.I).getData();
            	if (frame.getCCByType(CC.J) != null) aJ = frame.getCCByType(CC.J).getData();
            	double Len = MathUtils.calculateArcLength(aX, aY, aI, aJ, frame.hasG03());
            	moveLength += Len;
            	if (burn)
                burnLength += Len;
            	
            	CpSubFrame[] tmpFrames = MTRUtils.interpolateArc(aX, aY, aI, aJ, frame.hasG03(), 20, CC.X, CC.Y);
            	
            	for (int j = 0; j < tmpFrames.length; j++)
              {
            		Point tmpPoint = tmpFrames[j].getXY();

                currentX += tmpPoint.x;
                if (currentX < minX) minX = currentX;
                if (currentX > maxX) maxX = currentX;
                
                currentY += tmpPoint.y;
                if (currentY < minY) minY = currentY;
                if (currentY > maxY) maxY = currentY;
                
              };
            };
//            for (int j = 0; j < frame.getLength(); j++)
//            {
//                CpSubFrame subFrame = frame.getSubFrame(j);
//                for (int k = 0; k < subFrame.getLength(); k++)
//                {
//                    CC cc = subFrame.getCC(k);
//                    switch (cc.getType())
//                    {
//                    case CC.X:
//                        X = new Long(cc.getData());
//                        moveLength += Math.abs(X.intValue());
//                        currentX += X.intValue();
//                        if (currentX < minX) minX = currentX;
//                        if (currentX > maxX) maxX = currentX;
//                        if (burn)
//                            burnLength += Math.abs(X.intValue());
//                        break;
//                    case CC.Y:
//                        Y = new Long(cc.getData());
//                        moveLength += Math.abs(Y.intValue());
//                        currentY += Y.intValue();
//                        if (currentY < minY) minY = currentY;
//                        if (currentY > maxY) maxY = currentY;
//                        if (burn)
//                            burnLength += Math.abs(Y.intValue());
//                        break;
////                    case CC.Z:
////                        Z = new Long(cc.getData());
////                        moveLength += Math.abs(Z.intValue());
////                        currentZ += Z.intValue();
////                        if (currentZ < minZ) minZ = currentZ;
////                        if (currentZ > maxZ) maxZ = currentZ;
////                        if (burn)
////                            burnLength += Math.abs(Z.intValue());
////                        break;
////                    case CC.I:    // FIXME не обсчитываются габариты детали по дугам, устраняется путем вызоыа CPList.getShapeSize()
////                    case CC.J:
//                    case CC.M:
//                        if (Arrays.binarySearch(CC.CUT_ON_COMMANDS, cc.getData()) >= 0)
//                        {
//                            burnCount++;
//                            burn = true;
//                        }
//                        else if (Arrays.binarySearch(CC.CUT_OFF_COMMANDS, cc.getData()) >= 0)
//                        {
//                            burn = false;
//                        }
//                        break;
//                    }
//                }
//            }
        }
        _burnCount = burnCount;
        _burnLength = burnLength;
        _moveLength = moveLength;
        _minX = minX;
        _maxX = maxX;
        _minY = minY;
        _maxY = maxY;
//        _minZ = minZ;
//        _maxZ = maxZ;
     }

    protected void generateCpErrors() throws IOException
    {
        if (_error == null)
            throw new IllegalArgumentException("error can not be null, during calling this method");

        _log.warn("Компиляция закончена с ошибками");

        Utils.saveToTxtFile(_cpErrorFileName, getErrorMessage(), "UTF8");
        Utils.clearFile(_cpInfoFileName);
        
        HashMap map = new HashMap();
        map.put(_error.getMessage(),String.valueOf(_error.getFrameNumber()));
        //map.put(CP_INFO_DIMENSION_Z, String.valueOf((double)(Math.round((double)(Math.abs(_minZ) + Math.abs(_maxZ)) / SIZE_TRANSFORMATION_RATIO)) / 10));
        XmlUtils.writeParameters2(_cpInfoFileName, map, "Info", true);
        
        //Utils.saveToTxtFile(_cpErrorFileName, getErrorMessage(), "UTF8");
        //Utils.clearFile(_cpInfoFileName);
    }

    protected void generateWarnings() throws IOException
    {
        if (_warnings.size() == 0)
            return;

        _log.warn("Компиляция закончена с предупреждениями");

        Utils.saveToTxtFile(_cpWarningsFileName, getWarnings(), "UTF8");
    }

    protected void generateCpInfo() throws IOException
    {
        Utils.clearFile(_cpInfoFileName);
        HashMap map = new HashMap();
        int indx = _cpFileName.indexOf("/cps/"); 
        if(indx != -1) {
        	_cpFileName = _cpFileName.substring(indx + 5);
        };
        map.put(CP_INFO_CP_NAME, (_cpName != null ? _cpFileName : "не задано"));
        map.put(CP_INFO_BURN_COUNT, String.valueOf(_burnCount));
        map.put(CP_INFO_BURN_LENGTH, String.valueOf((double)(Math.round((double)_burnLength / SIZE_TRANSFORMATION_RATIO)) / 10));
        map.put(CP_INFO_MOVE_LENGTH, String.valueOf((double)(Math.round((double)_moveLength / SIZE_TRANSFORMATION_RATIO)) / 10));
        map.put(CP_INFO_DIMENSION_X, String.valueOf((double)(Math.round((double)(Math.abs(_minX) + Math.abs(_maxX)) / SIZE_TRANSFORMATION_RATIO)) / 10));
        map.put(CP_INFO_DIMENSION_Y, String.valueOf((double)(Math.round((double)(Math.abs(_minY) + Math.abs(_maxY)) / SIZE_TRANSFORMATION_RATIO)) / 10));
        //map.put(CP_INFO_DIMENSION_Z, String.valueOf((double)(Math.round((double)(Math.abs(_minZ) + Math.abs(_maxZ)) / SIZE_TRANSFORMATION_RATIO)) / 10));
        XmlUtils.writeParameters2(_cpInfoFileName, map, "Info", false);
    }

    /**Компилировать УП
     * <p>
     * Порядо выполнения:
     * <ul>
     *  <li>Сделать предкомпиляционные действия (если имеются)</li>
     *  <li>Очистить УП от коментариев и другой ненужной информации</li>
     *  <li>Построить CpList на основании указанного Builder-а</li>
     *  <li>Сделать заданные шаги компиляции (setCompilerSteps(...))</li>
     *  <li>Сделать послекомпиляционные действия (если имеются)</li>
     *  <li>Сгенерировать выходные файлы (ccp, info, errors, ...)</li>
     *  <li></li>
     * </ul>
     * @throws Throwable
     */
    protected void compile() throws Throwable
    {
        _log.info("Компиляция УП \"" + _cpFileName + "\" стартует...");

        long time = new Date().getTime();

        //time = new Date().getTime();

        beforeCompile();
        Utils.traceTime(_log, time, "предкомпиляционные действия");
        if (_error != null)
        {
            generateCpErrors();
            return;
        }
        
        String subCpName = (String)_cpParams.getValue(PARAM_COMMON_SUB_CP);
        time = new Date().getTime();
        _error = filtrateSource(_cpFileName, subCpName, _filtratedCP, _warnings);
        Utils.traceTime(_log, time, "очистка входной УП");
        if (_error != null)
        {
            generateCpErrors();
            return;
        }
        
        time = new Date().getTime();
        int type = 0;
        try {
          type = Integer.parseInt((String)_cpParams.getValue(PARAM_COMMON_TYPE_COMPILER));
        }catch(Throwable e){};
        buildCpList(type);
        Utils.traceTime(_log, time, "построение cpList-а");
        if (_error != null)
        {
            generateCpErrors();
            return;
        }
        if (DEBUG == true)
            _cpList.printToFile("debugInfo.cpList.0.AfterBuild.txt");

        doSteps();
        
        if (DEBUG)
        {
            MTRUtils.checkCpList(_cpListKerf);
            //MTRUtils.checkCpListGeoLengths(_cpListKerf, 500);
        }
        
        if (_error != null)
        {
            generateCpErrors();
            return;
        }

        time = new Date().getTime();
        afterCompile();
        Utils.traceTime(_log, time, "послекомпиляционные действия");
        if (_error != null)
        {
            generateCpErrors();
            return;
        }

        // clear error and warning file
        Utils.clearFile(_cpErrorFileName);
        Utils.clearFile(_cpWarningsFileName);

        time = new Date().getTime();
        generateCCP(_ccp, _cpList);

        if (_cpListKerf != null)
            generateCCP(_ccpKerf, _cpListKerf);
        else
            Utils.copyFiles(_ccpFileName, _ccpKerfFileName);

        generateCpInfo();
        generateWarnings();
        Utils.traceTime(_log, time, "создание конечных файлов");
        _log.info("Компиляция успешно закончена");
    }

    public void addParameter(String name, Object value, String description)
    {
        _cpParams.add(name, value, description);
    }

    public Object getParameter(String name)
    {
        return _cpParams.getValue(name);
    }

    protected void setCompilerSteps(List compilerSteps)
    {
        _compilerSteps = compilerSteps;
    }

    public List getCompilerSteps()
    {
        return _compilerSteps;
    }

    public CPList getCpList()
    {
        return _cpList;
    }

    public String getCpFileName()
    {
        return _cpFileName;
    }

    public String getCcpFileName()
    {
        return _ccpFileName;
    }

    public String getCcpKerfFileName()
    {
        return _ccpKerfFileName;
    }

    public String getCpInfoFileName()
    {
        return _cpInfoFileName;
    }

    public String getErrorMessage()
    {
        if (_error != null)
            return "Ошибка" + _error.toString();
        else
            return null;

    }

    public String getWarnings()
    {
        String warnings = "Предупреждения:\n";

        Iterator iterator = _warnings.iterator();
        while (iterator.hasNext())
        {
            CompilerError element = (CompilerError)iterator.next();
            warnings += element.toString() + "\n";
        }
        return warnings;
    }

    public Vector getFiltratedCp()
    {
        return _filtratedCP;
    }

    public static void main(String[] args)
 {
		
		if( (args.length < 8) && (args.length != 0)){
			System.out.println("Args: " + StringUtils.toString(args));

			System.out.println("Invalid arguments\n"
							+ "usage: <compiler> cp_name, sub_cp, scale, feed, kerf, angle, inversement, optimization_level");
			return;
		}
		
		//удалить "cpc.error"
		final String errorDataFileName = Utils.getTmpDir() + "/cpc.error";
		File file = new File(errorDataFileName);
		file.delete();
		try {
		  //прочитать настройки компилятора 
		  String cfg = Utils.getIniDir() + "/compiler0.cfg";  
		  if(args.length != 0) {
		    int optimizationLevel = 0;
		    String optimizationLevelString = args[7].trim();
		    try {
		      optimizationLevel = Integer.parseInt(optimizationLevelString);
		      if (optimizationLevel > 0) {
			File cfgFile = new File(Utils.getIniDir() + "/compiler"
				+ optimizationLevel + ".cfg");
					
			if (cfgFile.exists() == false)
			  System.err.println("Config file \"" + cfgFile.getAbsolutePath()
								+ "\" not exist");
					
			else if (cfgFile.canRead() & cfgFile.canWrite() == false)
			  System.err.println("Check file permissions for \""
								+ cfgFile.getAbsolutePath() + "\"");
					
			else if (cfgFile.length() <= 0)
			  System.err.println("File \"" + cfgFile.getAbsolutePath()
								+ "\" is empty");
			else
			  cfg = cfgFile.getAbsolutePath();
		      }
		    } catch (NumberFormatException e) {
		       System.err.println("Wrong optimization level: "
						+ optimizationLevelString);
		    }

		    System.out.println("Compiler configuration file: " + cfg);
    
		    LinkedMap params = new LinkedMap();
			
		    XmlUtils.readParameters2(cfg, params);

          // params.get(PARAM_COMMON_OUTPUT_ERROR)
			
		    params.put(PARAM_COMMON_CP_NAME, args[0].trim());
		    params.put(PARAM_COMMON_SUB_CP, args[1].trim());
		    params.put(PARAM_RSI_SCALE, args[2].trim());
		    params.put(PARAM_COMMON_FEED, args[3].trim());
		    params.put(PARAM_KERFING_D, args[4].trim());
		    params.put(PARAM_RSI_ROTATION_ANGLE, args[5].trim());
		    params.put(PARAM_RSI_INVERSE, args[6].trim());// optimizationLevelString);
		    if (args.length == 9) {
		      //вид компилятора, если нет то 0
		      params.put(PARAM_COMMON_TYPE_COMPILER, args[8].trim());
		    }
		    
		    XmlUtils.writeParameters2(cfg, params, "compiler", false);
		  };
			//и откомпилить!
 		 new Compiler(cfg);
			
		 //разобрать результат
		 //привести к формату Form::cpc.error и создать этот файл 
		 File errorFile = new File(Utils.getTmpDir() + "/compile.error");
		 if (errorFile.exists() && errorFile.length() > 0) {
		    BufferedReader reader = new BufferedReader(new FileReader(errorFile));

   	    String line = reader.readLine();

   	    String errData = "0\n" + line + "\n0";

   	    FileOutputStream errDataFile = new FileOutputStream(errorDataFileName);
				errDataFile.write(errData.getBytes());
				errDataFile.close();
		 }
		} catch (Throwable e) {
			System.err.println("Exception during compiling. See log file.");
			_log.error(MSG_COMPILER_ERROR, e);
			try {
				FileOutputStream errDataFile = new FileOutputStream(errorDataFileName);
				errDataFile.write(MSG_COMPILER_ERROR.getBytes());
				errDataFile.close();
			} catch (Throwable e2) {
				System.err.println("Exception during error writeng. See log file.");
				_log.error(MSG_COMPILER_ERROR, e);
			}
		}


		try {
			Utils.touch(Utils.getTmpDir() + "/cpc.completed");
		} catch (Throwable e) {
			System.err.println("Exception during touching 'cpc.completed'. See log file.");
			_log.error(MSG_COMPILER_ERROR, e);
		}

	}

//    public static void main(String[] args)
//    {
//        int retCode = 0;
//
//        if (args.length != 1)
//        {
//            System.out.println("Invalid arguments\n" +
//                    "usage: compiler " + COMPILER_CFG_FILE_NAME);
//            return;
//        }
//
//        try
//        {
//            new Compiler(args[0]);
//        }
//        catch (Throwable e)
//        {
//            retCode = -1;
//            _log.error(MSG_COMPILER_ERROR, e);
//        }
//
//        System.exit(retCode);
//    }

}
