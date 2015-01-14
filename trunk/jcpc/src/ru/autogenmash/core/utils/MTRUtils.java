package ru.autogenmash.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import ru.autogenmash.core.ArcInterpolator;
import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.utils.compiler.Compiler;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 10.05.2007 9:59:43
 */
public final class MTRUtils
{
    /** Карта соответствия имен осей их индексам. */
    private static final Map _axisNames = new HashMap();


    /**Возвращает индекс оси по ее имени.
     * @param axisName имя оси
     * @return индекс оси
     */
    public static String getAxisIndexStr(final String axisName)
    {
        if (_axisNames.size() == 0)
        {
            _axisNames.put("X", "0");
            _axisNames.put("Y", "1");
            _axisNames.put("Z", "2");
            _axisNames.put("W", "3");
            _axisNames.put("C", "4");
            _axisNames.put("U", "5");
        }

        return (String)_axisNames.get(axisName);
    }

    /**Создать кадр-дугу в относительных координатах
     * @return дуга
     */
    public static CachedCpFrame createArcFrame(int g, int x, int y, int i, int j, boolean allowFullArcs)
    {
        CpSubFrame arcSubFrame = createArcSubFrame(g, x, y, i, j, allowFullArcs);

        CpSubFrame[] data = new CpSubFrame[] {arcSubFrame};
        CachedCpFrame arcFrame = new CachedCpFrame(CpFrame.FRAME_TYPE_ARC, data);
        arcFrame.setHasG02(g == 2);
        arcFrame.setHasG03(g == 3);

        arcFrame.setHasX(x != 0);
        arcFrame.setHasY(y != 0);
        arcFrame.setHasI(i != 0);
        arcFrame.setHasJ(j != 0);

        return arcFrame;
    }
    
    public static CachedCpFrame createArcFrame(int g, int x, int y, int i, int j)
    {
        return createArcFrame(g, x, y, i, j, false);
    }

    /**Создать кадр-дугу в относительных координатах
     * @return дуга
     */
    public static CachedCpFrame createArcFrame(int g, Point endPoint, Point centerPoint, boolean allowFullArcs)
    {
        return createArcFrame(g, endPoint.x, endPoint.y, centerPoint.x, centerPoint.y, allowFullArcs);
    }
    
    public static CachedCpFrame createArcFrame(int g, Point endPoint, Point centerPoint)
    {
        return createArcFrame(g, endPoint, centerPoint, false);
    }

    public static CpSubFrame createArcSubFrame(int g, int x, int y, int i, int j, boolean allowFullArcs)
    {
        if (g < 0 || g > 3)
            throw new IllegalArgumentException("Wrong g comand");

        if (allowFullArcs == false)
            if (x == 0 && y == 0)
                throw new IllegalArgumentException("Wrong x or(and) y command");

        if (i == 0 && j == 0)
            throw new IllegalArgumentException("Wrong i or(and) j command");

        Vector ccs = new Vector(5);
        ccs.add(new CC(CC.G, g));
        if (x != 0)
            ccs.add(new CC(CC.X, x));
        if (y != 0)
            ccs.add(new CC(CC.Y, y));
        if (i != 0)
            ccs.add(new CC(CC.I, i));
        if (j != 0)
            ccs.add(new CC(CC.J, j));

        CpSubFrame arcSubFrame = new CpSubFrame(CpSubFrame.RC_GEO_ARC, (CC[])ccs.toArray(new CC[0]));
        return arcSubFrame;
    }
    
    public static CpSubFrame createArcSubFrame(int g, int x, int y, int i, int j)
    {
        return createArcSubFrame(g, x, y, i, j, false);
    }

    /**Создать кадр-линию в относительных координатах
     * @return линия
     */
    public static CachedCpFrame createLineFrame(int g, int x, int y)
    {
        CpSubFrame[] data = {createLineSubFrame(g, x, y)};
        CachedCpFrame lineFrame = new CachedCpFrame(CpFrame.FRAME_TYPE_LINE, data);

        lineFrame.setHasG00(g == 0);
        lineFrame.setHasG01(g == 1);
        lineFrame.setHasX(x != 0);
        lineFrame.setHasY(y != 0);

        return lineFrame;
    }

    public static CpSubFrame createLineSubFrame(int g, int x, int y)
    {
        if (g < 0 || g > 1)
            throw new IllegalArgumentException("Wrong g comand");

        if (x == 0 && y == 0)
            throw new IllegalArgumentException("Wrong x or(and) y command");

        Vector ccs = new Vector(3);
        ccs.add(new CC(CC.G, g));
        if (x != 0)
            ccs.add(new CC(CC.X, x));
        if (y != 0)
            ccs.add(new CC(CC.Y, y));

        int type = CpSubFrame.RC_GEO_LINE;
        if (g == 0)
            type = CpSubFrame.RC_GEO_FAST;

        return new CpSubFrame(type, (CC[])ccs.toArray(new CC[0]));
    }

