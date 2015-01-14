package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;
import ru.autogenmash.core.utils.compiler.PipeCutterParamsParser;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 25.07.2007 15:01:04
 */
public class CheckG59CommandAction extends StepActionBase
{

    
    public CheckG59CommandAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    /* (non-Javadoc)
     * @see ru.autogenmash.core.utils.compiler.IStepAction#execute(ru.autogenmash.core.CPList, ru.autogenmash.core.CpParameters)
     */
    //Вставить коррекцию подачи на радиусах при резе с наклоненным резаком.
    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        
        try
        {
            // покадровая подготовительная обработка CPList
            for (int i = 0; i < cpList.getLength(); i++)
            {
                CpFrame frame = cpList.getFrame(i);
                CpSubFrame subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_INFO);
                if (subFrame != null) {
                	CC[] data = subFrame.getData();
                	String dataStr = null;
                	for (int k = 0; k < data.length; k++)
           		     if (data[k].getType() == CC.G) {
           		       dataStr = data[k].getDescription();
           		       break;
           		     }
                	if(dataStr != null) {
                		Process process = Runtime.getRuntime().exec("./db.sh -c " + dataStr + " check");
                		int res = process.waitFor();
                		if (res != 0)
                			return new CompilerError(i, "Ошибка при обработке команды газ.консоли");
                	}
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new CompilerError("Ошибка при обработке команды газ.консоли");
        }

        return null;
    }	

    public String getDescription()
    {
        return "Проверка команд автогазконсоли";
    }

}
