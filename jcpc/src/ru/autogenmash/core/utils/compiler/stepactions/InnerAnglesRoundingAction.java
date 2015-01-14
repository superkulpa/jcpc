package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;
import java.util.Vector;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/** Скругление внутренних углов.
 * @author Dymarchuk Dmitry
 * 17.08.2009 10:01:51
 */
public class InnerAnglesRoundingAction extends StepActionBase
{
    
    /** В градусах. "Корридор" отклонения от 180 градусов, т.е. 
     * скругляться будут сопряжения вне корридора [180 - value, 180 + value]*/
    public static final double CROSS_ANGLE_CORRIDOR = 0.05;

    private static int _shortArcsCount = 0;
    
    public InnerAnglesRoundingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public String getDescription()
    {
        return "Скругление внутренних углов";
    }
   
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
//        _shortArcsCount = 0;
//        
//        int minMovementLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_ROUNDING_MIN_MOVEMENT_LENGTH));
//        minMovementLength *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        int minGeoLengthAfterReducing = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_ROUNDING_MIN_GEO_LENGTH_AFTER_REDUCING));
//        minGeoLengthAfterReducing *= Compiler.SIZE_TRANSFORMATION_RATIO;
//        
//        int eps = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_ROUNDING_ACCURACY));
//        eps *= Compiler.SIZE_TRANSFORMATION_RATIO;
//
//        double minAngle = Double.parseDouble((String)cpParameters.getValue(Compiler.PARAM_ROUNDING_MIN_ANGLE_TO_ROUND)) / 10;
//
//        int roundedCouplings = 0;
//
//        Vector newFrames = new Vector(2 * cpList.getLength());
//        for (int k = 0; k < cpList.getLength() - 1; k++)
//        {
//            CachedCpFrame frame1 = cpList.getFrame(k);
//            CachedCpFrame frame2 = cpList.getFrame(k + 1);
//
//            if (frame1.getLength() != 1 || frame2.getLength() != 1)
//            {
//                newFrames.add(frame1);
//                continue;
//            }
//            
//            boolean frame1Broken = false;
//            boolean frame2Broken = false;
//            
//            CachedCpFrame arcFrame = roundCoupling(frame1, frame2, minMovementLength, minGeoLengthAfterReducing, minAngle, eps);
//            if (frame1.getType() == CpFrame.FRAME_TYPE_UNKNOWN && (frame1.hasX() || frame1.hasY()))
//            {
//                frame1Broken = true;
//                _log.debug("Первый кадр сопряжения \"" + frame1 + "\" вырожден при скруглении");
//            }
//            else
//                newFrames.add(frame1);
//            
//            if (frame2.getType() == CpFrame.FRAME_TYPE_UNKNOWN && (frame2.hasX() || frame2.hasY()))
//            {
//                frame2Broken = true;
//                _log.debug("Второй кадр сопряжения \"" + frame2 + "\" вырожден при скруглении");
//                k++;
//            }
//            
//            if (arcFrame != null)
//            {
//                newFrames.add(arcFrame);
//                roundedCouplings++;
//            }
//            else
//            {
//                if (frame1Broken)
//                {
//                    repairFrame(frame1);
//                    newFrames.add(frame1);
//                }
//                if (frame2Broken)
//                    repairFrame(frame2);
//            }
//        }
//        CachedCpFrame lastFrame = cpList.getFrame(cpList.getLength() - 1);
//        if ((lastFrame.getType() == CpFrame.FRAME_TYPE_UNKNOWN && (lastFrame.hasX() || lastFrame.hasY()) ) == false)
//            newFrames.add(lastFrame);
//
//        cpList.setData((CachedCpFrame[])newFrames.toArray(new CachedCpFrame[0]));
//
//        if (_shortArcsCount > 0)
//            _log.info("Не скруглено из-за малой длины дуги: " + _shortArcsCount);
//        
//        _log.info("Скруглено " + roundedCouplings + " сопряжений");
//        
//        //MTRUtils.checkCpList(cpList);
        
