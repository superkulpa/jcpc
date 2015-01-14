package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.map.LinkedMap;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/** Wavering movements tolerant smoothing. (WMTS - copyright 2009 by Diman)
 * (CorridorFrictionRemoveAction - synonym)
 * толерантное сглаживание колеблющихся перемещений
 * @author Dymarchuk Dmitry
 * 28.10.2009 17:28:43
 */
public class WaveringMovementsTolerantSmoothAction extends StepActionBase
{

    private boolean _splineShortLastStep = false;
    
    public WaveringMovementsTolerantSmoothAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public String getDescription()
    {
        return "Толерантное сглаживание колеблющихся перемещений";
    }
    
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
      //TODO Сплайнирование доработать механизм 
//        int splineStepLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_SPLINE_STEP_LENGTH));
//        splineStepLength *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        int minMovementLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_MIN_MOVEMENT_LENGTH));
//        minMovementLength *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        int kStepAccuracy = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_K_STEP_ACCURACY));
//        kStepAccuracy *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        boolean fullShapeSpline = (new Boolean((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_FULL_SHAPE_SPLINE))).booleanValue();
//        
//        boolean useNURBS = (new Boolean((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_USE_NURBS))).booleanValue();
//        
//        int includeLinesMaxLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_SMOOTHING_SPLINE_INCLUDE_LINES_MAX_LENGTH));
//        includeLinesMaxLength *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        _log.info("Всего гео кадров: " + cpList.getGeoFramesCount());
//        //traceMovements(cpList);
//
//        combineShortMovements(cpList, minMovementLength);
//        
//        // 2 method:
//        //for (int i = 0; i < 3; i++)
//        //{
//        //oneCouplingSmoothing(cpList);
//        //}
//
////        CompilerError result = kStepsSimpleLinearSmoothing(cpList, K, kStepAccuracy);
////        if (result != null)
////            return result;
//        
//        // 3 method (spline):
//        LinkedMap intervals;
//        if (fullShapeSpline)
//            intervals = findBadIntervals2(cpList);
//        else
//            intervals = findBadIntervals(cpList, includeLinesMaxLength);
//
//        _log.info("Найдено " + intervals.size() + " колеблющихся участков");
//
//        if (intervals.size() > 0)
//        {
//            smoothBySpline(cpList, intervals, splineStepLength, minMovementLength, useNURBS);
//            _log.info("Гео кадров после сплайнирования: " + cpList.getGeoFramesCount());
//            
//            if (_splineShortLastStep)
//                combineShortMovements(cpList, minMovementLength);   
//        }

