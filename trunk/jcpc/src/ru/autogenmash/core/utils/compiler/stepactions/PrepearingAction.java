package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 14:59:40
 */
public class PrepearingAction extends StepActionBase
{

    public static final String AUX_DATA_FULLARCS = "Fullarcs";
    
    /**Прибавка к индексу второй полудуги.
     * Требуется для уникальности ключа хранилища полудуг.
     */
    private static final int SECOND_ARC_INDEX_SUPPLYER = 1000 * 1000;

    public PrepearingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        prepareCpList(cpList, cpParameters);

        String result = transformCpList2ComparativeMovements(cpList);
        if (result != null)
            return new CompilerError(result);

        splitFullArcs(cpList);

        return null;
    }

    /**Подготовить УП лист:
     * <ul>
     *  <li>Преобразовать единицы измерения (десятки в импульсы).</li>
     *  <li>Использовать внешние параметры (вместо параметров из УП) подачу и эквидистанту.</li>
     *  <li>Преобразовать технологические команды.</li>
     * </ul>
     * @param cpList
     * @param cpParameters
     * @return
     */
    private void prepareCpList(CPList cpList, CpParameters cpParameters)
    {
        //int kerf = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_KERF));

        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);
            for (int j = 0; j < frame.getLength(); j++)
            {
                CpSubFrame subFrame = frame.getSubFrame(j);
                for (int k = 0; k < subFrame.getLength(); k++)
                {
                    CC cc = subFrame.getCC(k);
                    switch (cc.getType())
                    {
                    case CC.X:
                    case CC.Y:
                    case CC.I:
                    case CC.J:
                    case CC.A:
                    case CC.D:
                            //cc.setData(kerf * Compiler.SIZE_TRANSFORMATION_RATIO);
                            cc.setData(cc.getData() * Compiler.SIZE_TRANSFORMATION_RATIO);
                        break;
                    case CC.F:
                        int fValue = cc.getData();
                        cc.setData(fValue * 10 * Compiler.SIZE_TRANSFORMATION_RATIO);
                        break;
                    case CC.M:
                        int data = cc.getData();
                        if (data == 83 || data == 85)
                            cc.setData(74);
                        else if (data == 81 || data == 82/* || data == 84*/)
                            cc.setData(data - 10);
                        break;
                    }
                }
            }
        }
    }

    /**Разбить полные дуги на 2 полудуги.
     * @param cpList
     * @return
     */
    private void splitFullArcs(CPList cpList)
    {
        Boolean hasFullArcs = (Boolean)cpList.getAuxData(AUX_DATA_FULLARCS);
        if (hasFullArcs == null)
        {
            _log.warn("Ошибка при получении вспомогательной информации: " +
                    "параметр \"" + AUX_DATA_FULLARCS + "\" отсутствует");
            System.err.println("Warning in " + LoopingAction.class.getName() +
                    ": parameter \"" + AUX_DATA_FULLARCS + "\" is missing");
            return;
        }
        else if (hasFullArcs.booleanValue() == false)
            return;

        // TODO переделать;
        TreeMap fullArcsMap = new TreeMap();

        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);

            if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
            {
                if (frame.hasX() == false && frame.hasY() == false)
                {
                    // полная дуга
                    int interpI = 0;
                    int interpJ = 0;
                    if (frame.hasI())
                        interpI = frame.getDataByType(CC.I).intValue();
                    if (frame.hasJ())
                        interpJ = frame.getDataByType(CC.J).intValue();
                    frame.setHasX(interpI != 0);
                    frame.setHasY(interpJ != 0);

                    for (int j = 0; j < frame.getLength(); j++)
                    {
                        CpSubFrame subFrame = frame.getSubFrame(j);
                        if (subFrame.getType() == CpSubFrame.RC_GEO_ARC)
                        {
                            Vector ccs1 = new Vector(5);
                            Vector ccs2 = new Vector(5);
                            ccs1.add(new CC(CC.G, (frame.hasG02() == true ? 2 : 3), "Half arc 1"));
                            ccs2.add(new CC(CC.G, (frame.hasG02() == true ? 2 : 3), "Half arc 2"));
                            if (interpI != 0)
                            {
                                ccs1.add(new CC(CC.X, interpI * 2, "Half arc 1"));
                                ccs2.add(new CC(CC.X, -interpI * 2, "Half arc 2"));
                            }
                            if (interpJ != 0)
                            {
                                ccs1.add(new CC(CC.Y, interpJ * 2, "Half arc 1"));
                                ccs2.add(new CC(CC.Y, -interpJ * 2, "Half arc 2"));
                            }
                            if (interpI != 0)
                            {
                                ccs1.add(new CC(CC.I, interpI, "Half arc 1"));
                                ccs2.add(new CC(CC.I, -interpI, "Half arc 2"));
                            }
                            if (interpJ != 0)
                            {
                                ccs1.add(new CC(CC.J, interpJ, "Half arc 1"));
                                ccs2.add(new CC(CC.J, -interpJ, "Half arc 2"));
                            }
                            subFrame.setData((CC[])ccs1.toArray(new CC[0]));

                            CachedCpFrame frame2 = new CachedCpFrame(
                                    CpFrame.FRAME_TYPE_ARC, new CpSubFrame[] {
                                            new CpSubFrame(CpSubFrame.RC_GEO_ARC, (CC[])ccs2.toArray(new CC[0]))});
                            frame2.setHasG02(frame.hasG02());
                            frame2.setHasG03(frame.hasG03());
                            frame2.setHasX(frame.hasX());
                            frame2.setHasY(frame.hasY());
                            frame2.setHasI(frame.hasI());
                            frame2.setHasJ(frame.hasJ());

                            fullArcsMap.put(new Integer(i), frame);
                            fullArcsMap.put(new Integer(i + SECOND_ARC_INDEX_SUPPLYER), frame2);
                        }
                    }
                }
            }
        }

        int k = 0;
        CachedCpFrame[] frames = new CachedCpFrame[cpList.getLength() + fullArcsMap.size() / 2];
        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);

            if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
            {
                frames[i + k] = cpList.getFrame(i);
                if (fullArcsMap.containsKey(new Integer(i)))
                {
                    k++;
                    frames[i + k] = (CachedCpFrame)fullArcsMap.get(new Integer(i + SECOND_ARC_INDEX_SUPPLYER));
                }
            }
            else
                frames[i + k] = cpList.getFrame(i);
        }
        cpList.setData(frames);
    }

    /**Преобразовать все перемещения CPList-а в относительные.
     * @param cpList
     * @return
     */
    private String transformCpList2ComparativeMovements(CPList cpList)
    {
        int xAbs = 0;
        int yAbs = 0;

//        int x900 = 0;
//        int y900 = 0;
//        int z900 = 0;

        boolean isComparativeMovement = true;
        //boolean isG900 = false;

        for (int l = 0; l < cpList.getLength(); l++)
        {
            CachedCpFrame frame = cpList.getFrame(l);
            for (int m = 0; m < frame.getLength(); m++)
            {
                CpSubFrame subFrame = frame.getSubFrame(m);
                for (int k = 0; k < subFrame.getLength(); k++)
                {
                    CC cc = subFrame.getCC(k);
                    if (cc.getType() == CC.G)
                    {
                        if (cc.getData() == 91)
                        {
                            isComparativeMovement = true;
                        }
                        else if (cc.getData() == 90)
                        {
                            isComparativeMovement = false;
                            cc.setData(91);
                            cc.setDescription("transformed");
                        }
//                        else if (cc.getData() == 900)
//                        {
//                            isG900 = true;
//                        }
                    }
                }
                if (subFrame.isGeo() && isComparativeMovement == false)
                {
                    // do transformation
                    int x = xAbs;
                    int y = yAbs;

                    if (frame.hasX())
                        x = Utils.toInt(subFrame.getDataByType(CC.X));
                    if (frame.hasY())
                        y = Utils.toInt(subFrame.getDataByType(CC.Y));
                    int i = Utils.toInt(subFrame.getDataByType(CC.I));
                    int j = Utils.toInt(subFrame.getDataByType(CC.J));

                    int newX = x - xAbs;
                    int newY = y - yAbs;

                    if (newX == 0 && newY == 0)
                        _log.error("Преобразование абсолютных координат в относительные: " +
                                "присутствуют некорректные данные (x=0,y=0)");

                    Vector data = new Vector(5);
                    if (frame.getType() == CpFrame.FRAME_TYPE_LINE)
                        data.add(new CC(CC.G, frame.hasG00() ? 0 : 1));
                    else if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
                        data.add(new CC(CC.G, frame.hasG02() ? 2 : 3));
                    if (newX != 0)
                    {
                        data.add(new CC(CC.X, newX));
                        frame.setHasX(true);
                    }
                    if (newY != 0)
                    {
                        data.add(new CC(CC.Y, newY));
                        frame.setHasY(true);
                    }
                    if (i != 0)
                    {
                        data.add(new CC(CC.I, i));
                        frame.setHasI(true);
                    }
                    if (j != 0)
                    {
                        data.add(new CC(CC.J, j));
                        frame.setHasJ(true);
                    }

                    frame.setHasX(newX != 0);
                    frame.setHasY(newY != 0);

                    if (data.size() < 2)
                        _log.error("Некорректное G90 преобразование");

                    if (subFrame.isGeo() && data.size() < 2 && (xAbs != 0 || yAbs != 0))
                        return "Ошибка в " + (l + 1) +
                        " кадре: некорректная геометрическая команда";
                    subFrame.setData((CC[])data.toArray(new CC[0]));

                    if (frame.hasX())
                        xAbs = x;
                    if (frame.hasY())
                        yAbs = y;
                }
            }
//            if (isG900)
//            {
//                CpSubFrame[] subFrames = new CpSubFrame[3];
//                subFrames[0] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("X") + "=PARK(" + x900 + ")") });
//                subFrames[1] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("Y") + "=PARK(" + y900 + ")") });
//                subFrames[2] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("Z") + "=PARK(" + z900 + ")") });
//                frame.setType(CpFrame.FRAME_TYPE_UNKNOWN);
//                frame.setData(subFrames);
//                isG900 = false;
//            }
        }

        return null;
    }

    public String getDescription()
    {
        return "Подготовка контура";
    }