        return null;
    }

    private void repairFrame(CachedCpFrame frame)
    {
        if (frame.hasG02() || frame.hasG03())
            frame.setType(CpFrame.FRAME_TYPE_ARC);
        else if (frame.hasG00() || frame.hasG01())
            frame.setType(CpFrame.FRAME_TYPE_LINE);
        else
            throw new IllegalArgumentException("Frame \"" + frame + "\" is invalid");
    }

    /**Обработать (скруглить) сопряжение.
     * @param frame1 - первый кадр сопряжения
     * @param frame2 - второй кадр сопряжения
     * @param r - радиус скругления
     * @return - возвращает дугу если было произведено скругление, иначе - null
     */
    private CachedCpFrame roundCoupling(CachedCpFrame frame1, CachedCpFrame frame2, 
            int minMovementLength, int minGeoLengthAfterReducing, double minAngle, int eps)
    {
        if (frame1.hasG01() && frame2.hasG01())
            return roundLineToLineCoupling(frame1, frame2, minMovementLength, minGeoLengthAfterReducing, minAngle, eps);

        else if (frame1.hasG01() && frame2.getType() == CpFrame.FRAME_TYPE_ARC)
            return roundLineToArcCoupling(frame1, frame2, minMovementLength, minGeoLengthAfterReducing, minAngle, eps);

        else if (frame1.getType() == CpFrame.FRAME_TYPE_ARC && frame2.hasG01())
            return roundArcToLineCoupling(frame1, frame2, minMovementLength, minGeoLengthAfterReducing, minAngle, eps);

//        else if (frame1.getType() == CpFrame.FRAME_TYPE_ARC &&
//                 frame2.getType() == CpFrame.FRAME_TYPE_ARC)
//            return roundArcToArcCoupling(frame1, frame2, r);


        return null;
    }

    /**Вычислить радиус скругления на основании угла (в ГРАДУСАХ)
     * @param degreeAngle
     * @return
     */
    public static int calculateRoundingRadius(double degreeAngle, int eps)
    {
        return (int)Math.round(Math.abs(MathUtils.sin(degreeAngle / 2)) * eps);
    }
    
    private CachedCpFrame roundLineToLineCoupling(CachedCpFrame frame1, CachedCpFrame frame2, 
            int minMovementLength, int minGeoLengthAfterReducing, double minAngle, int eps)
    {
        int x1 = Utils.toInt(frame1.getDataByType(CC.X));
        int y1 = Utils.toInt(frame1.getDataByType(CC.Y));
        int x2 = Utils.toInt(frame2.getDataByType(CC.X));
        int y2 = Utils.toInt(frame2.getDataByType(CC.Y));

        double angle = getCrossAngle(new Point(-x1, -y1), new Point(x2, y2));
        _log.debug("Угол сопряжения: " + angle + " град.");

        if (MathUtils.compareDouble(Math.abs(angle), 180, CROSS_ANGLE_CORRIDOR))
            return null;

        if (-minAngle < angle && angle < minAngle)
        {
            _log.debug("Скругление отключено. Угол сопряжения меньше " + minAngle + " град.");
            return null;
        }
        
        int r = calculateRoundingRadius(angle, eps);
        _log.debug("Радиус скругления (линия-линия): " + r);

        double l, length1, length2;
        Point rNormal1, rNormal2;
        
        boolean recalc = false;
        while (true)
        {
            length1 = MathUtils.length(x1, y1);
            rNormal1 = new Point();
            MathUtils.normalToLine(new Point(x1, y1), r, false, rNormal1);
    
            length2 = MathUtils.length(x2, y2);
            rNormal2 = new Point();
            MathUtils.normalToLine(new Point(x2, y2), r, false, rNormal2);
    
            l = Math.abs((double)r / MathUtils.tan(angle / 2)); // величина, на которую будут уменьшины линии
            if ( (l >= length1 || l >= length2) && recalc == false)
            {
                recalc = true;
                eps = (int)Math.round(Math.min(length1, length2) / MathUtils.cos(angle/2));
                r = calculateRoundingRadius(angle, eps);
                _log.debug("Радиус скругления (линия-линия) уменьшен: " + r);
            }
            else
                break;
        }

        double k1 = (double)1 - l / length1;
        int x1New = (int)Math.round(x1 * k1);
        int y1New = (int)Math.round(y1 * k1);

        if (x1New == 0 && y1New == 0)
            frame1.setType(CpFrame.FRAME_TYPE_UNKNOWN);
//        else 
//            if (MathUtils.length(x1New, y1New) < minGeoLengthAfterReducing)
//                return null;
        
        double k2 = (double)1 - l / length2;
        int x2New = (int)Math.round(x2 * k2);
        int y2New = (int)Math.round(y2 * k2);
        
        if (x2New == 0 && y2New == 0)
            frame2.setType(CpFrame.FRAME_TYPE_UNKNOWN);
        else
            if (MathUtils.length(x2New, y2New) < minGeoLengthAfterReducing)
                return null;

        int g = 2;
        if (angle < 0)
        {
            g = 3;
        }
        else
        {
            rNormal1.inverse();
            rNormal2.inverse();
        }

        CachedCpFrame roundingFrame = MTRUtils.createArcFrame(g,
                rNormal1.x - rNormal2.x, rNormal1.y - rNormal2.y,
                rNormal1.x, rNormal1.y);

        if (roundingFrame.getEuclidLength() < minMovementLength)
        {
            _shortArcsCount++;
            return null;
        }
        
        if (frame1.getType() == CpFrame.FRAME_TYPE_LINE)
        {
            frame1.getSubFrameByType(CpSubFrame.RC_GEO_LINE).copy(MTRUtils.createLineSubFrame(1, x1New, y1New));
            frame1.setHasX(x1New != 0);
            frame1.setHasY(y1New != 0);
        }
        
        if (frame2.getType() == CpFrame.FRAME_TYPE_LINE)
        {
            frame2.getSubFrameByType(CpSubFrame.RC_GEO_LINE).copy(MTRUtils.createLineSubFrame(1, x2New, y2New));
            frame2.setHasX(x2New != 0);
            frame2.setHasY(y2New != 0);
        }

        return roundingFrame;
    }

    private double getCrossAngle(Point point1, Point point2)
    {
        double angle = MathUtils.calculateCrossAngle(point1, point2);
        angle = Math.toDegrees(angle);

        if (angle > 180)
            angle = angle - 360;
        else if (angle < -180)
            angle = 360 + angle;

        return angle;
    }

    private CachedCpFrame roundLineToArcCoupling(CachedCpFrame frame1, CachedCpFrame frame2, 
            int minMovementLength, int minGeoLengthAfterReducing, double minAngle, int eps)
    {
        int x1 = -Utils.toInt(frame1.getDataByType(CC.X));
        int y1 = -Utils.toInt(frame1.getDataByType(CC.Y));

        int x2 = Utils.toInt(frame2.getDataByType(CC.X));
        int y2 = Utils.toInt(frame2.getDataByType(CC.Y));
        int i = Utils.toInt(frame2.getDataByType(CC.I));
        int j = Utils.toInt(frame2.getDataByType(CC.J));

        Point rTangent2 = new Point();
        MathUtils.tangentToArc(new Point(x2, y2), new Point(i, j), frame2.hasG02(), 1000, true, true, rTangent2);
        
        double angle = getCrossAngle(new Point(x1, y1), rTangent2);
        _log.debug("Угол сопряжения: " + angle + " град.");

        if (MathUtils.compareDouble(Math.abs(angle), 180, CROSS_ANGLE_CORRIDOR))
            return null;
        
        if (-minAngle < angle && angle < minAngle)
        {
            _log.debug("Скругление отключено. Угол сопряжения меньше " + minAngle + " град.");
            return null;
        }
        int r = calculateRoundingRadius(angle, eps);
        _log.debug("Радиус скругления (линия-дуга | дуга-линия): " + r);
        
        double R = MathUtils.length(i, j);
        int lineLength = (int)Math.round(MathUtils.length(x1, y1));
        if ((r >= lineLength) || (R < r))
            return null;

//        double xyLength = MathUtils.length(x2, y2);
//        double max = (r*2 + Math.sqrt(r*r*4 - xyLength*xyLength)) / 2;
//        //if (MathUtils.calculateArcLength(x2, y2, i, j, frame2.hasG02()) < Math.PI*R)
//        if (max < r*2)
//            return null;

        if (MathUtils.compareDouble(Math.abs(angle), 180, CROSS_ANGLE_CORRIDOR))
            return null;

        int sgnAngle = 1; // зависимость от угла. переход через 180. инвертировать r.
        if (angle < 0)
            sgnAngle = -1;

        int sgnDirection = 1; // зависимость от направления дуги. инвертировать r.
        if (frame2.hasG02())
            sgnDirection = -1;

        // ! sgnAngle и sgnDirection действуют независимо

        double xyAngle = MathUtils.calculateAngle2(x1, y1);

//        int rotatedY2 = MathUtils.rotateCoords(xyAngle, x2, y2, false).y;
//        if (sgnAngle == 1 && rotatedY2 < r)
//            return null;
//        else if (sgnAngle == -1 && rotatedY2 > r)
//            return null;

        Point OO2 = MathUtils.rotateCoords(xyAngle, i, j, false);

        double remainsR = R + (double)sgnDirection * sgnAngle * r;
        if (remainsR <= 0)
        {
            _log.error("remainsR <= 0 : " + remainsR);
            return null;
        }

        Point intersectionPoint = MathUtils.calculateIntersectionLineToArc(
                new Point(lineLength , 0),
                new Point(OO2.x, OO2.y - sgnAngle * r),
                remainsR,
                new Point(), new Point(Integer.MAX_VALUE, Integer.MAX_VALUE));
                //new Point(newIJ.x - r, newIJ.y - r), new Point(newIJ.x, newIJ.y + r));

        if (intersectionPoint == null || intersectionPoint.isZero())
            return null;

        double k1 = R / remainsR;
        double k2 = (double)-1 * sgnDirection * sgnAngle * r / remainsR; //-1

        Point OO1 = new Point(intersectionPoint.x, intersectionPoint.y + sgnAngle * r);

        // k*O2O1
        Point O2A = new Point(
                (int)Math.round(k1 * (OO1.x - OO2.x)),
                (int)Math.round(k1 * (OO1.y - OO2.y)));

        Point O1A = new Point(
                (int)Math.round(k2 * (OO1.x - OO2.x)),
                (int)Math.round(k2 * (OO1.y - OO2.y)));

        Point OA = new Point(OO2.x + O2A.x, OO2.y + O2A.y);

        double theoreticalMinArcLength = MathUtils.calculateArcLength(OA.x, OA.y, OO2.x, OO2.y, frame2.hasG02());
        double realArcLength = MathUtils.calculateArcLength(x2, y2, i, j, frame2.hasG02());
        if (theoreticalMinArcLength + 100 > realArcLength)
        {
            _log.debug("Невозможно построить скругляющую дугу. Смежная дуга слишком мала.");
            return null;
        }
        
        int reducedLength = intersectionPoint.x;
        if (reducedLength >= lineLength)
            return null;

        CpSubFrame reducedLineSubFrame = MTRUtils.createLineSubFrame(
                1, reducedLength - lineLength, 0);
        
        OA.rotate(xyAngle, true);
        O2A.rotate(xyAngle, true);
        
        CpSubFrame reducedArcSubFrame = MTRUtils.createArcSubFrame(
                frame2.hasG02() ? 2 : 3, x2 - OA.x, y2 - OA.y, -O2A.x, -O2A.y);
        
        if (reducedLineSubFrame.getEuclidLength() < minGeoLengthAfterReducing ||
            reducedArcSubFrame.getEuclidLength() < minGeoLengthAfterReducing)
        {
            _log.debug("Скругление отключено. Длина редуцированного(ых) " +
                    "перемещения(ий) меньше " + minGeoLengthAfterReducing + " юнитов.");
            return null;
        }
        
        int g = 2;
        if (angle < 0)
            g = 3;

        CachedCpFrame roundingFrame = MTRUtils.createArcFrame(g,
                    MathUtils.rotateCoords(xyAngle, O1A.x, O1A.y + sgnAngle * r, true), // -1
                    MathUtils.rotateCoords(xyAngle, 0, sgnAngle * r, true)); // -1
        
        if (roundingFrame.getEuclidLength() < minMovementLength)
        {
            _shortArcsCount++;            
            return null;
        }
        
        // reduce line and arc
        CpSubFrame lineSubFrame = frame1.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
        lineSubFrame.setData(reducedLineSubFrame.getData());
        frame1.rotate(xyAngle, true);

        CpSubFrame arcSubFrame = frame2.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
        arcSubFrame.setData(reducedArcSubFrame.getData());

        return roundingFrame;
    }

    private CachedCpFrame roundArcToLineCoupling(CachedCpFrame frame1, CachedCpFrame frame2, 
            int minMovementLength, int minGeoLengthAfterReducing, double minAngle, int eps)
    {
        frame1.reverse();
        frame2.reverse();

        CachedCpFrame arcFrame = roundLineToArcCoupling(frame2, frame1, minMovementLength, minGeoLengthAfterReducing, minAngle, eps);
        if (arcFrame != null)
            arcFrame.reverse();

        frame1.reverse();
        frame2.reverse();

        return arcFrame;
    }