        return null;
    }

    private void smoothBySpline(CPList cpList, LinkedMap intervals,
            int splineStepLength, int minMovementLength, boolean useNURBS)
    {
        int waveringMovementsCount = 0;
        int idx = 0;
        Vector newFrames = new Vector(2 * cpList.getLength(), cpList.getLength() / 2);
        
        Iterator iterator = intervals.keySet().iterator();
        while (iterator.hasNext())
        {
            Object key = iterator.next();
            int end = ((Integer)intervals.get(key)).intValue();
            int start = ((Integer)key).intValue();
            waveringMovementsCount += end - start + 1;

            Point[] polyLine = new Point[end - start + 1 + 1];
            for (int l = start; l <= end; l++)
                polyLine[l - start + 1] = cpList.getFrame(l).getXY();
            polyLine[0] = new Point();

            double initalPolylineEuqlidLength = calculateSplineEuqlidLength(polyLine);
            
            polyLine = MathUtils.convertComparativePolyLineToAbsolute(polyLine);

            CachedCpFrame[] smoothedFrames;
            if (useNURBS)
                // NURBS
                smoothedFrames = createNURBS(polyLine, initalPolylineEuqlidLength, splineStepLength);
            else
                // PARAMETRIZED SPLINE
                smoothedFrames = createParametrizedSpline(polyLine, initalPolylineEuqlidLength, splineStepLength, minMovementLength);

            // CHECK
            checkSmoothing(polyLine, smoothedFrames);

            for (int l = idx; l < start; l++)
                newFrames.add(cpList.getFrame(l));
            
            for (int l = 0; l < smoothedFrames.length; l++)
                newFrames.add(smoothedFrames[l]);

            idx = end + 1;
        }
        
        for (int l = idx; l < cpList.getLength(); l++)
            newFrames.add(cpList.getFrame(l));

        _log.info("Сглажено при сплайн-корридорировании: " + waveringMovementsCount);
        
        cpList.setData(newFrames);
    }

    private CachedCpFrame[] createParametrizedSpline(Point[] polyLine, 
            double initalLength, int splineStepLength, int minMovementLength)
    {
        int discrCount = 10; // реально это точность при построении сплайна
        
        int[] polylineX = new int[polyLine.length];
        int[] polylineY = new int[polyLine.length];
        for (int l = 0; l < polylineY.length; l++)
        {
            Point point = polyLine[l];
            polylineX[l] = point.x;
            polylineY[l] = point.y;
        }

        int[] smoothedX = smoothPolyLine2(polylineX, discrCount);
        int[] smoothedY = smoothPolyLine2(polylineY, discrCount);
        
        double splineLength = calculateSplineEuqlidLength(smoothedX, smoothedY);
//        double splineLength = initalLength;
        
        int steps = (int)Math.round(splineLength / splineStepLength)/* + 1*/;
        if (steps == 0)
            steps = 1;

        int outputTrajectoryStepLength = (int)Math.round(splineLength / steps) + 1;
        
        ArrayList smoothedFramesList = new ArrayList(steps);
   
        int xAbs = 0;
        int yAbs = 0;
        for (int l = 0; l < smoothedX.length; l++)
        {
            xAbs += smoothedX[l];
            yAbs += smoothedY[l];

            if (MathUtils.length(xAbs, yAbs) > outputTrajectoryStepLength)
            {
                smoothedFramesList.add(MTRUtils.createLineFrame(1, xAbs, yAbs));
                xAbs = 0;
                yAbs = 0;
            }
        }
        if (xAbs != 0 || yAbs != 0)
        {
            smoothedFramesList.add(MTRUtils.createLineFrame(1, xAbs, yAbs));
            double len = MathUtils.length(xAbs, yAbs);
            if (len < minMovementLength)
            {
                _splineShortLastStep = true;
                _log.warn("Последний шаг сплайна меньше допуска: " + len);
            }
        }
        
        return (CachedCpFrame[])smoothedFramesList.toArray(new CachedCpFrame[0]);
    }

    private CachedCpFrame[] createNURBS(Point[] polyLine, double initalPolylineEuqlidLength, int splineStepLength)
    {
        int discrCount;
        discrCount = (int)Math.round(initalPolylineEuqlidLength / splineStepLength);
        if (discrCount == 0)
            discrCount = 1;
        CachedCpFrame[] smoothedFrames = smoothPolyLine(polyLine, discrCount);
        
        if (Compiler.DEBUG)
        {
            for (int i = 0; i < smoothedFrames.length; i++)
            {
                double len2 = smoothedFrames[i].getEuclidLength();
                String msg = "len2: " + len2;
                if (len2 < 500)
                    System.err.println(msg);
            }
        }
        
        return smoothedFrames;
    }

    public static double calculateSplineEuqlidLength(int[] polylineX, int[] polylineY)
    {
        if (polylineX.length != polylineY.length)
            throw new IllegalArgumentException("Arrays sizes violation: "  + 
                polylineX.length + " != " + polylineY.length);

        double length = 0;
        
        for (int i = 0; i < polylineX.length; i++)
        {
            int x = polylineX[i];
            int y = polylineY[i];
            length += MathUtils.length(x, y);
        }
        
        return length;
    }
    
    public static double calculateSplineEuqlidLength(Point[] polyline)
    {
        double length = 0;
        
        for (int i = 0; i < polyline.length; i++)
            length += polyline[i].length();
        
        return length;
    }
    
    public static double calculateSplineEuqlidLength(CachedCpFrame[] frames)
    {
        double length = 0;
        
        for (int i = 0; i < frames.length; i++)
            length += frames[i].getEuclidLength();
        
        return length;
    }

