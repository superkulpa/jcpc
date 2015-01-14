package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

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

/**
 * @author Dymarchuk Dmitry
 * @version
 * 02.10.2007 15:39:45
 */
public class OptimizationAction extends StepActionBase
{
    /** В юнитах. */
    public static final int RADIUS_COMPARABLE_ACCURACY = 100;


    public OptimizationAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    /**
     * Корректировка центров окружностей проводится на этапе проверки радиусов 
     * на шаге ErrorCheckingAction. 
     */
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        int angle = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_OPTIMIZATION_ANGLE_COMPARE_ACCURACY));
        double accuracy = Math.toRadians((double)angle / 10);
        
        int minLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_OPTIMIZATION_MIN_MOVEMENT_LENGTH));
        minLength *= Compiler.SIZE_TRANSFORMATION_RATIO;
        
        boolean combineSimilarDirectionArcs = (new Boolean((String)cpParameters.
            getValue(Compiler.PARAM_OPTIMIZATION_COMBINE_SIMILAR_DIRECTION_ARCS))).booleanValue();
        
        // 1) выродить мелкие радиуса
        String result;
        result = transformSmallArcs(cpList, minLength);
        if (result != null)
            return new CompilerError(result);

        // 2) объеденить коллинеарные линейные перемещения
        result = combineSimilarDirectionLines(cpList, accuracy);
        if (result != null)
            return new CompilerError(result);
        
        if (combineSimilarDirectionArcs)
        {
            // 3) объеденить коллинеарные дуговые перемещения
            result = combineSimilarDirectionAndRadiusArcs(cpList, accuracy);
            if (result != null)
                return new CompilerError(result);
        }

        return null;
    }

    public String getDescription()
    {
        return "Первичная геометрическая оптимизация";
    }
    
    /**Преобразовать все "маленькие" дуги в линии
     * @param cpList
     * @return
     */
    private String transformSmallArcs(CPList cpList, int minLength)
    {
        int transformedArcCount = 0;

        for (int l = 0; l < cpList.getLength(); l++)
        {
            CachedCpFrame frame = cpList.getFrame(l);
            if (frame.getType() != CpFrame.FRAME_TYPE_ARC)
                continue;

            for (int m = 0; m < frame.getLength(); m++)
            {
                CpSubFrame subFrame = frame.getSubFrame(m);
                if (subFrame.getType() == CpSubFrame.RC_GEO_ARC)
                {
                    int x = 0;
                    int y = 0;
                    int i = 0;
                    int j = 0;
                    for (int k = 0; k < subFrame.getLength(); k++)
                    {
                        CC cc = subFrame.getCC(k);
                        switch (cc.getType())
                        {
                        case CC.X:
                            x = cc.getData();
                            break;
                        case CC.Y:
                            y = cc.getData();
                            break;
                        case CC.I:
                            i = cc.getData();
                            break;
                        case CC.J:
                            j = cc.getData();
                            break;
                        }
                    }

                    //double r = MathUtils.length(i, j);
                    double length = MathUtils.calculateArcLength(x, y, i, j, frame.hasG02());
                    if (length < minLength/* || r < DEFAULT_MIN_ARC_RADIUS*/)
                    {
                        //проверка на полную дугу
                        if(x == 0 && y == 0)
                        MTRUtils.transformArcToLine(frame, 1, x, y);
                        transformedArcCount++;
                    }
                }
            }
        }

        _log.info("Преобразовано \"маленьких\" дуг в линии: " + transformedArcCount);

        return null;
    }

    private String combineSimilarDirectionLines(CPList cpList, double angleAccuracy)
    {
        for (int l = 0; l < cpList.getLength() - 1; l++)
        {
            CachedCpFrame frame1 = cpList.getFrame(l);
            CachedCpFrame frame2 = cpList.getFrame(l + 1);
            
            if (frame1 == null || frame2 == null)
                continue;
            
            if (frame1.hasG01() == false || frame1.getLength() > 1)
                continue;
            
            if (frame2.hasG01() == false || frame2.getLength() > 1)
                continue;

            int x1 = Utils.toInt(frame1.getDataByType(CC.X));
            int y1 = Utils.toInt(frame1.getDataByType(CC.Y));
            if((x1 == 0) && (y1 == 0)) continue;
            double k1 = MathUtils.calculateAngle2(x1, y1);

            int x2 = Utils.toInt(frame2.getDataByType(CC.X));
            int y2 = Utils.toInt(frame2.getDataByType(CC.Y));
            if((x2 == 0) && (y2 == 0)) continue;
            double k2 = MathUtils.calculateAngle2(x2, y2);
            //проверяем только на полное совпадение(1 mm на 24 m)
            if (MathUtils.compareDouble(k1, k2, 0.00001/*angleAccuracy*/))
            {
                CpSubFrame subFrame = frame2.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
                int newX = x1 + x2;
                int newY = y1 + y2;
                if (newX == 0 && newY == 0)
                {
                    _log.warn("Сопряжение \"туда-обратно\": \"" + frame1 + "\" и \"" + frame2 + "\" не объединено");
                    return null;
                }
                subFrame.setData(MTRUtils.createLineSubFrame(1, newX, newY).getData());
                frame2.setHasX(newX != 0);
                frame2.setHasY(newY != 0);
                
                cpList.setFrame(l, null);
            }
        }

        // TODO при сильной загрузке процессора перенести defragment() в метод execute()
        // вызывать его всего один раз за этот шаг
        // проверять null кадры
        int similarMovementsCount = cpList.defragment();
        _log.info("Объеденино " + similarMovementsCount + " колинеарных линий");

        return null;
    }

    /**Объединять дуги с одним направлением обхода, радиусом и углом между ними близким к 180.
     * @param cpList
     * @return
     */
    private String combineSimilarDirectionAndRadiusArcs(CPList cpList, double angleAccuracy)
    {
        
        for (int l = 0; l < cpList.getLength() - 1; l++)
        {
            CachedCpFrame frame1 = cpList.getFrame(l);
            CachedCpFrame frame2 = cpList.getFrame(l + 1);
            
            if (frame1 == null || frame2 == null)
                continue;
            
            if (frame1.getType() != CpFrame.FRAME_TYPE_ARC || frame1.getLength() > 1)
                continue;
            
            if (frame2.getType() != CpFrame.FRAME_TYPE_ARC || frame2.getLength() > 1)
                continue;
            
            if (frame1.hasG02() != frame2.hasG02())
                continue;
            
            double r1 = frame1.getArcRadius();
            double r2 = frame2.getArcRadius();
            if (MathUtils.compareDouble(r1, r2, RADIUS_COMPARABLE_ACCURACY) == false)
                continue;
            
            int x1 = Utils.toInt(frame1.getDataByType(CC.X));
            int y1 = Utils.toInt(frame1.getDataByType(CC.Y));
            int i1 = Utils.toInt(frame1.getDataByType(CC.I));
            int j1 = Utils.toInt(frame1.getDataByType(CC.J));
            
            int x2 = Utils.toInt(frame2.getDataByType(CC.X));
            int y2 = Utils.toInt(frame2.getDataByType(CC.Y));
            int i2 = Utils.toInt(frame2.getDataByType(CC.I));
            int j2 = Utils.toInt(frame2.getDataByType(CC.J));
            
            Point rTangent1 = new Point();
            MathUtils.tangentToArc(new Point(x1, y1), new Point(i1, j1), frame1.hasG02(), 1000, false, true, rTangent1);
            
            Point rTangent2 = new Point();
            MathUtils.tangentToArc(new Point(x2, y2), new Point(i2, j2), frame2.hasG02(), 1000, true, true, rTangent2);
            rTangent2.inverse();
            
            double angle = Math.abs(MathUtils.calculateCrossAngle(rTangent1, rTangent2));
            if (MathUtils.compareDouble(angle, Math.PI, angleAccuracy))
            {
                int g = frame1.hasG02() ? 2 : 3;
                int newX = x1 + x2;
                int newY = y1 + y2;
                
                if (newX == 0 && newY == 0)
                {
                    _log.info("Обединение колинеарных дуг пропущено для кадров \"" + 
                            frame1 + "\" и \"" + frame2 + "\", т.к. они дают полную дугу");
                    continue;
                }
                
                int newI = (int)Math.round(((double)i1 + i2 + x1) / 2);
                int newJ = (int)Math.round(((double)j1 + j2 + y1) / 2);
                
                CpSubFrame subFrame = frame2.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
                subFrame.setData(MTRUtils.createArcSubFrame(g, newX, newY, newI, newJ).getData());
                String result = MTRUtils.correctFrameArcCenter(frame2);
                if (result != null)
                    return result;
                cpList.setFrame(l, null);
            }
        }
        
        // TODO при сильной загрузке процессора перенести defragment() в метод execute()
        // вызывать его всего один раз за этот шаг
        // проверять null кадры
        int similarMovementsCount = cpList.defragment();
        _log.info("Объеденино " + similarMovementsCount + " колинеарных дуг");
        
        return null;
    }
    
}
