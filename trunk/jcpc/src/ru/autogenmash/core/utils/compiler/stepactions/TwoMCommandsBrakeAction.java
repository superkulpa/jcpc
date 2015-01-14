package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**Вставить маленькое перемещение между подряд идущими M командами.
 * Временный шаг. Реально это залипуха, т.к. qnx kernel разработчики не захотели 
 * устранять баг в ядре.
 * @author Dymarchuk Dmitry
 * 25.05.2010 13:19:48
 */
public class TwoMCommandsBrakeAction extends StepActionBase
{

    public TwoMCommandsBrakeAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
//        int correctValue = 1;
//        for (int l = 0; l < cpList.getLength() - 1; l++)
//        {
//            CachedCpFrame frame1 = cpList.getFrame(l);
//            if (frame1.hasM() == false || frame1.isGeo())
//                continue;
//            
//            CachedCpFrame frame2 = cpList.getFrame(l + 1);
//            if (frame2.hasM() == false || frame2.isGeo())
//                continue;
//
//            int mData = Utils.toInt(frame2.getDataByType(CC.M));
//            if (mData == 2 || mData == 17 || mData == 30)
//            	continue;
//            
//            CpSubFrame[] subFrames = new CpSubFrame[frame1.getLength() + 1];
//            for (int i = 0; i < subFrames.length; i++)
//                subFrames[i] = frame1.getSubFrame(i);
//            
//            subFrames[subFrames.length - 1] = MTRUtils.createLineSubFrame(1, correctValue, 0);
//            correctValue = - correctValue;
//            frame1.setData(subFrames);
//            frame1.setType(CpFrame.FRAME_TYPE_LINE);
//            frame1.setHasX(true);
//
////            int previousG = 1;
////            if (l > 0)
////                for (int p = l - 1; p >= 0; p--)
////                {
////                    CachedCpFrame previousFrame = cpList.getFrame(l);
////                    previousFrame.get
////                }
//            
//        }
        
        return null;
    }

    public String getDescription()
    {
        return "Разъединение подряд идущих M команд";
    }

}