//    private void printSmoothingInfo(int length, int removedFramesCount)
//    {
//        _log.info("Всего кадров: " + length + 
//                ", удалено при корридорировании:" + removedFramesCount);
//    }

    /** Найти интервалы с "пилой", для дальнейшего сглаживания
     * @param cpList
     * @return пара <№ начального кадра> - <№ конечного кадра>
     */
    private LinkedMap findBadIntervals(CPList cpList, int includeLinesMaxLength)
    {
        LinkedMap intervals = new LinkedMap();

        double angle = 180;
        CachedCpFrame prevFrame = null;

        int lineCount = 0;
        for (int l = 0; l < cpList.getLength(); l++)
        {
            CachedCpFrame frame = cpList.getFrame(l);

            if (prevFrame != null && frame.hasG01() && frame.hasM() == false &&
                    prevFrame.hasG01() && prevFrame.hasM() == false)
            {
                Point v1 = prevFrame.getXY();
                Point v2 = frame.getXY();

                v1.inverse();

                angle = MathUtils.calculateCrossAngle(v1, v2);
                angle = Math.abs(Math.toDegrees(angle));
            }

            if (frame.hasG01() && frame.getLength() == 1 && frame.getEuclidLength() < includeLinesMaxLength /*&& angle > 100*/)
                lineCount++;
            else
            {
                if (lineCount > 2)
                    intervals.put(new Integer(l - lineCount), new Integer(l - 1));

                lineCount = 0;
            }
            prevFrame = frame;
        }

        int last = cpList.getLength() - 1;
        if (lineCount > 2)
            intervals.put(new Integer(last - lineCount + 1), new Integer(last));

        return intervals;
    }

    private LinkedMap findBadIntervals2(CPList cpList)
    {
        LinkedMap intervals = new LinkedMap();

        int lineCount = 0;
        for (int l = 0; l < cpList.getLength(); l++)
        {
            CachedCpFrame frame = cpList.getFrame(l);

            if (frame.hasG01() && frame.getLength() == 1)
                lineCount++;
            else
            {
                if (lineCount > 2)
                    intervals.put(new Integer(l - lineCount), new Integer(l - 1));

                lineCount = 0;
            }
        }

        int last = cpList.getLength() - 1;
        if (lineCount > 2)
            intervals.put(new Integer(last - lineCount + 1), new Integer(last));

        return intervals;
    }

    private void checkSmoothing(Point[] polyLine, CachedCpFrame[] smoothedFrames)
    {
        int xAbs = 0;
        int yAbs = 0;
        for (int l = 0; l < smoothedFrames.length; l++)
        {
            CachedCpFrame frame = smoothedFrames[l];
            Point xy = frame.getXY();
            xAbs += xy.x;
            yAbs += xy.y;
        }

        int checkedX = polyLine[polyLine.length - 1].x - polyLine[0].x;
        int checkedY = polyLine[polyLine.length - 1].y - polyLine[0].y;

        if (xAbs != checkedX || yAbs != checkedY)
        {
            System.err.println("Error! Wrong smoothing");
            _log.error("Ошибка при сплайн сглаживании: конечная точка не совпадает с траекторией");
        }
    }

    /**Сгладить полилинию, принадлежащей контуру упрвляющей программы
     * NURBS-сплайном, c одинаковыми весами для всех точек.
     * @param polyLine - опорный многоугольник (полилиния УП), в абсолютных координатах
     * @param discrCount - количетво отрезков в новой полилинии (сглаженной)
     */
    private CachedCpFrame[] smoothPolyLine(Point[] polyLine, int discrCount)
    {
        CachedCpFrame[] res = new CachedCpFrame[discrCount];
        double step = (double)1 / discrCount;

        int n = polyLine.length - 1;

        int xPrev = 0;
        int yPrev = 0;

        double t = 0;

        for (int k = 0; k <= discrCount; k++)
        {
            double xSum = 0;
            double ySum = 0;
            double sum = 0;

            for (int i = 0; i <= n; i++)
            {
                int x = polyLine[i].x;
                int y = polyLine[i].y;

                double factor = MathUtils.binomK(i, n) * Math.pow(t, i) * Math.pow((double)1 - t, n - i);
                if (Double.isNaN(factor))
                    throw new IllegalArgumentException("Bernshtein factor is NaN");
                xSum += factor * x;
                ySum += factor * y;
                sum += factor;
            }

            int newX = (int)Math.round(xSum / sum);
            int newY = (int)Math.round(ySum / sum);

            if (newX == 0 && newY == 0 && k != 0)
            {
                String err = "Ошибка при сплайн-сглаживании. Перемещение не может быть нулевым";
                _log.error(err);
                _warnings.add(new CompilerError(err));
            }

            if (k != 0)
                res[k - 1] = MTRUtils.createLineFrame(1, newX - xPrev, newY - yPrev);

            xPrev = newX;
            yPrev = newY;

            t += step;
        }

        return res;
    }

    /**Сгладить полилинию, принадлежащей контуру упрвляющей программы
     * параметрическим сплайном.
     * @param polyLine - опорный многоугольник (полилиния УП), в абсолютных координатах
     * @param discrCount - количетво отрезков в новой полилинии (сглаженной)
     */
    private int[] smoothPolyLine2(int[] polyLine, int discrCount)
    {
        int n = polyLine.length - 1;

        int[] res = new int[discrCount * (n + 1)];
        double step = (double)1 / discrCount;

        int prevValue = 0;

        for (int i = 0; i <= n; i++)
        {
            double arg = polyLine[i];     // i
            double argPrev; // i-1
            double argNext1;  // i+1
            double argNext2;  // i+2
            if (i == 0)
                argPrev = arg;
            else
                argPrev = (double)polyLine[i - 1];
            if (i == n)
            {
                argNext1 = arg;
                argNext2 = arg;
            }
            else
            {
                argNext1 = (double)polyLine[i + 1];
                if (i == n - 1)
                    argNext2 = argNext1;
                else
                    argNext2 = (double)polyLine[i + 2];
            }

            double a0 = (argPrev + 4*arg + argNext1) / 6;
            double a1 = (-argPrev + argNext1) / 2;
            double a2 = (argPrev -2*arg + argNext1) / 2;
            double a3 = (-argPrev + 3*arg - 3*argNext1 + argNext2) / 6;

            double t = 0;
            for (int j = 0; j < discrCount; j++)
            {
                t += step;
                int value = (int)Math.round(((a3*t + a2)*t + a1)*t + a0);
                res[i * discrCount + j ] = value - prevValue;
                prevValue = value;
            }
        }

        return res;
    }