    public static void transformArcToLine(CachedCpFrame frame, int g, int x, int y)
    {
        if (frame.getType() != CpFrame.FRAME_TYPE_ARC)
            throw new IllegalArgumentException("Frame \"" + frame + " is not an Arc");

        CpSubFrame subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
        subFrame.copy(createLineSubFrame(g, x, y));

        frame.setType(CpFrame.FRAME_TYPE_LINE);

        frame.setHasG00(g == 0);
        frame.setHasG01(g == 1);
        frame.setHasG02(false);
        frame.setHasG03(false);

        frame.setHasX(x != 0);
        frame.setHasY(y != 0);
        frame.setHasI(false);
        frame.setHasJ(false);
    }
    
    public static String correctFrameArcCenter(CachedCpFrame frame)
    {
        if (frame.getType() != CpFrame.FRAME_TYPE_ARC)
            throw new IllegalArgumentException("Frame \"" + frame + "\" is not an Arc");

        CpSubFrame arcSubFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
        // проверка радиусов дугового интерполятора
        int x = Utils.toInt(arcSubFrame.getDataByType(CC.X));
        int y = Utils.toInt(arcSubFrame.getDataByType(CC.Y));
        int i = Utils.toInt(arcSubFrame.getDataByType(CC.I));
        int j = Utils.toInt(arcSubFrame.getDataByType(CC.J));

        double l1 = MathUtils.length(i, j);
        double l2 = MathUtils.length(x, y, i, j);

        if (MathUtils.compareDouble(l1, l2, Compiler.RADIUS_COMPARABLE_ACCURACY) == false)
            return "Несовпадение радиусов дугового интерполятора.";
        else if (l1 != l2)
        {
            Point newCenter = MathUtils.correctArcCenter(frame.hasG02(), x, y, i, j);
            arcSubFrame.setData(createArcSubFrame(frame.hasG02() ? 2 : 3, x, y, newCenter.x, newCenter.y).getData());
            frame.setHasI(newCenter.x != 0);
            frame.setHasJ(newCenter.y != 0);
        }

        return null;
    }

    /**Представить (интерполировать) дугу в виде множества отрезков.
     * @param x
     * @param y
     * @param i
     * @param j
     * @param direction true (-1) по ЧС.
     * @param stepLength (она же feed) длина одного отрезка разбиения дуги.
     * @param axis1 имя оси X (например CC.X)
     * @param axis2 имя оси Y (например CC.W)
     * @return CpSubFrame[], содержащий множество отрезков.
     */
    public static CpSubFrame[] interpolateArc(int x, int y, int i, int j,
            boolean direction, double stepLength, int axis1, int axis2)
    {
        ArrayList subFramesList = new ArrayList();

        ArcInterpolator interpolator = new ArcInterpolator(direction == true ? -1 : 1, new Point(x, y), new Point(i, j));

        Point stepPoint = new Point(0, 0);

        while (interpolator.getRemainedS() > 0)
        {
            CC[] cc = new CC[3];
            // разбиваем дугу на множество отрезков; перебираем их
            interpolator.doStep(stepLength, stepPoint);

            cc[0] = new CC(CC.G, 1, "G01");
            cc[1] = new CC(axis1, (int)stepPoint.x, "part of Arc");
            cc[2] = new CC(axis2, (int)stepPoint.y, "part of Arc");

            subFramesList.add(new CpSubFrame(CpSubFrame.RC_GEO_LINE, cc));
        }

        Object[] objects = subFramesList.toArray();
        CpSubFrame[] subFrames = new CpSubFrame[objects.length];
        for (int k = 0; k < objects.length; k++)
            subFrames[k] = (CpSubFrame)objects[k];

        return subFrames;
    }

    public static void addFrames(List storage, CachedCpFrame[] frames)
    {
        if (storage == null)
            throw new IllegalArgumentException("Storage " + storage + " can not be null");
    
        for (int i = 0; i < frames.length; i++)
        {
            storage.add(frames[i]);
        }
    }
    
    public static void checkCpListGeoLengths(CPList cpList, int minLength)
    {
        for (int p = 0; p < cpList.getLength(); p++)
        {
            CachedCpFrame frame = cpList.getFrame(p);
            
            if (frame.isGeo() == false)
                continue;
            
            double euclidLength = frame.getEuclidLength();
            if (euclidLength < minLength)
                System.err.println(euclidLength + " : " + frame);
        }
    }
    