//    /**Преобразовать одну координату дугового перемещения.
//     * @param cc команда подлежащая преобразованию.
//     * @param isComparativeMovement
//     * @param absMovement
//     * @param transformation - во что преобразовывать, если true,
//     * то в абсолютные, иначе в относительные.
//     */
//    private static void transformArcCoordinate(CC cc, boolean isComparativeMovement, int xAbs, boolean transformation)
//    {
//        if (transformation == true && isComparativeMovement == true)
//        {
//            cc.setData(xAbs + cc.getData());
//            cc.setDescription("G90 transformed");
//        }
//        else if (transformation == false && isComparativeMovement == false)
//        {
//            cc.setData(cc.getData() - xAbs);
//            cc.setDescription("G91 transformed");
//        }
//    }
//
//    /**Преобразовать одну координату линейного перемещения.
//     * @param cc команда подлежащая преобразованию.
//     * @param isComparativeMovement
//     * @param absMovement
//     * @param transformation - во что преобразовывать, если true,
//     * то в абсолютные, иначе в относительные.
//     */
//    private static int transformLineCoordinate(CC cc, boolean isComparativeMovement,
//            int absMovement, boolean transformation)
//    {
//        int data = cc.getData();
//        if (transformation == true)
//        {
//            // в абсолютные
//            if (isComparativeMovement)
//            {
//                absMovement += data;
//                cc.setData(absMovement);
//                cc.setDescription("G90 transformed");
//            }
//            else
//            {
//                absMovement = data;
//            }
//        }
//        else
//        {
//            // в относительные
//            if (isComparativeMovement)
//            {
//                absMovement += data;
//            }
//            else
//            {
//                cc.setData(data - absMovement);
//                cc.setDescription("G91 transformed");
//                absMovement = data;
//            }
//        }
//
//        return absMovement;
//    }