//    private void traceMovements(CPList cpList)
//    {
//        for (int i = 0; i < cpList.getLength(); i++)
//        {
//            CachedCpFrame frame = cpList.getFrame(i);
//
//            if (frame.isGeo() == false)
//                continue;
//
//            int x = Utils.toInt(frame.getDataByType(CC.X));
//            int y = Utils.toInt(frame.getDataByType(CC.Y));
//
//            int length = (int)MathUtils.length(x, y);
//            if (length < 10)
//                _log.debug("Внимание! Очень короткое перемещение");
//            if (length < 500)
//                _log.debug("Короткое перемещение: " + length);
//        }
//    }

    private void combineShortMovements(CPList cpList, int minMovementLength)
    {
        int combinedMovementsCount = 0;

        for (int l = 0; l < cpList.getLength() - 1; l++)
        {
            CachedCpFrame frame1 = cpList.getFrame(l);
            CachedCpFrame frame2 = cpList.getFrame(l + 1);

            if (frame1 == null || frame2 == null)
                continue;

            if (frame1.isGeo() == false || frame1.getLength() > 1)
                continue;
            
            if (frame2.isGeo() == false || frame2.getLength() > 1)
                continue;

            if (combineCoupling(frame1, frame2, minMovementLength))
            {
                cpList.setFrame(++l, null);
                combinedMovementsCount++;
            }
        }

        if (combinedMovementsCount > 0 )
            cpList.defragment(); 
        
        _log.info("Вырождено \"маленьких\" перемещений: " + combinedMovementsCount);
    }
    
    private boolean combineCoupling(CachedCpFrame frame1, CachedCpFrame frame2, int minMovementLength)
    {
//        у какого кадра меньше длина, тот и вырождаем.
//        если у обоих маленькая длина, то оба вырождаем в линию.
        
        double length1 = frame1.getEuclidLength();
        double length2 = frame2.getEuclidLength();
        
        if (length1 > minMovementLength && length2 > minMovementLength)
            return false;
        
        int x1 = Utils.toInt(frame1.getDataByType(CC.X));
        int y1 = Utils.toInt(frame1.getDataByType(CC.Y));
        
        int x2 = Utils.toInt(frame2.getDataByType(CC.X));
        int y2 = Utils.toInt(frame2.getDataByType(CC.Y));
        
        int newX = x1 + x2;
        int newY = y1 + y2;

        if (newX == 0 && newY == 0)
        {
            _log.warn("Замыкание кадров на сопряжении \"" + frame1 + "\" и \"" + frame2 + "\", кадры не объединены");
            return false;
        }
        
        if (frame1.hasG01() && frame2.hasG01())
        {
            frame1.copy(MTRUtils.createLineFrame(1, newX, newY));
        }
        else if (frame1.hasG01() && frame2.getType() == CpFrame.FRAME_TYPE_ARC)
        {
            if (length1 > length2)
            {
                frame1.copy(MTRUtils.createLineFrame(1, newX, newY));
            }
            else
            {
                int g = frame2.hasG02() ? 2 : 3;
                int i = Utils.toInt(frame2.getDataByType(CC.I));
                int j = Utils.toInt(frame2.getDataByType(CC.J));
                
                frame1.copy(MTRUtils.createArcFrame(g, newX, newY, i + x1/2, j + y1/2));
                String result = MTRUtils.correctFrameArcCenter(frame1);
                if (result != null)
                {
                    _log.error(result);
                    return false;
                }
            }
        }
        else if (frame1.getType() == CpFrame.FRAME_TYPE_ARC && frame2.hasG01())
        {
            int g = frame1.hasG02() ? 2 : 3;
            int i = Utils.toInt(frame1.getDataByType(CC.I));
            int j = Utils.toInt(frame1.getDataByType(CC.J));
            
            if (length1 < length2)
            {
                frame1.copy(MTRUtils.createLineFrame(1, newX, newY));
            }
            else
            {
                frame1.copy(MTRUtils.createArcFrame(g, newX, newY, i, j));
                String result = MTRUtils.correctFrameArcCenter(frame1);
                if (result != null)
                {
                    _log.error(result);
                    return false;
                }
            }
        }
        else if (frame1.getType() == CpFrame.FRAME_TYPE_ARC && frame2.getType() == CpFrame.FRAME_TYPE_ARC)
        {
            int g1 = frame1.hasG02() ? 2 : 3;
            int i1 = Utils.toInt(frame1.getDataByType(CC.I));
            int j1 = Utils.toInt(frame1.getDataByType(CC.J));
            
            int g2 = frame2.hasG02() ? 2 : 3;
            int i2 = Utils.toInt(frame2.getDataByType(CC.I));
            int j2 = Utils.toInt(frame2.getDataByType(CC.J));
            
            int g = g1;
            int i = i1;
            int j = j1;
            if (length1 < length2)
            {
                g = g2;
                i = i2 + x1;
                j = j2 + y1;
            }

            frame1.copy(MTRUtils.createArcFrame(g, newX, newY, i, j));
            String result = MTRUtils.correctFrameArcCenter(frame1);
            if (result != null)
            {
                _log.error(result);
                return false;
            }
        }
        else
            return false;
            //System.err.println("Wrong frames: \"" + frame1 + "\" | \"" + frame2 + "\"");
        
        return true;
    }
    
