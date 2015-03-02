/*$Id: JMacros.java,v 1.2 2009/11/11 12:28:15 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.widgets.Display;

import ru.autogenmash.core.ArcInterpolator;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.gui.AGMStyledSWTForm;
import ru.autogenmash.gui.SWTFormBase;

/**
 * @author Dymarchuk Dmitry
 * @version
 * partial refactoring 19.09.2007 9:06:15
 */
public abstract class JMacros
{
    public static final String OUTPUT_DIR;
    public static final String OUTPUT_CP_NAME = "macro";
    public static final String OUTPUT_EXTENSION = ".cp";

    public static final String DISTANCE_BETWEEN_SHAPES = "Переход";
    /** Минимальная высота размерной линии. */
    public static final int MIN_LINE_SIZE_HEIGHT = 50;
    /** Макс высота размерной линии. */
    public static final int MAX_LINE_SIZE_HEIGHT = 75;

    /** Длина размерной стрелки. */
    public static final int ARROW_LENGTH = 10;

    /** Длина шага интерполирования дуги. */
    public static final int ARC_FEED = 10;

    /** Количество разбиений (на единицу длины экрана) при интерполировании дуги. */
    public static final int ARC_STEP_COUNT_PER_M = 20;

    /** Допустимые шаги масштабной сетки. */
    public static final int[] GRADUATION = { 50, 100, 200, 300, 500,
                    1000, 1500, 2000, 2500, 5000, 10000, 15000, 20000, 25000, 50000, 100000 };
    /**тип подхода/отхода - отсутствует*/
    public static final int LEAD_None = 0;
    public static final int LEAD_Tangent = 1; // тип подхода/отхода - касательная
    public static final int LEAD_Normal = 2; // тип подхода/отхода - нормаль
    public static final int LEAD_Arc = 3; // тип подхода/отхода - дуга

    public static final int RES_800x600 = 10; // резолюция экрана 800x600
    public static final int RES_1024x768 = 11; // резолюция экрана 1024x768
    public static final int RES_1280x1024 = 12; // резолюция экрана 1280x1024

    /** Выход отсутствует. */
    public static final int EXIT_NONE = 0;
    /** Выход в начальную точку. */
    public static final int EXIT_TO_START_POINT = 1;
    /** Выход по X. */
    public static final int EXIT_X = 2;
    /** Выход по Y. */
    public static final int EXIT_Y = 3;

    static
    {
        if (System.getProperty("os.name").equals("QNX"))
            OUTPUT_DIR = "/cps";
        else
            OUTPUT_DIR = "D:\\cps";
    }

    // данные
    /** Параметры с формы. */
    protected JMacrosParameters geoParameters;
    /** Спиок контуров. */
    protected Vector shapeList;
    /** Список определяющий линейные размеры макроса. */
    protected JLineSizeList lineSizeList;
    /** Список траекторий. */
    protected Vector trajectoryList;
    /** Список траекторий для геометрической проверки. */
    protected Vector checkList;
    /** Общая траектория. */
    protected JCPList generalTrajectory;

    private static JMacrosForm form; // форма для управления и отрисовки

    // рисовальные данные
    private Image imageView; // image для отрисовки макроса
    private GC gc; // графическая канва для отрисовки макроса
    private Font font; // текущий фонт
    private int maxCellCount; // теоретическое количество ячеек координатной сетки
    private int resolution; // текущая резолюция экрана
    private int drift; // смещение рисования от 0 точки
    /** Мин высота размерной линии. */
    private int minLineSizeHeight = MIN_LINE_SIZE_HEIGHT;
    /** Макс высота размерной линии. */
    private int maxLineSizeHeight = MAX_LINE_SIZE_HEIGHT;
    private int lineWidthInt; // толщина внутренней линии
    private int lineWidthExt; // толщина внешней линии
    private int lineWidthInt_800x600; // толщина внутренней линии, в зависимомти от резолюции экрана
    private int lineWidthInt_1024x768; // толщина внутренней линии, в зависимомти от резолюции экрана
    private int lineWidthInt_1280x1024; // толщина внутренней линии, в зависимомти от резолюции экрана
    private int lineWidthExt_800x600; // толщина внешней линии, в зависимомти от резолюции экрана
    private int lineWidthExt_1024x768; // толщина внешней линии, в зависимомти от резолюции экрана
    private int lineWidthExt_1280x1024; // толщина внешней линии, в зависимомти от резолюции экрана
//    private int rDrawPoint; // радиус обычной рисовальной точки
    private int rPirsingPoint; // радиус точки пробивки
    private int rSizePoint; // радиус размерной точки
    private Color colorText; // цвет текста
    private Color colorMovement; // цвет перемещения (резка)
    private Color colorVacantMovement; // цвет перемещения (холостой переход)
    private Color colorBackgroundLine; // цвет фоновой линии
    private Color colorPirsingPoint; // цвет точки пробивки
    private Color colorSizePoint; // цвет размерной точки
    private Color colorSizeLine; // цвет размерной линии
    private Color colorTable; // цвет координатной сетки
    private Color colorTableScale; // цвет координатных надписей
    private Color colorBackground; // цвет фона
    private Color colorDraw; // цвет отрисовки линий
    private int styleLine; // тип линии для отрисовки (текущий)
    private int styleTable; // тип линии для отрисовки координатной сетки
    private int styleMovement; // тип линии для отрисовки (резка)
    private int styleVacantMovement; // тип линии для отрисовки (холостой переход)
    private int styleSizeLine; // тип размерной линии
    private int styleAuxSizeLine; // тип вспомогательной размерной линии
    private float sizeHeight; // высота выделения размерных линий (в процентах 0.07)
    private Point currPosition; // текущая позиция отрисовки
    private Point prevPosition; // предыдущая позиция отрисовки

    // матетематические данные
    private float scale; // коэффициент масштабирования при отрисовке
    private Point shapeSize; // габариты детали (без отхода/подхода)
    private Point viewSize; // габариты детали (c отходом/подходом)
    private Point LeadIn; // вектор подхода
    private Point LeadOut; // вектор отхода
    private int distanceBetweenShapes; // расстояние между деталями
    private int exitType = EXIT_NONE; // тип выхода

    private int detailLeadInDistance; // длина подхода для внешних контуров
    private int detailLeadOutDistance; // длина отхода для внешних контуров
    private int holesLeadInDistance; // длина подхода для отверстий
    private int holesLeadOutDistance; // длина отхода для отверстий
    private int detailLeadInAngle; // угол подхода для внешних контуров
    private int detailLeadOutAngle; // угол отхода для внешних контуров
    private int holesLeadInAngle; // угол подхода для отверстий
    private int holesLeadOutAngle; // угол отхода для отверстий
    private int detailLeadInType; // тип подхода для внешних контуров
    private int detailLeadOutType; // тип отхода для внешних контуров
    private int holesLeadInType; // тип подхода для внутренних контуров
    private int holesLeadOutType; // тип отхода для внутренних контуров
    private int holesStartPointData; // номер точки пробивки для отверстий
    private int detailStartPointData; // номер точки пробивки для внешних контуров
    private int detailKerf;
    private int holesKerf;
    private int detailCutOnCommand; // значение M команды для внешнего контура
    private int holesCutOnCommand; // значение M команды для внутреннего контура
    //
    protected String macrosName; // название макроса (например "Ребро")
    protected String CPName; // название CP. Usage: %CPName, CPName.cp

    /** конструктор
     */
    public JMacros()
    {

        geoParameters = new JMacrosParameters();
        shapeList = new Vector();
        lineSizeList = new JLineSizeList();
        trajectoryList = new Vector();
        generalTrajectory = new JCPList(JCPList.SHAPE);

        shapeSize = new Point(0, 0);
        viewSize = new Point(0, 0);
        LeadIn = new Point(0, 0);
        LeadOut = new Point(0, 0);
        distanceBetweenShapes = 200;
        holesStartPointData = 1;
        detailStartPointData = 1;

        form = new JMacrosForm(this);
    }

    /********************************************************************************************************************/
    /**		ПОДГОТОВИТЕЛЬНЫЕ МЕТОДЫ		*********************************************************************************/
    /********************************************************************************************************************/

