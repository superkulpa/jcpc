package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:01:29
 */
public class ArcReplacingAction extends StepActionBase
{
    /** Длина шага при дуговой интерполяции. */
    public static final int ARC_INTERPOLATION_STEP = 10 * 100;

    public ArcReplacingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);

            if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
            {
                // если текущий кадр дуга, то заменить ее на линейную
                // интерполяцию и перейти к следующему кадру
                int interpX = 0;
                int interpY = 0;
                int interpI = 0;
                int interpJ = 0;
                boolean direction = false;
                if (frame.hasX())
                    interpX = frame.getDataByType(CC.X).intValue();
                if (frame.hasY())
                    interpY = frame.getDataByType(CC.Y).intValue();
                if (frame.hasI())
                    interpI = frame.getDataByType(CC.I).intValue();
                if (frame.hasJ())
                    interpJ = frame.getDataByType(CC.J).intValue();
                if (frame.hasG02())
                    direction = true;

                CpSubFrame[] arcFrames = MTRUtils.interpolateArc(interpX, interpY, interpI, interpJ, direction, ARC_INTERPOLATION_STEP, CC.X, CC.Y);
                int newLength = frame.getLength() - 1 + arcFrames.length;
                CpSubFrame[] subFrames = new CpSubFrame[newLength];

                for (int j = 0; j < frame.getLength(); j++)
                {
                    final CpSubFrame subFrame = frame.getSubFrame(j);
                    if (subFrame.hasCommand(CC.I) || subFrame.hasCommand(CC.J))
                    {
                        for (int k = 0; k < newLength; k++)
                        {
                            if (k < j)
                                subFrames[k] = frame.getSubFrame(k);
                            else if (k < arcFrames.length + j)
                                subFrames[k] = arcFrames[k - j];
                            else
                                subFrames[k] = frame.getSubFrame(k - arcFrames.length + 1);
                        }
                    }
                }

                frame.setData(subFrames);
            }
        }

        return null;
    }

    public String getDescription()
    {
        return "Интерполяция дуг линиями";
    }
}