//    private void oneCouplingSmoothing(CPList cpList, int eps)
//    {
//        int smoothedMovementsCount = 0;
//
//        for (int l = 0; l < cpList.getLength() - 1; l++)
//        {
//            CachedCpFrame frame1 = cpList.getFrame(l);
//            CachedCpFrame frame2 = cpList.getFrame(l + 1);
//
//            if (frame1 == null || frame2 == null)
//                continue;
//
//            if (frame1.hasG01() == false || frame1.getLength() > 1)
//                continue;
//            
//            if (frame2.hasG01() == false || frame2.getLength() > 1)
//                continue;
//
//            if (smoothCoupling(frame1, frame2, eps))
//            {
//                cpList.setFrame(++l, null);
//                smoothedMovementsCount++;
//            }
//        }
//
//        int removedFramesCount = 0;
//        if (smoothedMovementsCount > 0 )
//            removedFramesCount = cpList.defragment(); 
//        
//        _log.info( 
//            "Сглажено при использовании метода одного сопряжения: " + smoothedMovementsCount + 
//            ", удалено: " + removedFramesCount);
//    }

//    private boolean smoothCoupling(CachedCpFrame prevFrame, CachedCpFrame currFrame, int E)
//    {
//        int x1 = Utils.toInt(prevFrame.getDataByType(CC.X));
//        int y1 = Utils.toInt(prevFrame.getDataByType(CC.Y));
//
//        int x2 = Utils.toInt(currFrame.getDataByType(CC.X));
//        int y2 = Utils.toInt(currFrame.getDataByType(CC.Y));
//
////        Point prevVect = new Point(x1, y1);
////        Point currVect = new Point(x2, y2);
////
////        double crossAngle = MathUtils.calculateCrossAngle(prevVect, currVect);
////        crossAngle = Math.abs(crossAngle);
////        if (crossAngle > Math.PI)
////            crossAngle -= Math.PI;
////
////        if (crossAngle < Math.PI * 0.75)
//        {
//            double alpha = MathUtils.calculateAngle2(x1 + x2, y1 + y2);
//            Point checkPoint = MathUtils.rotateCoords(alpha, x1, y1, false);
//            if (Math.abs(checkPoint.y) < E)
//            {
//                prevFrame.getSubFrameByType(CpSubFrame.RC_GEO_LINE).setData(
//                        MTRUtils.createLineSubFrame(1, x1 + x2, y1 + y2).getData());
//
//                return true;
//            }
//        }
//
//        return false;
//    }