    /** загрузить геометрические параметры макроса
     *
     */
    public void LoadParameters(JMacrosParameters _macrosParameters)
    {
        geoParameters = _macrosParameters;
    }

    /** установить параметры подхода/отхода
     *
     */
    public void SetLeadInLeadOutParams(int _detailLeadInDistance, int _detailLeadOutDistance,
            int _holesLeadInDistance, int _holesLeadOutDistance,
            int _detailLeadInAngle, int _detailLeadOutAngle,
            int _holesLeadInAngle, int _holesLeadOutAngle,
            int _detailLeadInType, int _detailLeadOutType,
            int _holesLeadInType, int _holesLeadOutType,
            int _detailStartPointData, int _holesStartPointData)
    {
        // установить длины подхода/отхода для внешних контуров и отверстий
        detailLeadInDistance = _detailLeadInDistance;
        detailLeadOutDistance = _detailLeadOutDistance;
        holesLeadInDistance = _holesLeadInDistance;
        holesLeadOutDistance = _holesLeadOutDistance;

        detailLeadInAngle = _detailLeadInAngle;
        detailLeadOutAngle = _detailLeadOutAngle;
        holesLeadInAngle = _holesLeadInAngle;
        holesLeadOutAngle = _holesLeadOutAngle;

        detailLeadInType = _detailLeadInType;
        detailLeadOutType = _detailLeadOutType;
        holesLeadInType = _holesLeadInType;
        holesLeadOutType = _holesLeadOutType;

        detailStartPointData = _detailStartPointData;
        holesStartPointData = _holesStartPointData;
    }

    public void setDetailKerf(int kerf)
    {
        this.detailKerf = kerf;
    }

    public void setHolesKerf(int kerf)
    {
        this.holesKerf = kerf;
    }

    public void setDetailCutOnCommand(int cutOnCommand)
    {
        this.detailCutOnCommand = cutOnCommand;
    }

    public void setHolesCutOnCommand(int cutOnCommand)
    {
        this.holesCutOnCommand = cutOnCommand;
    }

    public void setExitType(int exitType)
    {
        this.exitType = exitType;
    }

    public void setDistanceBetweenShapes(int distanceBetweenShapes)
    {
        this.distanceBetweenShapes = distanceBetweenShapes;
    }

    public int getDistanceBetweenShapes()
    {
        return distanceBetweenShapes;
    }

    /********************************************************************************************************************/
    /**		АБСТРАКТНЫЕ МЕТОДЫ		*************************************************************************************/
    /********************************************************************************************************************/

    /** задать геометрические параметры макроса
     *
     */
    public abstract void InitGeoParameters();

    /** проверка на правильность заполнения размеров детали
     *
     * @return false если размеры заданв не верно
     */
    public abstract boolean CheckForCorrectSize();

    /** задать дополнительные параметры макроса
     *
     */
    public abstract void InitAdditionalParameters();

    /**Заполнить список контуров макроса на основании геометрических параметров.
     * <p><i>Внешний контур (макрос может содержать только один внешний контур)
     * нужно заполнять в самом конце (чтобы он был последний в списке контуров).</i>
     *
     */
    public abstract void FillShapeList();

    /** заполнить список линейных размеров макроса на основании геометрических параметров
     *
     */
    public abstract void FillLineSizeList();

    /********************************************************************************************************************/
    /**		ОСНОВНЫЕ МЕТОДЫ		*****************************************************************************************/
    /********************************************************************************************************************/

    /** подготовить макрос к работе
     *
     */
    public void Init()
    {

        // задать дополнительные параметры макроса
        InitAdditionalParameters();
        // задать геометрические параметры макроса
        InitGeoParameters();
        // заполнить список контуров макроса на основании геометрических параметров
        FillShapeList();
        // заполнить список линейных размеров макроса на основании геометрических параметров
        FillLineSizeList();
    }

    public void OpenForm()
    {
        form.open();
    }

    /**Скопировать содержимое одного листа траекторий в другой.
     * @param src
     * @param dest
     */
    public static void copyList(List src, List dest)
    {
        for (int i = 0; i < src.size(); i++)
        {
            JCPList srcList = (JCPList)src.get(i);
            JCPList trgList = new JCPList(srcList.getType());
            trgList.setMovementToULC(srcList.getMovementToULC());
            trgList.setMovementFromLeadInToULC(srcList.getMovementFromLeadInToULC());
            trgList.SetMovementFromLeadOutToULC(srcList.getMovementFromLeadOutToULC());
            for (int j = 0; j < srcList.getLength(); j++)
            {
                trgList.pushBack(srcList.GetCC(j));
            }
            dest.add(trgList);
        }
    }

//    protected void addKerf()
//    {
//        if (trajectoryList.size() == 1 && detailKerf == 0)
//            return;
//        if (trajectoryList.size() > 1 && detailKerf == 0 && holesKerf == 0)
//            return;
//
//        JCPList cpList;
//
//        if (trajectoryList.size() > 1)
//        {
//            if (holesKerf != 0)
//            {
//                cpList = ((JCPList)trajectoryList.get(0));
//                cpList.add(1, new JCC(JCC.D, new int[] {holesKerf}, "holesKerf"));
//            }
//        }
//
//        for (int i = 0; i < trajectoryList.size(); i++)
//        {
//            cpList = ((JCPList)trajectoryList.get(i));
//
//            if (i == trajectoryList.size() - 1)
//            {
//                // внешний контур
//                if (detailKerf == 0)
//                    continue;
//                cpList.add(1, new JCC(JCC.D, new int[] {detailKerf}, "detailKerf"));
//            }
//            else
//            {
//                if (holesKerf == 0)
//                    continue;
//            }
//
//            for (int j = 0; j < cpList.getLength(); j++)
//            {
//
//                if (cpList.getCCType(j) == JCC.Arc)
//                {
//                    int[] data = cpList.getData(j);
//                    if (data[4] == -1)
//                    {
//                        // по ЧС
//                        cpList.add(j, new JCC(JCC.G, new int[] {41}, "G41"));
//                    }
//                    else if (data[4] == 1)
//                    {
//                        cpList.add(j, new JCC(JCC.G, new int[] {42}, "G42"));
//                    }
//                }
//            }
//        }
//    }