//    private CachedCpFrame roundArcToArcCoupling(CachedCpFrame frame1, CachedCpFrame frame2, int r)
//    {
//        int x1 = Utils.toInt(frame1.getDataByType(CC.X));
//        int y1 = Utils.toInt(frame1.getDataByType(CC.Y));
//        int i1 = Utils.toInt(frame1.getDataByType(CC.I));
//        int j1 = Utils.toInt(frame1.getDataByType(CC.J));
//
//        int x2 = Utils.toInt(frame2.getDataByType(CC.X));
//        int y2 = Utils.toInt(frame2.getDataByType(CC.Y));
//        int i2 = Utils.toInt(frame2.getDataByType(CC.I));
//        int j2 = Utils.toInt(frame2.getDataByType(CC.J));
//
//        Point rTangent1 = new Point();
//        MathUtils.tangentToArc(new Point(x1, y1), new Point(i1, j1), frame1.hasG02(), r, false, true, rTangent1);
//        rTangent1.inverse();
//
//        Point rTangent2 = new Point();
//        MathUtils.tangentToArc(new Point(x2, y2), new Point(i2, j2), frame2.hasG02(), r, true, true, rTangent2);
//
//        double angle = MathUtils.calculateCrossAngle(rTangent1, rTangent2);
//        if (MathUtils.compareDouble(Math.abs(angle), 180, CROSS_ANGLE_CORRIDOR))
//            return null;
//
//        double R1 = MathUtils.length(i1, j1);
//        double R2 = MathUtils.length(i2, j2);
//        if (angle > 0)
//        {
//            R1 += r;
//            R2 += r;
//        }
//        else
//        {
//            R1 -= r;
//            R2 -= r;
//        }
//
//        Point centerPoint2 = new Point(x1 - i1 + i2, y1 - j1 + j2);
//
//        CPList cpList1 = new CPList(new CachedCpFrame[] {frame1});
//        CPList cpList2 = new CPList(new CachedCpFrame[] {frame2});
//
//        Point min1 = cpList1.getMinPosition();
//        Point max1 = cpList1.getMaxPosition();
//        min1.x -= i1;
//        min1.y -= j1;
//        max1.x -= i1;
//        max1.y -= j1;
//
//        Point min2 = cpList2.getMinPosition();
//        Point max2 = cpList2.getMaxPosition();
//        min2.x += x1 - i2;
//        min2.y += y1 - j2;
//        max2.x += x1 - i2;
//        max2.y += y1 - j2;
//
//
//        Point intersectionPoint = MathUtils.calculateIntersectionArcToArc(R1, centerPoint2, R2, min1, max1, min2, max2);
//
//        if (intersectionPoint != null)
//        {
//            System.out.println("intersected");
//        }
//
//        return null;
//    }

}