//    protected String cpListMovements2ComparativeMovements(CPList cpList)
//    {
//        _xAbs = 0;
//        _yAbs = 0;
//        _zAbs = 0;
//        int x900 = 0;
//        int y900 = 0;
//        int z900 = 0;
//        boolean isComparativeMovement = true;
//        boolean isG900 = false;
//
//        for (int i = 0; i < cpList.getLength(); i++)
//        {
//            int xCpv = 0, yCpv = 0, iCpv = 0, jCpv = 0;
//            CachedCpFrame frame = cpList.getFrame(i);
//
//            for (int j = 0; j < frame.getLength(); j++)
//            {
//                CpSubFrame subFrame = frame.getSubFrame(j);
//
//                for (int k = 0; k < subFrame.getLength(); k++)
//                {
//                    CC cc = subFrame.getCC(k);
//                    switch (cc.getType())
//                    {
//                    case CC.G:
//                        if (cc.getData() == 91)
//                        {
//                            isComparativeMovement = true;
//                        }
//                        else if (cc.getData() == 90)
//                        {
//                            isComparativeMovement = false;
//                            cc.setData(91);
//                            cc.setDescription("transformed");
//                        }
//                        else if (cc.getData() == 900)
//                        {
//                            isG900 = true;
//                        }
//                        break;
//                    case CC.X:
//                        if (isG900)
//                            x900 = cc.getData();
//                        else
//                        {
//                            _xAbs = transformMovement(cc, isComparativeMovement, _xAbs);
//                            xCpv = cc.getData();
//                        }
//                        break;
//                    case CC.Y:
//                        if (isG900)
//                            y900 = cc.getData();
//                        else
//                        {
//                            _yAbs = transformMovement(cc, isComparativeMovement, _yAbs);
//                            yCpv = cc.getData();
//                        }
//                        break;
//                    case CC.I:
//                        transformMovement(cc, isComparativeMovement, _xAbs);
//                        iCpv = cc.getData();
//                        break;
//                    case CC.J:
//                        transformMovement(cc, isComparativeMovement, _yAbs);
//                        jCpv = cc.getData();
//                        break;
//                    case CC.Z:
//                        if (isG900)
//                            z900 = cc.getData();
//                        else
//                            _zAbs = transformMovement(cc, isComparativeMovement, _zAbs);
//                        break;
//                    }
//                }
//
//                if (frame.isGeo() == false)
//                    continue;
//
//                Vector data = new Vector(5);
//                if (frame.getType() == CpFrame.FRAME_TYPE_LINE)
//                    data.add(new CC(CC.G, frame.hasG00() ? 0 : 1));
//                else if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
//                    data.add(new CC(CC.G, frame.hasG02() ? 2 : 3));
//                if (xCpv != 0)
//                    data.add(new CC(CC.X, xCpv));
//                if (yCpv != 0)
//                    data.add(new CC(CC.Y, yCpv));
//                if (iCpv != 0)
//                    data.add(new CC(CC.I, iCpv));
//                if (jCpv != 0)
//                    data.add(new CC(CC.J, jCpv));
//
//                frame.setHasX(xCpv != 0);
//                frame.setHasY(yCpv != 0);
//                frame.setHasI(iCpv != 0);
//                frame.setHasJ(jCpv != 0);
//
//                if (data.size() == 1 && isComparativeMovement == false)
//                {
//                    frame.setHasG00(false);
//                    frame.setHasG01(false);
//                    frame.setHasG02(false);
//                    frame.setHasG03(false);
//                }
//
//                if (subFrame.isGeo() && data.size() < 2 && (_xAbs != 0 || _yAbs != 0))
//                    return "Ошибка в " + (i + 1) +
//                    " кадре: некорректная геометрическая команда";
//                subFrame.setData((CC[])data.toArray(new CC[0]));
//            }
//            if (isG900)
//            {
//                CpSubFrame[] subFrames = new CpSubFrame[3];
//                subFrames[0] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("X") + "=PARK(" + x900 + ")") });
//                subFrames[1] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("Y") + "=PARK(" + y900 + ")") });
//                subFrames[2] = new CpSubFrame(CpSubFrame.RC_GEO_PARK,
//                        new CC[] { new CC(CC.PARK, 0, MTRUtils.getAxisIndexStr("Z") + "=PARK(" + z900 + ")") });
//                frame.setType(CpFrame.FRAME_TYPE_UNKNOWN);
//                frame.setData(subFrames);
//                isG900 = false;
//            }
//        }
//
//        return null;
//    }
//
//    /**Преобразовать абсолютные перемещения в относительные.
//     * @param cc команда подлежащая преобразованию.
//     * @param isComparativeMovement
//     * @param absMovement
//     */
//    private int transformMovement(CC cc, boolean isComparativeMovement, int absMovement)
//    {
//        if (isComparativeMovement)
//            absMovement += cc.getData();
//        else
//        {
//            _data = cc.getData();
//            cc.setData(_data - absMovement);
//            cc.setDescription("G91 transformed");
//            absMovement = _data;
//        }
//        return absMovement;
//    }

}