    /** сгенерировать список траекторий на основании списка контуров
     *
     */
    protected void FillTrajectoryList()
    {
        Point currPosition = new Point(0, 0);
        int shapeType = 0;

        int[] data = new int[5];
        int i = 0, j = 0, k = 0;
        boolean hasDCommand = false;

        for (i = 0; i < shapeList.size(); i++)
            ((JCPList)shapeList.get(i)).removeVoidMovements();

        distanceBetweenShapes = form.getDistanceBetweenDetails();//geoParameters.GetValue(DISTANCE_BETWEEN_SHAPES);
        exitType = form.getExitType();

        trajectoryList = (Vector)shapeList.clone();// Achtung!!!;
        checkList = new Vector();
        copyList(shapeList, checkList);

        JCPList CPListSource; // текущий контур
        JCC CCLead; // подход или отход

        for (i = 0; i < trajectoryList.size(); i++)
        {

            currPosition.x = 0;
            currPosition.y = 0;

            data = new int[2];
            data[0] = 0;
            data[1] = 0;
            CCLead = new JCC(JCC.Line, data, "");

            CPListSource = ((JCPList)trajectoryList.get(i));
            shapeType = CPListSource.getType();

            for (j = 0; j < CPListSource.getLength(); j++)
            {

                if (CPListSource.getCCType(j) == JCC.Line)
                {
                    data = new int[2];
                    data = CPListSource.getData(j);
                    currPosition.x += data[0];
                    currPosition.y += data[1];
                }
                if (CPListSource.getCCType(j) == JCC.Arc)
                {
                    data = new int[5];
                    data = CPListSource.getData(j);
                    currPosition.x += data[0];
                    currPosition.y += data[1];
                }

                if (CPListSource.getCCType(j) == JCC.StartPoint)
                    if (shapeType == JCPList.SHAPE)
                    {
                        if (CPListSource.getData(j)[0] == detailStartPointData)
                        {
                            k = j;
                            break;
                        }
                    }
                    else if (CPListSource.getData(j)[0] == holesStartPointData)
                    {
                        k = j;
                        break;
                    }

                // все команды до StartPoint переносим в конец списка
                CPListSource.pushBack(CPListSource.GetCC(j));
            }

            for (j = 0; j < k; j++)
                // удалить мусор
                CPListSource.remove(0);

            JCC movementAfterSP = CPListSource.getFirstMovementAfterPosition(0);
            JCC movementBeforeSP = CPListSource.getFirstMovementBeforePosition(0);
            if (movementBeforeSP == null || movementAfterSP == null)
            {
                //TODO bad solution. It ought to take info.
                System.err.println("Error! movement after/before SP is null");
                return;
            }

            if (shapeType == JCPList.HOLE)
            { //--------------------------------------------------------------------------------------------
                // для отверстий
                // отход
                switch (holesLeadOutType)
                {
                case JMacros.LEAD_Tangent:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = TangentToLine(data, holesLeadOutDistance));
                        //CPListSource.PushBack(k + 1, new JCC(JCC.Line, data, "HOLE " + k + " LeadOut"));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = TangentToArc(data, holesLeadOutDistance, false, false));
                    }
                    break;
                case JMacros.LEAD_Normal:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = NormalToLine(data, holesLeadOutDistance, false));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = NormalToArc(data, holesLeadOutDistance, false, false));
                    }
                    break;
                case JMacros.LEAD_Arc:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = ArcToLine(data, holesLeadOutDistance, holesLeadOutAngle, false));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = ArcToArc(data, holesLeadOutDistance, holesLeadOutAngle, false, false));
                    }
                    break;
                case JMacros.LEAD_None:

                    break;
                default:
                    try
                    {
                        throw new Exception("ERROR! Default item in use! JMacros.FillTrajectoryList() holes LeadOut");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (holesLeadOutType != JMacros.LEAD_None)
                        CPListSource.setLeadOut(CCLead);
                }

                // выключить эквидистанту
                data = new int[] {40};
                //CPListSource.pushBack(new JCC(JCC.G, data, "G40"));
                int pos = CPListSource.getLength() - 1;
                if (holesLeadOutType == JMacros.LEAD_None)
                    pos++;
                CPListSource.add(pos, new JCC(JCC.G, data, "G40"));

                // выключить рез
                data = new int[1];
                data[0] = 74;
                CPListSource.pushBack(new JCC(JCC.M, data, ""));

                LeadOut.x = CCLead.GetData()[0];
                LeadOut.y = CCLead.GetData()[1];

                data = new int[2];
                data[0] = 0;
                data[1] = 0;
                CCLead = new JCC(JCC.Line, data, "");

                // подход
                switch (holesLeadInType)
                {
                case JMacros.LEAD_Tangent:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = TangentToLine(data, holesLeadInDistance));
                        //CPListSource.Push(k + 1, new JCC(JCC.Line, data, "HOLE " + k + " LeadIn"));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = TangentToArc(data, holesLeadInDistance, true, false));
                    }
                    break;
                case JMacros.LEAD_Normal:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = NormalToLine(data, holesLeadInDistance, true));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = NormalToArc(data, holesLeadInDistance, true, false));
                    }
                    break;
                case JMacros.LEAD_Arc:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = ArcToLine(data, holesLeadInDistance, holesLeadInAngle, true));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = ArcToArc(data, holesLeadInDistance, holesLeadInAngle, true, false));
                    }
                    break;
                case JMacros.LEAD_None:

                    break;
                default:
                    try
                    {
                        throw new Exception("ERROR! Default item in use! JMacros.FillTrajectoryList() holes LeadIn");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    CPListSource.setLeadIn(CCLead);
                }

                // включить рез
                data = new int[1];
                data[0] = holesCutOnCommand;
                CPListSource.push(new JCC(JCC.M, data, ""));

                data = new int[] {41};
                //CPListSource.push(new JCC(JCC.G, data, "G41"));
                CPListSource.add(2, new JCC(JCC.G, data, "G41"));

                if (holesKerf != 0 && hasDCommand == false)
                {
                    hasDCommand = true;
                    data = new int[] {holesKerf};
                    CPListSource.push(new JCC(JCC.D, data, "D"));
                }

                LeadIn.x = CCLead.GetData()[0];
                LeadIn.y = CCLead.GetData()[1];

            }
            else
            { //-------------------------------------------------------------------------------------------------------------
                // для внешних контуров

                // вычислить габариты внешнего контура (без подхода/отхода)
                shapeSize = CPListSource.calculateShapeSize();

                // отход
                switch (detailLeadOutType)
                {
                case JMacros.LEAD_Tangent:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = TangentToLine(data, detailLeadOutDistance));
                        //CPListSource.PushBack(k + 1, new JCC(JCC.Line, data, "HOLE " + k + " LeadOut"));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = TangentToArc(data, detailLeadOutDistance, false, true));
                    }
                    break;
                case JMacros.LEAD_Normal:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = NormalToLine(data, detailLeadOutDistance, false));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = NormalToArc(data, detailLeadOutDistance, false, true));
                    }
                    break;
                case JMacros.LEAD_Arc:
                    if (movementBeforeSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = ArcToLine(data, detailLeadOutDistance, detailLeadOutAngle, false));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementBeforeSP.GetData();
                        CPListSource.pushBack(CCLead = ArcToArc(data, detailLeadOutDistance, detailLeadOutAngle, false, true));
                    }
                    break;
                case JMacros.LEAD_None:

                    break;
                default:
                    try
                    {
                        throw new Exception("ERROR! Default item in use! JMacros FillTrajectoryList() shape LeadOut");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if (holesLeadOutType != JMacros.LEAD_None)
                        CPListSource.setLeadOut(CCLead);
                }

                // выключить эквидистанту
                data = new int[] {40};
                //CPListSource.pushBack(new JCC(JCC.G, data, "G40"));
                int pos = CPListSource.getLength() - 1;
                if (detailLeadOutType == JMacros.LEAD_None)
                    pos++;
                CPListSource.add(pos, new JCC(JCC.G, data, "G40"));

                // выключить рез
                data = new int[1];
                data[0] = 74;
                CPListSource.pushBack(new JCC(JCC.M, data, ""));

                LeadOut.x = CCLead.GetData()[0];
                LeadOut.y = CCLead.GetData()[1];

                data = new int[2];
                data[0] = 0;
                data[1] = 0;
                CCLead = new JCC(JCC.Line, data, "");

                // подход
                switch (detailLeadInType)
                {
                case JMacros.LEAD_Tangent:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = TangentToLine(data, detailLeadInDistance));
                        //CPListSource.Push(k + 1, new JCC(JCC.Line, data, "HOLE " + k + " LeadIn"));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = TangentToArc(data, detailLeadInDistance, true, true));
                    }
                    break;
                case JMacros.LEAD_Normal:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = NormalToLine(data, detailLeadInDistance, true));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = NormalToArc(data, detailLeadInDistance, true, true));
                    }
                    break;
                case JMacros.LEAD_Arc:
                    if (movementAfterSP.type == JCC.Line)
                    {
                        data = new int[2];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = ArcToLine(data, detailLeadInDistance, detailLeadInAngle, true));
                    }
                    else
                    {
                        data = new int[5];
                        data = movementAfterSP.GetData();
                        CPListSource.push(CCLead = ArcToArc(data, detailLeadInDistance, detailLeadInAngle, true, true));
                    }
                    break;
                case JMacros.LEAD_None:

                    break;
                default:
                    try
                    {
                        throw new Exception("ERROR! Default item in use! JMacros FillTrajectoryList() shape LeadIn");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    CPListSource.setLeadIn(CCLead);
                }

                // включить рез
                data = new int[1];
                data[0] = detailCutOnCommand;
                CPListSource.push(new JCC(JCC.M, data, ""));

                data = new int[] {41};
                //CPListSource.push(new JCC(JCC.G, data, "G41"));
                CPListSource.add(2, new JCC(JCC.G, data, "G41"));

                if (holesKerf != 0 || (holesKerf == 0 && detailKerf != 0))
                {
                    data = new int[] {detailKerf};
                    CPListSource.push(new JCC(JCC.D, data, "D"));
                }

                LeadIn.x = CCLead.GetData()[0];
                LeadIn.y = CCLead.GetData()[1];

            } //---------------------------------------------------------------------------------------------------------------------

            // расчитать расстояния от точки подхода и отхода до ULC
            CPListSource.setMovementFromLeadInToULC(CPListSource.getMovementToULC().x - currPosition.x + LeadIn.x, CPListSource.getMovementToULC().y - currPosition.y + LeadIn.y);
            CPListSource.setMovementFromLeadOutToULC(CPListSource.getMovementToULC().x - currPosition.x - LeadOut.x, CPListSource.getMovementToULC().y - currPosition.y - LeadOut.y);
        }

        // вычислить габариты внешнего контура (с подходом/отходом)
        CPListSource = ((JCPList)trajectoryList.get(trajectoryList.size() - 1));
        viewSize = CPListSource.calculateShapeSize();

        CreateGeneralTrajectory();
    }

    /** формирование общей траектории на основании списка траекторий
     *
     */
    protected void CreateGeneralTrajectory()
    {
        int[] data;
        int i = 0, j = 0;

        JCPList CPListSource;
        JCPList CPListSourcePrev;

        for (i = 0; i < trajectoryList.size() - 1; i++)
        {

            CPListSourcePrev = ((JCPList)trajectoryList.get(i));
            CPListSource = ((JCPList)trajectoryList.get(i + 1));

            for (j = 0; j < CPListSourcePrev.getLength(); j++)
            {
                // копировать траектории в главную траекторию
                if (CPListSourcePrev.getCCType(j) == JCC.Line)
                {
                    data = new int[1];
                    data[0] = 1;
                    generalTrajectory.pushBack(new JCC(JCC.G, data, "G" + data));
                }
                generalTrajectory.pushBack(CPListSourcePrev.GetCC(j));
            }

            // сформировать переход между контурами
            data = new int[1];
            data[0] = 0;
            generalTrajectory.pushBack(new JCC(JCC.G, data, ""));
            data = new int[2];
            data[0] = CPListSourcePrev.getMovementFromLeadOutToULC().x - CPListSource.getMovementFromLeadInToULC().x;
            data[1] = CPListSourcePrev.getMovementFromLeadOutToULC().y - CPListSource.getMovementFromLeadInToULC().y;
            generalTrajectory.pushBack(new JCC(JCC.Line, data, ""));
        }

        CPListSource = ((JCPList)trajectoryList.get(trajectoryList.size() - 1));
        for (j = 0; j < CPListSource.getLength(); j++)
        {
            // копировать последнюю траекторию (она же внешний контур) в главную траекторию
            if (CPListSource.getCCType(j) == JCC.Line)
            {
                data = new int[1];
                data[0] = 1;
                generalTrajectory.pushBack(new JCC(JCC.G, data, ""));
            }
            generalTrajectory.pushBack(CPListSource.GetCC(j));
        }

        // выход
        data = new int[2];
        switch (exitType)
        {
        case EXIT_Y:
            data[0] = CPListSource.getMovementFromLeadOutToULC().x - distanceBetweenShapes;
            data[1] = CPListSource.getMovementFromLeadOutToULC().y + shapeSize.y;
            break;
        case EXIT_X:
            data[0] = CPListSource.getMovementFromLeadOutToULC().x + shapeSize.x;
            data[1] = CPListSource.getMovementFromLeadOutToULC().y - distanceBetweenShapes;
            break;
        case EXIT_TO_START_POINT:
            data[0] = CPListSource.getMovementFromLeadOutToULC().x - distanceBetweenShapes;
            data[1] = CPListSource.getMovementFromLeadOutToULC().y - distanceBetweenShapes;
            break;
        }
        if ((data[0] != 0) || (data[1] != 0))
        {
            int[] G = new int[1];
            G[0] = 0;
            generalTrajectory.pushBack(new JCC(JCC.G, G, ""));
        }
        if (exitType != EXIT_NONE)
            generalTrajectory.pushBack(new JCC(JCC.Line, data, ""));

        // заход
        CPListSource = ((JCPList)trajectoryList.get(0));
        data = new int[2];
        data[0] = distanceBetweenShapes - CPListSource.getMovementFromLeadInToULC().x;
        data[1] = distanceBetweenShapes - CPListSource.getMovementFromLeadInToULC().y;
        generalTrajectory.push(new JCC(JCC.Line, data, ""));
        if ((data[0] != 0) || (data[1] != 0))
        {
            int[] G = new int[1];
            G[0] = 0;
            generalTrajectory.push(new JCC(JCC.G, G, ""));
        }

        generalTrajectory.pushBack(new JCC(JCC.M, new int[] {2}, "M02"));
    }

    /** генерация УП на основании заданных параметров
     *
     */
    public void GenerateCP(String cpName)
    {
        if (cpName == null)
            cpName = OUTPUT_CP_NAME;
        // заполнение уп листа уп командами, на основании типа макроса и параметров, заданных пользователем
        try
        {
            File file = new File(OUTPUT_DIR);
            if (!file.isDirectory())
                file.mkdir();
            //OutputStream f = new FileOutputStream(OUTPUT_DIR + "/" + CPName + OUTPUT_EXTENSION);
            OutputStream f = new FileOutputStream(OUTPUT_DIR + "/" + cpName + OUTPUT_EXTENSION);
            f.write(("%" + CPName + "\n").getBytes());

            for (int i = 0; i < generalTrajectory.getLength(); i++)
                f.write(generalTrajectory.GetCC(i).GenerateCPString().getBytes());
            f.close();

            SWTFormBase.showInfo("Управляющая программа создана успешно!");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            SWTFormBase.showError("Управляющая программа не создана!\n" +
                    "(Ошибка записи файла программы)");
        }
    }

    /** отрисовать макрос в image
     *
     */
    public void DrawMacros()
    {
        // масштаб
        // с подходом/отходом
        scale = Math.max((float)(viewSize.y + distanceBetweenShapes) / (form.labelView.getBounds().width - maxLineSizeHeight),
                (float)(viewSize.x + distanceBetweenShapes) / (form.labelView.getBounds().height - maxLineSizeHeight));
        // без подхода/отхода
        //scale = Math.max((float)(shapeSize.y + distanceBetweenShapes)/form.labelView.getBounds().width, (float)(shapeSize.x + distanceBetweenShapes)/form.labelView.getBounds().height);
        scale *= 1.02;

        //рисование от края экрана
        imageView = new Image(form.display, form.labelView.getBounds().width, form.labelView.getBounds().height);
        //рисование по центру экрана
        //imageView = new Image( form.display, Math.round((shapeSize.y + distanceBetweenShapes)/scale), Math.round((shapeSize.x + distanceBetweenShapes)/scale) );
        gc = new GC(imageView);

        // включить антиалиазинг (сглаживание линий)
        gc.setAntialias(SWT.ON);

        gc.setBackground(colorBackground);

        // подготовить фон
        gc.fillRectangle(0, 0, imageView.getBounds().width, imageView.getBounds().height);

        DrawTable(drift, drift);

        // отрисовать размерные линии
        DrawSizes(1, styleAuxSizeLine, scale);

        currPosition = new Point(Math.round(drift * scale), Math.round(drift * scale));
        prevPosition = new Point(Math.round(drift * scale), Math.round(drift * scale));

        for (int i = 0; i < generalTrajectory.getLength(); i++)
            DrawCommand(generalTrajectory, i, scale);

        currPosition.x = Math.round(drift * scale);
        currPosition.y = Math.round(drift * scale);

        for (int i = 0; i < generalTrajectory.getLength(); i++)
            DrawPoints(generalTrajectory, i, scale);

        //DrawText("МАСШТАБ  1 : " + String.valueOf( (float)Math.round(scale*100)/100), imageView.getBounds().width/2 - 70, imageView.getBounds().height - 30, false);

        gc.dispose();

        form.LoadImage(imageView);
    }

    protected boolean checkMacrosGeometry()
    {
        Region[] regions = new Region[checkList.size()];

        for (int i = checkList.size() - 1; i >= 0; i--)
        {
            Vector poly = new Vector(50);
            JCPList shape = (JCPList)checkList.get(i);
            Point currPoint = new Point(-shape.getMovementToULC().x, -shape.getMovementToULC().y);

            for (int j = 0; j < shape.getLength(); j++)
            {
                JCC cc = shape.GetCC(j);
                int[] data = cc.GetData();
                int type = cc.type;

                switch (type)
                {
                case JCC.Line:
                    currPoint.x += data[0];
                    currPoint.y += data[1];
                    poly.add(new Integer(currPoint.x));
                    poly.add(new Integer(currPoint.y));
                    if (i < checkList.size() - 1)
                        if (regions[checkList.size() - 1].contains(currPoint.x, currPoint.y) == false)
                            return false;
                    break;
                case JCC.Arc:
                    Point endPoint = new Point(data[0], data[1]);
                    Point centerPoint = new Point(data[2], data[3]);
                    Point stepPoint = new Point(0, 0);

                    ArcInterpolator arcInterpolator = new ArcInterpolator(data[4], endPoint, centerPoint);
                    double sizeFactor = (double)arcInterpolator.getRemainedS() / 1000;
                    if (sizeFactor < 1)
                        sizeFactor = 1;

                    while (arcInterpolator.getRemainedS() > 0)
                    {
                        // разбиваем дугу на множество отрезков; перебираем их
                        arcInterpolator.doStep(Math.round(sizeFactor * ARC_FEED), stepPoint);
                        currPoint.x += stepPoint.x;
                        currPoint.y += stepPoint.y;
                        poly.add(new Integer(currPoint.x));
                        poly.add(new Integer(currPoint.y));
                        if (i < checkList.size() - 1)
                            if (regions[checkList.size() - 1].contains(currPoint.x, currPoint.y) == false)
                                return false;
                    }
                    break;
                }
            }
            int[] polyLine = new int[poly.size()];
            for (int k = 0; k < poly.size(); k++)
                polyLine[k] = ((Integer)poly.get(k)).intValue();
            regions[i] = new Region();
            regions[i].add(polyLine);
        }

//        int[] polyLine = new int[poly.size()];
//        for (int i = 0; i < poly.size(); i++)
//        {
//            polyLine[i] = ((Integer)poly.get(i)).intValue();
//            if (i % 2 == 0)
//                System.out.println(((Integer)poly.get(i)).intValue() + "\t" + ((Integer)poly.get(i+1)).intValue());
//        }
//        Point point = new Point(409, 201);
//        Region region = new Region();
//        region.add(polyLine);
//        Boolean contains = new Boolean(region.contains(point.x, point.y));
//        Image image = new Image(Display.getCurrent(), 700, 700);
//        GC gc = new GC(image);
//        gc.drawText(contains.toString(), 0, 0);
//        gc.drawPolyline(polyLine);
//        gc.setBackground(AGMStyledSWTForm.COLOR_RED);
//        gc.fillOval(point.x - Math.round(6 / 2), point.y - Math.round(6 / 2), 6, 6);
//        gc.dispose();
//
//        form.labelView.setImage(image);

        return true;
    }

    /********************************************************************************************************************/
    /**		ВСПОМОГАТЕЛЬНЫЕ РИСОВАЛЬНЫЕ ФУНКЦИИ		*********************************************************************/
    /********************************************************************************************************************/

    /** отрисовка одного линейного перемещения
     *
     */
    void DrawLine(int _x1, int _y1, int _x2, int _y2, Color _clr, int _lineStyle)
    {
        gc.setLineWidth(lineWidthExt);
        gc.setLineStyle(SWT.LINE_SOLID);
        gc.setForeground(colorBackgroundLine);
        gc.drawLine(_x1, _y1, _x2, _y2);
        gc.setLineWidth(lineWidthInt);
        gc.setLineStyle(_lineStyle);
        gc.setForeground(_clr);
        gc.drawLine(_x1, _y1, _x2, _y2);
    }

    /** отрисовка размерных линий
     *
     * @param _x1
     * @param _y1
     * @param _x2
     * @param _y2
     * @param _lineStyle
     * @param _lineWidth
     */
    void DrawSizeLine(int _x1, int _y1, int _x2, int _y2, int _lineStyle, int _lineWidth)
    {
        gc.setLineWidth(_lineWidth);
        gc.setLineStyle(_lineStyle);
        gc.setForeground(colorSizeLine);
        gc.drawLine(_x1, _y1, _x2, _y2);
    }

    /** отрисовка текста заданным образом
     *
     * @param _text
     * @param _x
     * @param _y
     * @param _transparent
     */
    void DrawText(String _text, int _x, int _y, boolean _transparent)
    {
        gc.setForeground(colorText);
        gc.setBackground(colorBackground);
        gc.setFont(font);
        gc.drawText(_text, _x, _y, _transparent);
    }

    /** отрисовка точки заданным образом
     * x, y - центральные точки
     * @param _x
     * @param _y
     * @param _size
     * @param _clr
     */
    void DrawPoint(int _x, int _y, int _size, Color _clr)
    {
        gc.setBackground(_clr);
        gc.fillOval(_x - Math.round(_size / 2), _y - Math.round(_size / 2), _size, _size);

    }

    /** отрисовка стрелки, заданной длины по направлению вектора (x1,y1), в точке (x2,y2)
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param length
     */
    protected void DrawArrow(int x1, int y1, int x2, int y2, int length)
    {
        if (length <= 0)
            return;
        Point antiArrowVector = new Point((int)Math.round(x1 * (1 - length / Math.sqrt(x1 * x1 + y1 * y1))) - x1, (int)Math.round(y1 * (1 - length / Math.sqrt(x1 * x1 + y1 * y1))) - y1);
        Point p1 = RotateCoords(Math.PI / 8, antiArrowVector.x, antiArrowVector.y, true);
        Point p2 = RotateCoords(Math.PI / 8, antiArrowVector.x, antiArrowVector.y, false);
        int[] arrow = { x2, y2, p1.x + x2, p1.y + y2, p2.x + x2, p2.y + y2 };
        gc.setBackground(colorSizeLine);
        gc.fillPolygon(arrow);
    }

    /** отрисовка координатной сетки
     *
     * @param _x
     * @param _y
     */
    protected void DrawTable(int _x, int _y)
    {
        Point cellCount = new Point(0, 0);
        int realDivision = 0;
        int Division = 0;
        int step = 0;
        int min = Integer.MAX_VALUE;
        int maxViewSize = 0;
        Point GCSize = new Point(Math.round(imageView.getBounds().width), Math.round(imageView.getBounds().height));
        if (GCSize.y < GCSize.x)
            maxViewSize = GCSize.x;
        else
            maxViewSize = GCSize.y;
        realDivision = Math.round(maxViewSize * scale / maxCellCount);

        int k = 0;
        int i;
        for (i = 0; i < GRADUATION.length; i++)
            if (Math.abs(realDivision - GRADUATION[i]) < min)
            {
                min = Math.abs(realDivision - GRADUATION[i]);
                k = i;
            }
        Division = GRADUATION[k];

        cellCount.y = Math.round(GCSize.x * scale / Division);
        cellCount.x = Math.round(GCSize.y * scale / Division);
        step = Math.round(Division / scale);
        // пересчет масштаба для точного совпадения размерной сетки и чертежа макроса (устранение эффекта округления)
        scale = (float)Division / step;

        gc.setLineStyle(styleTable);

        for (i = 0; i < cellCount.x + 1; i++)
        {
            // горизонтальные
            gc.setForeground(colorTable);
            gc.drawLine(_y, _x + step * i, GCSize.x, _x + step * i);
            gc.setForeground(colorTableScale);
            gc.drawText(String.valueOf(0 + Division * i), _y + 5, _x + 5 + step * i, true);
        }
        for (i = 0; i < cellCount.y + 1; i++)
        {
            // вертикальные
            gc.setForeground(colorTable);
            gc.drawLine(_y + step * i, _x, _y + step * i, GCSize.y);
            gc.setForeground(colorTableScale);
            gc.drawText(String.valueOf(0 + Division * i), _y + 5 + step * i, _x + 5, true);
        }
    }

    /** отрисовка размерных линий
     *
     * @param _lineWidth
     * @param _lineStyle
     * @param _scale
     */
    void DrawSizes(int _lineWidth, int _lineStyle, float _scale)
    {

        Point line1 = new Point(0, 0);
        Point line2 = new Point(0, 0);
        //Point curPosition	= new Point(0, 0);
        JLineSize lineSize;

        int delta = Math.round(sizeHeight * Math.min(imageView.getBounds().height, imageView.getBounds().width));
        if (delta / scale < minLineSizeHeight)
            delta = Math.round(minLineSizeHeight * scale);
        if (delta / scale > maxLineSizeHeight)
            delta = Math.round(maxLineSizeHeight * scale);

        gc.setLineWidth(_lineWidth);

        for (int i = 0; i < lineSizeList.GetLength(); i++)
        {
            gc.setForeground(colorSizeLine);
            gc.setLineStyle(_lineStyle);
            Point curPosition = new Point(distanceBetweenShapes + Math.round(drift * _scale), distanceBetweenShapes + Math.round(drift * _scale));
            lineSize = lineSizeList.GetLineSize(i);
            lineSize.CalculateLines(shapeSize, delta, line1, line2);
            {
                int textExtent;
                if (lineSize.GetPosition() == JLineSize.POS_UP || lineSize.GetPosition() == JLineSize.POS_DOWN)
                    textExtent = gc.textExtent(lineSize.GetDescription()).x;
                else
                    textExtent = gc.textExtent(lineSize.GetDescription()).y;

                if (MathUtils.length((curPosition.y - lineSize.GetPoint1().y + line1.y / _scale),
                        (curPosition.x - lineSize.GetPoint1().x + line1.x / _scale),
                        (curPosition.y - lineSize.GetPoint2().y + line2.y / _scale),
                        (curPosition.x - lineSize.GetPoint2().x + line2.x / _scale)) / scale < 2 * textExtent)
                    continue;
            }
            gc.drawLine(Math.round((curPosition.y - lineSize.GetPoint1().y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint1().x) / _scale),
                    Math.round((curPosition.y - lineSize.GetPoint1().y + line1.y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint1().x + line1.x) / _scale));
            gc.drawLine(Math.round((curPosition.y - lineSize.GetPoint2().y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint2().x) / _scale),
                    Math.round((curPosition.y - lineSize.GetPoint2().y + line2.y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint2().x + line2.x) / _scale));
            DrawLineWithArrows(Math.round((curPosition.y - lineSize.GetPoint1().y + line1.y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint1().x + line1.x) / _scale),
                    Math.round((curPosition.y - lineSize.GetPoint2().y + line2.y) / _scale),
                    Math.round((curPosition.x - lineSize.GetPoint2().x + line2.x) / _scale),
                    lineSize.GetDescription(), styleSizeLine, _lineWidth);
        }
    }

    /**	Нарисовать линию, заданного стиля и толщины на краях которой
     * распологаются стрелки по направлению (антинаправлению) прямой
     * выводит по середине надпись text
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param text
     * @param lineStyle
     * @param lineWidth
     */
    void DrawLineWithArrows(int x1, int y1, int x2, int y2, String text, int lineStyle, int lineWidth)
    {

        gc.setLineWidth(lineWidth);
        gc.setLineStyle(lineStyle);
        gc.setForeground(colorSizeLine);

        gc.drawLine(x1, y1, x2, y2);
        DrawArrow(x1 - x2, y1 - y2, x1, y1, ARROW_LENGTH);
        DrawArrow(x2 - x1, y2 - y1, x2, y2, ARROW_LENGTH);
        int textExtentX = gc.textExtent(text).x;
        int textExtentY = gc.textExtent(text).y;

        DrawText(text, Math.round((x1 + x2) / 2) - Math.round(textExtentX / 2),
                Math.round((y1 + y2) / 2) - Math.round(textExtentY / 2), false);
    }

    /** отрисовка одной команды
     *
     * @param _CPList
     * @param _position
     * @param _scale
     */
    void DrawPoints(JCPList _CPList, int _position, float _scale)
    {

        ArcInterpolator arcInterpolator;

        int[] data;
        int type;

        type = _CPList.GetCC(_position).type;

        switch (type)
        {
        case JCC.Line:
            //x, y
            data = new int[2];
            data = _CPList.GetCC(_position).GetData();
            currPosition.x += data[0];
            currPosition.y += data[1];
            // отрисовка конечной точки перемещения
            //DrawPoint(Math.round(currPosition.y/_scale), Math.round(currPosition.x/_scale), rDrawPoint, colorDrawPoint);
            break;
        case JCC.Arc:
            //x, y, i, j, direction = {1, -1} 1 - по часовой
            data = new int[5];
            data = _CPList.GetCC(_position).GetData();
            Point endPoint = new Point(data[0], data[1]);
            Point centerPoint = new Point(data[2], data[3]);
            Point stepPoint = new Point(0, 0);

            arcInterpolator = new ArcInterpolator(data[4], endPoint, centerPoint);
            double sizeFactor = (double)arcInterpolator.getRemainedS() / 1000;
            if (sizeFactor < 1)
                sizeFactor = 1;

            while (arcInterpolator.getRemainedS() > 0)
            {
                // разбиваем дугу на множество отрезков; перебираем их
                arcInterpolator.doStep(Math.round(sizeFactor * ARC_FEED), stepPoint);
                currPosition.x += stepPoint.x;
                currPosition.y += stepPoint.y;
            }
            // отрисовка конечной точки перемещения
            //DrawPoint(Math.round(currPosition.y/_scale), Math.round(currPosition.x/_scale), rDrawPoint, colorDrawPoint);
            break;
        case JCC.Fast:
            break;
        case JCC.D:
            break;
        case JCC.M:
            break;
        case JCC.R:
            break;
        case JCC.StartPoint:
            data = new int[0];
            data = _CPList.GetCC(_position).GetData();
            DrawPoint(Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), rPirsingPoint, colorPirsingPoint);
            DrawText(String.valueOf(data[0]), Math.round(currPosition.y / _scale) + 5, Math.round(currPosition.x / _scale) + 5, true);
            break;
        default:
        }
    }

    /** отрисовка одной команды
     *
     * @param _CPList
     * @param _position
     * @param _scale
     */
    void DrawCommand(JCPList _CPList, int _position, float _scale)
    {

        ArcInterpolator arcInterpolator;

        int[] data;
        int type;

        type = _CPList.GetCC(_position).type;

        switch (type)
        {
        case JCC.Line:
            //x, y
            data = new int[2];
            data = _CPList.GetCC(_position).GetData();
            currPosition.x += data[0];
            currPosition.y += data[1];

            // отрисовка перемещения
            DrawLine(Math.round(prevPosition.y / _scale), Math.round(prevPosition.x / _scale), Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), colorDraw, styleLine);
//            // отрисовка конечной точки перемещения
//            DrawPoint(Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), rDrawPoint, colorMovement);
            // отрисовка обозначения
            DrawText(_CPList.GetCC(_position).GetDescription(), (int)Math.round(0.5 * (currPosition.y + prevPosition.y) / _scale) - 12, (int)Math.round(0.5 * (currPosition.x + prevPosition.x) / _scale) - 12, false);

            prevPosition.x = currPosition.x;
            prevPosition.y = currPosition.y;
            break;
        case JCC.Arc:
            //x, y, i, j, direction = {1, -1} 1 - по часовой
            data = new int[5];
            String description = _CPList.GetCC(_position).GetDescription();
            data = _CPList.GetCC(_position).GetData();
            Point endPoint = new Point(data[0], data[1]);
            Point centerPoint = new Point(data[2], data[3]);
            Point stepPoint = new Point(0, 0);
            Point centerArcPoint = new Point(0, 0);
            Point prevPosition_tmp = new Point(prevPosition.x, prevPosition.y);

            arcInterpolator = new ArcInterpolator(data[4], endPoint, centerPoint);
            double sizeFactor = (double)arcInterpolator.getRemainedS() / 1000;
            if (sizeFactor < 1)
                sizeFactor = 1;

            long S = arcInterpolator.getRemainedS();

            boolean centerArc = false;

            while (arcInterpolator.getRemainedS() > 0)
            {
                // разбиваем дугу на множество отрезков; перебираем их
                arcInterpolator.doStep(Math.round(sizeFactor * ARC_FEED), stepPoint);

                currPosition.x += stepPoint.x;
                currPosition.y += stepPoint.y;
                if ((arcInterpolator.getRemainedS() < 3 * S / 4) && (!centerArc))
                {
                    // это условие необходимо для отрисовки размерной линии дуги
                    centerArcPoint.x = currPosition.x;
                    centerArcPoint.y = currPosition.y;
                    centerArc = true;
                }
                // отрисовка перемещения
                DrawLine(Math.round(prevPosition.y / _scale), Math.round(prevPosition.x / _scale), Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), colorDraw, styleLine);

                prevPosition.x = currPosition.x;
                prevPosition.y = currPosition.y;
            }

            // отрисовка размерной линии дуги
            if (description != "")
            {
                {
                    double textExtent = MathUtils.length(0, 0, gc.textExtent(description).x, gc.textExtent(description).y);

                    if (MathUtils.length(centerArcPoint.y, centerArcPoint.x,
                            prevPosition_tmp.y + centerPoint.y, prevPosition_tmp.x + centerPoint.x) / scale < 2 * textExtent)
                        break;
                }
                DrawSizeLine(Math.round((centerArcPoint.y) / _scale),
                        Math.round((centerArcPoint.x) / _scale),
                        Math.round((prevPosition_tmp.y + centerPoint.y) / _scale),
                        Math.round((prevPosition_tmp.x + centerPoint.x) / _scale), styleSizeLine, 1);

                // отрисовка стрелки на конце размерной линии
                DrawArrow(Math.round((centerArcPoint.y) / _scale) - Math.round((prevPosition_tmp.y + centerPoint.y) / _scale), Math.round((centerArcPoint.x) / _scale) - Math.round((prevPosition_tmp.x + centerPoint.x) / _scale), Math.round((centerArcPoint.y) / _scale), Math.round((centerArcPoint.x) / _scale), ARROW_LENGTH);

                // отрисовка боковых вспомогательных линий размера дуги
                DrawSizeLine(Math.round((prevPosition_tmp.y) / _scale), Math.round((prevPosition_tmp.x) / _scale), Math.round((prevPosition_tmp.y + centerPoint.y) / _scale), Math.round((prevPosition_tmp.x + centerPoint.x) / _scale), styleAuxSizeLine, 1);
                DrawSizeLine(Math.round((prevPosition_tmp.y + centerPoint.y) / _scale), Math.round((prevPosition_tmp.x + centerPoint.x) / _scale), Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), styleAuxSizeLine, 1);
                DrawText(description, (int)Math.round(0.5 * (centerArcPoint.y + prevPosition_tmp.y + centerPoint.y) / _scale) - 10, (int)Math.round(0.5 * (centerArcPoint.x + prevPosition_tmp.x + centerPoint.x) / _scale) - 10, false);
                // отрисовка опорной точки размерной линии
                DrawPoint(Math.round((prevPosition_tmp.y + centerPoint.y) / _scale), Math.round((prevPosition_tmp.x + centerPoint.x) / _scale), rSizePoint, colorSizePoint);
            }
