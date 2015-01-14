package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * @author Dymarchuk Dmitry
 * 01.10.2009 16:19:05
 */
public class ScaleRotateInverseAction extends StepActionBase
{

    public ScaleRotateInverseAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        int scale = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_RSI_SCALE));
        double angle = Double.parseDouble((String)cpParameters.getValue(Compiler.PARAM_RSI_ROTATION_ANGLE));
        int inverse = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_RSI_INVERSE));

//        scale = scale / Compiler.SIZE_TRANSFORMATION_RATIO;
//        angle = angle / Compiler.SIZE_TRANSFORMATION_RATIO / 10;

        if (scale != 100)
            cpList.scale((double)scale / 100);

        if (angle != 0)
            cpList.rotate(Math.toRadians(angle / 10), true);
        if ((inverse & 1) != 0)
          cpList.reverseByX();
        if((inverse & 2) != 0)
        	cpList.reverseByY();
        
        return null;
    }

    public String getDescription()
    {
        return "Масштабирование, поворот, переворот контура";
    }
}




