    /** Проверить cpList на целостность и ошибки 
     * @param cpList
     * @return
     */
    public static void checkCpList(CPList cpList)
    {
        for (int p = 0; p < cpList.getLength(); p++)
        {
            CachedCpFrame frame = cpList.getFrame(p);

//            double euclidLength = frame.getEuclidLength();
//            if (frame.isGeo() && euclidLength < 500)
//                System.err.println(euclidLength + " : " + frame);
            
            Integer X = frame.getDataByType(CC.X);
            Integer Y = frame.getDataByType(CC.Y);
            Integer I = frame.getDataByType(CC.I);
            Integer J = frame.getDataByType(CC.J);

            if (X == null && frame.hasX())
                System.err.println("X Wrong frame: " + frame);
            if (Y == null && frame.hasY())
                System.err.println("Y Wrong frame: " + frame);
            if (I == null && frame.hasI())
                System.err.println("I Wrong frame: " + frame);
            if (J == null && frame.hasJ())
                System.err.println("J Wrong frame: " + frame);
            
            Integer gData = frame.getDataByType(CC.G);
            if (gData == null)
            {
                if (frame.hasG00() || frame.hasG01() || frame.hasG02() || frame.hasG03())
                    System.err.println("G Wrong frame: " + frame);
            }
            
            int g = Utils.toInt(gData);
            if (frame.hasG00() && g != 0)
                System.err.println("G0 Wrong frame: " + frame);
            if (frame.hasG01() && g != 1)
                System.err.println("G1 Wrong frame: " + frame);
            if (frame.hasG02() && g != 2)
                System.err.println("G2 Wrong frame: " + frame);
            if (frame.hasG03() && g != 3)
                System.err.println("G3 Wrong frame: " + frame);
            
            
            if (gData != null)
            {
                if (frame.hasG00() == false && g == 0)
                    System.err.println("G0 Wrong frame: " + frame);
                if (frame.hasG01() == false && g == 1)
                    System.err.println("G1 Wrong frame: " + frame);
                if (frame.hasG02() == false && g == 2)
                    System.err.println("G2 Wrong frame: " + frame);
                if (frame.hasG03() == false && g == 3)
                    System.err.println("G3 Wrong frame: " + frame);
            }
            
            
            if (frame.getType() == CpFrame.FRAME_TYPE_UNKNOWN)
            {
                if (X != null || Y != null || I != null || J != null)
                    System.err.println("Type unknown. Wrong frame: " + frame);
                
            }
            else if (frame.getType() == CpFrame.FRAME_TYPE_LINE)
            {
                if ((X == null && Y == null) || I != null || J != null)
                    System.err.println("Type line. Wrong frame: " + frame);
            }
            else if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
            {
                if (X == null && Y == null && I == null && J == null)
                    System.err.println("Type arc. Wrong frame: " + frame);
            }

            if (frame.hasX() && X.intValue() == 0)
                System.err.println("X 0. Wrong frame: " + frame);
            if (frame.hasY() && Y.intValue() == 0)
                System.err.println("Y 0. Wrong frame: " + frame);
            if (frame.hasI() && I.intValue() == 0)
                System.err.println("I 0. Wrong frame: " + frame);
            if (frame.hasJ() && J.intValue() == 0)
                System.err.println("J 0. Wrong frame: " + frame);
     
            
            for (int k = 0; k < frame.getLength(); k++)
            {
                CpSubFrame subFrame = frame.getSubFrame(k);
                int type = subFrame.getType();
                if (type == CpSubFrame.RC_GEO_FAST)
                {
                    if (gData == null)
                        System.err.println("G fast. Wrong frame: " + frame);
                    else if (g != 0)
                        System.err.println("G 0 fast. Wrong frame: " + frame);
                }
                else if (type == CpSubFrame.RC_GEO_LINE)
                {
                    if (gData == null)
                        System.err.println("G line. Wrong frame: " + frame);
                    else if (g != 1)
                        System.err.println("G 1 fast. Wrong frame: " + frame);
                }
                else if (type == CpSubFrame.RC_GEO_ARC)
                {
                    if (gData == null)
                        System.err.println("G arc. Wrong frame: " + frame);
                    else if (g < 2 || g > 3)
                        System.err.println("G 23 arc. Wrong frame: " + frame);
                }
            }
            
            if (gData != null)
            {
                if (g == 0 && frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST) == null)
                    System.err.println("G 0 subframe. Wrong frame: " + frame);
                if (g == 1 && frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE) == null)
                    System.err.println("G 1 subframe. Wrong frame: " + frame);
                if ((g == 2 || g == 3) && frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC) == null)
                    System.err.println("G 23 subframe. Wrong frame: " + frame);
            }
        }
    }
}