//            // отрисовка конечной точки перемещения
//            DrawPoint(Math.round(currPosition.y / _scale), Math.round(currPosition.x / _scale), rDrawPoint, colorMovement);
            break;
        case JCC.Fast:
            break;
        case JCC.D:
            break;
        case JCC.M:
            data = new int[0];
            data = _CPList.GetCC(_position).GetData();
            if ((data[0] == 71) ||
                (data[0] == 72) ||
                (data[0] == 81) ||
                (data[0] == 82))
            {
                colorDraw = colorMovement;
                styleLine = styleMovement;
            }
            else
            {
                colorDraw = colorVacantMovement;
                styleLine = styleVacantMovement;
            }
            break;
        case JCC.R:
            break;
        case JCC.StartPoint:
            break;
        default:
        }
    }

    /********************************************************************************************************************/
    /**		ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ		*********************************************************************************/
    /********************************************************************************************************************/

    /** получить количество контуров макроса
     *
     */
    public int GetShapeCount()
    {
        return shapeList.size();
    }

    /** возвращает геометрические параметры макроса
     *
     * @return
     */
    public JMacrosParameters GetGeoParameters()
    {
        return geoParameters;
    }

    /** очистить список контуров
     *
     */
    public void ClearShapeList()
    {
        shapeList.clear();
    }

    /** очистить список линейных размеров
     *
     */
    public void ClearLineSizeList()
    {
        lineSizeList.Clear();
    }

    /** очистить список траекторий
     *
     */
    public void ClearTrajectoryList()
    {
        trajectoryList.clear();
    }

    /** очистить общую траекторию
     *
     */
    public void ClearGeneralTrajectory()
    {
        generalTrajectory.clear();
    }

    /** очистить весь макрос
     *
     */
    public void Clear()
    {
        ClearShapeList();
        ClearLineSizeList();
        ClearTrajectoryList();
        ClearGeneralTrajectory();
    }

    /** инициализировать рисовальные параметры
     *
     */
    public void InitDrawParameters()
    {
        colorPirsingPoint = new Color(form.display, 255, 150, 0);
        colorBackgroundLine = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        colorSizePoint = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        colorSizeLine = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        colorTable = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
        colorTableScale = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY);
        colorBackground = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        colorMovement = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
        colorVacantMovement = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
        colorText = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);

        colorDraw = colorVacantMovement;

        styleMovement = SWT.LINE_SOLID;
        styleVacantMovement = SWT.LINE_DOT;
        styleSizeLine = SWT.LINE_SOLID;
        styleAuxSizeLine = SWT.LINE_DOT;
        styleTable = SWT.LINE_DOT;
        styleLine = styleVacantMovement;

        drift = 5; // смещение рисования от 0 точки

        lineWidthInt_800x600 = 2;
        lineWidthExt_800x600 = 2;
        lineWidthInt_1024x768 = 2;
        lineWidthExt_1024x768 = 4;
        lineWidthInt_1280x1024 = 2;
        lineWidthExt_1280x1024 = 4;

        if (form.display.getBounds().width > 1200)
            resolution = RES_1280x1024;
        else if (form.display.getBounds().width > 1000)
            resolution = RES_1024x768;
        else
            resolution = RES_800x600;

        switch (resolution)
        {
        case RES_800x600:
//            rDrawPoint = 4;
            rPirsingPoint = 6;
            rSizePoint = 4;
            lineWidthInt = lineWidthInt_800x600;
            lineWidthExt = lineWidthExt_800x600;
            font = AGMStyledSWTForm.FONT_10B;
            maxCellCount = 10;
            sizeHeight = 0.13f;
            minLineSizeHeight *= (double)800/1280;
            maxLineSizeHeight *= (double)800/1280;
            break;
        case RES_1024x768:
//            rDrawPoint = 6;
            rPirsingPoint = 10;
            rSizePoint = 6;
            lineWidthInt = lineWidthInt_1024x768;
            lineWidthExt = lineWidthExt_1024x768;
            font = AGMStyledSWTForm.FONT_12;
            maxCellCount = 12;
            sizeHeight = 0.10f;
            minLineSizeHeight *= (double)1024/1280;
            maxLineSizeHeight *= (double)1024/1280;
            break;
        case RES_1280x1024:
            styleAuxSizeLine = SWT.LINE_DASH;
//            rDrawPoint = 8;
            rPirsingPoint = 12;
            rSizePoint = 6;
            lineWidthInt = lineWidthInt_1280x1024;
            lineWidthExt = lineWidthExt_1280x1024;
            font = AGMStyledSWTForm.FONT_14;
            maxCellCount = 20;
            sizeHeight = 0.07f;
            break;
        }
    }

    /********************************************************************************************************************/
    /**		МАТЕМАТИКА		*********************************************************************************************/
    /********************************************************************************************************************/

    /** повернуть координаты на угол(рад) считая от текущего положения
     * inversion == true - против часовой
     */
    Point RotateCoords(double radAngle, int lx, int ly, boolean inversion)
    {
        double cosAngle = Math.cos(radAngle);
        double sinAngle = Math.sin(radAngle);
        short sgn;
        if (inversion)
            sgn = -1;
        else
            sgn = 1;
        return new Point((int)Math.round(cosAngle * lx + sgn * sinAngle * ly), (int)Math.round(-sgn * sinAngle * lx + cosAngle * ly));
    }

    /** повернуть координаты на угол(рад) считая от текущего положения(Sin,Cos посчитаны)
     * inversion == true - против часовой
     *
     * @param CosAngle
     * @param SinAngle
     * @param lx
     * @param ly
     * @param inversion
     * @return
     */
    Point RotateCoords(double CosAngle, double SinAngle, int lx, int ly, boolean inversion)
    {
        short sgn;
        if (inversion)
            sgn = -1;
        else
            sgn = 1;
        return new Point((int)Math.round(CosAngle * lx + sgn * SinAngle * ly), (int)Math.round(-sgn * SinAngle * lx + CosAngle * ly));
    }

    /** вычислить угол в радианах от горизонтали до заданного вектора против ЧС
     *
     * @param _x
     * @param _y
     * @return
     */
    double CalculateAngle(int _x, int _y)
    {
        double b = 0;

        if ((_x == 0) && (_y > 0))
            b = Math.PI / 2;
        else if ((_x == 0) && (_y < 0))
            b = 3 * Math.PI / 2;
        else if ((_y == 0) && (_x > 0))
            b = 0;
        else if ((_y == 0) && (_x < 0))
            b = Math.PI;
        else if ((_x == 0) && (_y == 0))
            b = 0;
        else if ((_x < 0) && (_y < 0))
            b = Math.atan((double)_y / _x) + Math.PI;
        else if ((_x < 0) && (_y > 0))
            b = Math.atan((double)Math.abs(_x) / Math.abs(_y)) + Math.PI / 2;
        else if ((_x > 0) && (_y < 0))
            b = Math.atan((double)Math.abs(_x) / Math.abs(_y)) + 3 * Math.PI / 2;
        else
            b = Math.atan((double)_y / _x);
        return b;
    }

    /** вычисление нормали подхода/отхода по направлению к заданному перемещению
     * подход: _LeadInLeadOut - true
     * отход:  _LeadInLeadOut - false
     *
     * @param _data
     * @param _length
     * @param _LeadInLeadOut
     * @return
     */
    JCC NormalToLine(int[] _data, int _length, boolean _LeadInLeadOut)
    {

        Point normal = new Point(0, _length);

        double b = CalculateAngle(_data[0], _data[1]);

        //normal = RotateCoords(angle, normal.x, normal.y, true);
        normal = RotateCoords(b, normal.x, normal.y, true);

        int[] data = new int[2];
        if (_LeadInLeadOut)
        {
            data[0] = -normal.x;
            data[1] = -normal.y;
        }
        else
        {
            data[0] = normal.x;
            data[1] = normal.y;
        }

        return new JCC(JCC.Line, data, "");
    }

    /** вычисление нормали подхода/отхода по направлению к заданному перемещению
     * подход: _LeadInLeadOut - true
     * отход:  _LeadInLeadOut - false
     *
     * @param _data
     * @param _length
     * @param _LeadInLeadOut
     * @param _isShape
     * @return
     */
    JCC NormalToArc(int[] _data, int _length, boolean _LeadInLeadOut, boolean _isShape)
    {

        int[] data = new int[2];

        if (_LeadInLeadOut)
        {
            data[0] = _data[2];
            data[1] = _data[3];
        }
        else
        {
            data[0] = _data[0] - _data[2];
            data[1] = _data[1] - _data[3];
        }

        if (!_isShape || _data[4] == 1)//FIXME Attention2
        {
            data[0] = -data[0];
            data[1] = -data[1];
        }

        return TangentToLine(data, _length);
    }

    /** вычисление касательной подхода/отхода по направлению к заданному перемещению
     *
     * @param _data
     * @param _length
     * @return
     */
    JCC TangentToLine(int[] _data, int _length)
    {

        double L = Math.sqrt(_data[0] * _data[0] + _data[1] * _data[1]);

        int[] data = new int[2];
        data[0] = (int)Math.round(_data[0] * _length / L);
        data[1] = (int)Math.round(_data[1] * _length / L);

        return new JCC(JCC.Line, data, "");
    }

    /** вычисление касательной подхода/отхода по направлению к заданному перемещению
     *
     * @param _data
     * @param _length
     * @param _LeadInLeadOut
     * @param _isShape
     * @return
     */
    JCC TangentToArc(int[] _data, int _length, boolean _LeadInLeadOut, boolean _isShape)
    {
        int[] data = new int[2];

//        System.out.println("dir = " + _data[4]);


        if (_LeadInLeadOut)
        {
            data[0] = _data[2];
            data[1] = _data[3];
        }
        else
        {
            data[0] = _data[2] - _data[0];
            data[1] = _data[3] - _data[1];
        }

        if (!_isShape || _data[4] == 1)//FIXME Attention
        {
            data[0] = -data[0];
            data[1] = -data[1];
        }

        return NormalToLine(data, _length, false);
    }

    /** вычисление дуги подхода/отхода по направлению к заданному перемещению
     * подход: _LeadInLeadOut - true
     * отход:  _LeadInLeadOut - false
     *
     * @param _data
     * @param _length
     * @param _angle
     * @param _LeadInLeadOut
     * @return
     */
    JCC ArcToLine(int[] _data, int _length, int _angle, boolean _LeadInLeadOut)
    {

        int[] data = new int[5];
        int r = (int)Math.round(180 * _length / (Math.PI * _angle)); // радиус дуги
        Point endPoint = new Point(0, -r);
        Point centerPoint = new Point(0, 0); // это начало координат

        // 1) поворачиваем на _angle
        if (_LeadInLeadOut)
            endPoint = RotateCoords(GradToRad(_angle), endPoint.x, endPoint.y, false);
        else
            endPoint = RotateCoords(GradToRad(_angle), endPoint.x, endPoint.y, true);

        // 2) вычисляем угол b - угол между вертикалью и прямой, к которой строится дуга
        double b = CalculateAngle(_data[0], _data[1]);

        // 3) переносим центр координат в точку сопряжения прямой и дуги
        endPoint.y += r;
        centerPoint.y += r;

        // 4) поворачиваем на угол b
        endPoint = RotateCoords(b, endPoint.x, endPoint.y, true);
        centerPoint = RotateCoords(b, centerPoint.x, centerPoint.y, true);

        data[0] = endPoint.x;
        data[1] = endPoint.y;
        data[2] = centerPoint.x;
        data[3] = centerPoint.y;
        data[4] = 1;

        if (_LeadInLeadOut)
            data = ReverseMovement(data);

        return new JCC(JCC.Arc, data, "");
    }

    /** вычисление дуги подхода/отхода по направлению к заданному перемещению
     * подход: _LeadInLeadOut - true
     * отход:  _LeadInLeadOut - false
     *
     * @param _data
     * @param _length
     * @param _angle
     * @param _LeadInLeadOut
     * @param _isShape
     * @return
     */
    JCC ArcToArc(int[] _data, int _length, int _angle, boolean _LeadInLeadOut, boolean _isShape)
    {

        int[] data = new int[2];
        int[] tangent = new int[2];

        if (_LeadInLeadOut)
        {
            data[0] = _data[2];
            data[1] = _data[3];
        }
        else
        {
            data[0] = _data[2] - _data[0];
            data[1] = _data[3] - _data[1];
        }
        if (_isShape == true && _data[4] == 1)//FIXME Attention3 (full block)
        {
            data[0] = -data[0];
            data[1] = -data[1];
        }
        tangent = NormalToLine(data, _length, !_isShape).GetData();

        return ArcToLine(tangent, _length, _angle, _LeadInLeadOut);
    }

    /** перевести градусы в радианы
     *
     * @param _grad
     * @return
     */
    double GradToRad(int _grad)
    {
        return Math.PI * _grad / 180;
    }

    /** перевести градусы в радианы
     *
     * @param _grad
     * @return
     */
    double GradToRad(double _grad)
    {
        return Math.PI * _grad / 180;
    }

    /** перевернуть перемещение по направлению
     *
     * @param _data
     * @return
     */
    int[] ReverseMovement(int[] _data)
    {
        int data[] = new int[_data.length];

        if (_data.length == 2)
        {
            data[0] = -_data[0];
            data[1] = -_data[1];
        }
        else
        {
            data[0] = -_data[0];
            data[1] = -_data[1];
            data[2] = _data[2] - _data[0];
            data[3] = _data[3] - _data[1];
            data[4] = _data[4];
        }

        return data;
    }

}