//    private CompilerError kStepsSimpleLinearSmoothing(CPList cpList, int K, int eps)
//    {
//        // добавить параметры Kmin и Kmax
//        // обернуть все внешним циклом от Kmax до Kmin
//
//        int smoothedMovementsCount = 0;
//        
//        for (int l = 0; l < cpList.getLength(); l++)
//        {
//            CachedCpFrame startFrame = cpList.getFrame(l);
//
//            if (startFrame == null)
//                continue;
//
//            if (startFrame.hasG01() == false || startFrame.getLength() > 1)
//                continue;
//
//            CachedCpFrame[] frames = new CachedCpFrame[K];
//
//            int xAbs = 0;
//            int yAbs = 0;
//
//            int m = l;
//            while (m < l + K)
//            {
//                if (m == cpList.getLength())
//                    break;
//
//                CachedCpFrame frame = cpList.getFrame(m);
//                if (frame.hasG01() == false || frame.getLength() > 1)
//                    break;
//                else
//                {
//                    frames[m - l] = frame;
//                    m++;
//                    if (frame.hasX())
//                        xAbs += Utils.toInt(frame.getDataByType(CC.X));
//                    if (frame.hasY())
//                        yAbs += Utils.toInt(frame.getDataByType(CC.Y));
//
//                }
//            }
//
//            double alpha = MathUtils.calculateAngle2(xAbs, yAbs);
//
//            xAbs = 0;
//            yAbs = 0;
//
//            int p = 0;
//            for (p = 0; p < frames.length; p++)
//            {
//                int x = 0;
//                int y = 0;
//
//                CachedCpFrame frame = frames[p];
//                if (frame == null)
//                    break;
//
//                if (frame.hasX())
//                    x = Utils.toInt(frame.getDataByType(CC.X));
//                if (frame.hasY())
//                    y = Utils.toInt(frame.getDataByType(CC.Y));
//
//                xAbs += x;
//                yAbs += y;
//
//                Point checkPoint = MathUtils.rotateCoords(alpha, xAbs, yAbs, false);
//                if (Math.abs(checkPoint.y) >= eps)
//                {
//                    xAbs -= x;
//                    yAbs -= y;
//                    p--;
//                    break;
//                }
//            }
//
//            if (p > 1) // надо объединять
//            {
//                CpSubFrame subFrame = MTRUtils.createLineSubFrame(1, xAbs, yAbs);
//                startFrame.getSubFrameByType(CpSubFrame.RC_GEO_LINE).setData(subFrame.getData());
//                for (int q = 1; q < p; q++)
//                    cpList.setFrame(l + q, null);
//
//                smoothedMovementsCount += p;
//            }
//        }
//        
//        int removedFramesCount = 0;
//        if (smoothedMovementsCount > 0 )
//            removedFramesCount = cpList.defragment(); // метод с параметром - новый размер
//        
//        _log.info( 
//            "Сглажено при использовании метода \"kStepLineSmoothing\": " + smoothedMovementsCount + 
//            ", удалено: " + removedFramesCount);
//
//        
//        return null;
//    }

    
/*******************Доделать**********************************************************************/    
//    
//    /**Сгладить заданный участок cpList-а с заданной точностью E. 
//     * Сглаживание происходит путем обратного последовательного перебора 
//     * перемещений до тех пор пока отклонение от исходного контура меньше E.
//     * @param cpList
//     * @param E точность в юнитах
//     * @return
//     */
//    private CompilerError intervalSimpleLinearSmoothing(CPList cpList, Map intervals, int E)
//    {
//        Iterator iterator = intervals.keySet().iterator();
//        while (iterator.hasNext())
//        {
//            Object key = iterator.next();
//            int start = ((Integer)key).intValue();
//            int end = ((Integer)intervals.get(key)).intValue();
//            
//            smoothOneInterval(cpList, start, end, E);
//        }
//        
//        return null;
//    }
//
//    private void smoothOneInterval(CPList cpList, int start, int end, int E)
//    {
//        int xAbs = 0;
//        int yAbs = 0;
//        int checkedXAbs = 0;
//        int checkedYAbs = 0;
//        
//        for (int l = start; l <= end; l++)
//        {
//            CachedCpFrame frame = cpList.getFrame(l);
//            if (frame.hasX())
//                xAbs += Utils.toInt(frame.getDataByType(CC.X));
//            if (frame.hasY())
//                yAbs += Utils.toInt(frame.getDataByType(CC.Y));
//        }
//        
//        double alpha = MathUtils.calculateAngle2(xAbs, yAbs);
//        System.out.println(Math.toDegrees(alpha));
//        
//        boolean satisfy = true;
//        
//        for (int l = end; l >= start; l--)
//        {
//            CachedCpFrame frame = cpList.getFrame(l);
//            if (frame.hasX())
//                checkedXAbs -= Utils.toInt(frame.getDataByType(CC.X));
//            if (frame.hasY())
//                checkedYAbs -= Utils.toInt(frame.getDataByType(CC.Y));
//         
//            Point checkPoint = MathUtils.rotateCoords(alpha, checkedXAbs, checkedYAbs, false);
//            if (Math.abs(checkPoint.y) > E)
//            {
//                satisfy = false;
//                break;
//            }
//        }
//        
//        if (satisfy)
//        {
//            CpSubFrame subFrame = MTRUtils.createLineSubFrame(1, xAbs, yAbs);
//            cpList.getFrame(start).getSubFrameByType(CpSubFrame.RC_GEO_LINE).setData(subFrame.getData());
//            for (int q = start + 1; q <= end; q++)
//                cpList.setFrame(q, null);
//            
//            xAbs = 0;
//            yAbs = 0;
//        }
//    }
//    
//    
//    /**Сгладить cpList с заданной точностью E. 
//     * Сглаживание происходит путем последовательного прибавления линейных 
//     * перемещений до тех пор пока отклонение от исходного контура меньше E.
//     * @param cpList
//     * @param E точность в юнитах
//     * @return
//     */
//    private CompilerError simpleLinearSmoothing(CPList cpList, int E)
//    {
//        int xAbs = 0;
//        int yAbs = 0;
//        
//        // последний удовлетворительный вектор (в рамках точности)
//        int localLastSatisfactoryX = 0;
//        int localLastSatisfactoryY = 0;
//        
//        int globalLastSatisfactoryX = 0;
//        int globalLastSatisfactoryY = 0;
//        
//        int startPoint = 0;
//        
//        for (int l = 0; l < cpList.getLength(); l++)
//        {
//            
//            boolean satisfy = true;
//            CachedCpFrame frame = cpList.getFrame(l);
//
//            if (frame == null || frame.hasG01() == false)
//            {
//                // TODO если есть что объединять, то объединить
//                xAbs = 0;
//                yAbs = 0;
//                startPoint = l + 1;
//                continue;
//            }
//
//            if (frame.hasX())
//                xAbs += Utils.toInt(frame.getDataByType(CC.X));
//            if (frame.hasY())
//                yAbs += Utils.toInt(frame.getDataByType(CC.Y));
//            
//            double alpha = MathUtils.calculateAngle2(xAbs, yAbs);
//            System.out.println(Math.toDegrees(alpha));
//            
//            int checkedXAbs = 0;
//            int checkedYAbs = 0;
//            
//            for (int k = startPoint; k < l; k++)
//            {
//                CachedCpFrame checkedFrame = cpList.getFrame(k);
//                
//                int x = 0;
//                int y = 0;
//
//                if (checkedFrame.hasX())
//                    x = Utils.toInt(checkedFrame.getDataByType(CC.X));
//                if (checkedFrame.hasY())
//                    y = Utils.toInt(checkedFrame.getDataByType(CC.Y));
//
//                checkedXAbs += x;
//                checkedYAbs += y;
//                
//                Point checkPoint = MathUtils.rotateCoords(alpha, checkedXAbs, checkedYAbs, false);
//                if (Math.abs(checkPoint.y) > E)
//                {
//                    satisfy = false;
//                    break;
//                }
//                else
//                {
//                    localLastSatisfactoryX = checkedXAbs;
//                    localLastSatisfactoryY = checkedYAbs;
//                }
//            }
//            
//            if (satisfy == false)
//            {
//                CpSubFrame subFrame = MTRUtils.createLineSubFrame(1, lastSatisfactoryX, lastSatisfactoryY);
//                cpList.getFrame(startPoint).getSubFrameByType(CpSubFrame.RC_GEO_LINE).setData(subFrame.getData());
//                for (int q = startPoint + 1; q < l; q++)
//                    cpList.setFrame(q, null);
//                
//                startPoint = l;
//                xAbs = 0;
//                yAbs = 0;
//                lastSatisfactoryX = 0;
//                lastSatisfactoryY = 0;
//                l--;
//            }
//            else
//            {
//                globalLastSatisfactoryX = localLastSatisfactoryX;
//                globalLastSatisfactoryY = localLastSatisfactoryY;
//            }
//        }
//            
//        int removedFramesCount = cpList.defragment(); // метод с параметром - новый размер
//        System.out.println("Всего кадров: " + cpList.getLength());
//        System.out.println("При коридорировании было удалено " +
//                removedFramesCount + " гео кадров");
//
//        return null;
//    }
/****************************************************************************************************/    

}










