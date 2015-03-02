/*$Id: JTrapezoidMultihead.java,v 1.2 2012/12/11 11:47:28 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros.additional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ru.autogenmash.gui.SWTFormBase;
import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 17.09.2007 14:28:47
 */
public class JTrapezoidMultihead extends JMacros
{
    public static final String NAME = "Трапеция 2";
    public static final int DISTANCE_BETWEEN_SUPPORTS = 3000;
    public static int B2 = 0;
    public boolean CheckForCorrectSize()
    {
        if ((geoParameters.GetValue("A0") <= 0) || (geoParameters.GetValue("B0") <= DISTANCE_BETWEEN_SUPPORTS))
            return false;
        if (geoParameters.GetValue("B1") >= geoParameters.GetValue("B0") 
        || (geoParameters.GetValue("B1") <= DISTANCE_BETWEEN_SUPPORTS))
            return false;
        return true;
    }

    public void FillLineSizeList()
    {
        B2 = (geoParameters.GetValue("B0") - geoParameters.GetValue("B1")) / 2;
        lineSizeList.Add(0, -geoParameters.GetValue("B0") + geoParameters.GetValue("B1") + B2/*geoParameters.GetValue("B2")*/,
                0, B2 - geoParameters.GetValue("B0"), JLineSize.POS_UP, "B1");
        lineSizeList.Add(0, B2 - geoParameters.GetValue("B0"),
                -geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"), JLineSize.POS_UP, "B2");

        lineSizeList.Add(0, B2 - geoParameters.GetValue("B0"),
                -geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"), JLineSize.POS_RIGHT, "A0");
        lineSizeList.Add(-geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"),
                -geoParameters.GetValue("A0"), 0, JLineSize.POS_DOWN, "B0");
    }

    public void FillShapeList()
    {
        Clear();
        B2 = (geoParameters.GetValue("B0") - geoParameters.GetValue("B1")) / 2;
        JCPList CPList = new JCPList(JCPList.SHAPE);
        CPList.setMovementToULC(0, -geoParameters.GetValue("B0") + geoParameters.GetValue("B1") + B2);//geoParameters.GetValue("B2"));

        CPList.addLine(0, geoParameters.GetValue("B1"), "");

        CPList.addLine(geoParameters.GetValue("A0"), B2, "");

        CPList.addStartPoint(1, "StartPoint");
        CPList.addLine(0, -geoParameters.GetValue("B0"), "");

        CPList.addLine(-geoParameters.GetValue("A0"),
                geoParameters.GetValue("B0") - geoParameters.GetValue("B1") - B2/*geoParameters.GetValue("B2")*/, "");
                
        CPList.setMovementFromLeadInToULC(0,0);
        CPList.setMovementFromLeadOutToULC(0,0);
        shapeList.add(CPList);
        
    }

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
            OutputStream f = new FileOutputStream(OUTPUT_DIR + "/" + cpName + OUTPUT_EXTENSION);
            f.write(("%" + CPName + "\n").getBytes());
            //все делаем исходя из параметров
            //фиксируемся на прямую ветку
            String tmp = "M91T1T2";
            f.write(tmp.getBytes());
            //пробивка 1 резаком, и выводим второй резак в точку поиска листа
            tmp = "\nM81T1G01Y-" + String.valueOf(DISTANCE_BETWEEN_SUPPORTS);
            f.write(tmp.getBytes());
            //фиксируемся на портал 2
            tmp = "\nM93T2";
            f.write(tmp.getBytes());
            tmp = "\nM700.1T2";
            f.write(tmp.getBytes());
            //ищем лист и доезжаем
            tmp = "\nY-" + String.valueOf(geoParameters.GetValue("B0") - DISTANCE_BETWEEN_SUPPORTS);
            f.write(tmp.getBytes());
            //фиксируемся на обратную ветвь
            tmp = "\nM92T2";
            f.write(tmp.getBytes());
            //пробивка с хода и режем стороны
            tmp = "\nM73T2";
            f.write(tmp.getBytes());
            //петля сначала
            tmp = "\n//Начало петли, для отключения взять в ()";
            f.write(tmp.getBytes());
            tmp = "\nG01Y-100M19";
            f.write(tmp.getBytes());
            tmp = "\nG03Y100J50M19";
            f.write(tmp.getBytes());
            tmp = "\n//Конец петли";
            f.write(tmp.getBytes());
            //сторона
            tmp = "\nG01X-" + String.valueOf(geoParameters.GetValue("A0"))
            + "Y" + String.valueOf(B2);
            f.write(tmp.getBytes());
            //тушимся с хода 2 
            tmp = "\nM84T2";
            f.write(tmp.getBytes());
            //подъезжаем 1 ко 2
            tmp = "\nM93T2Y" + String.valueOf(geoParameters.GetValue("B1") - DISTANCE_BETWEEN_SUPPORTS);
            f.write(tmp.getBytes());
            //на прямую ветку и доезжаем
            tmp = "\nM91T2Y" + String.valueOf(DISTANCE_BETWEEN_SUPPORTS);
            f.write(tmp.getBytes());
            //выход в исходную по Y
            tmp = "\nM83G00Y" + String.valueOf(B2);
            f.write(tmp.getBytes());
//            //ищем лист и еде
//            for (int i = 0; i < generalTrajectory.getLength(); i++) {
//                //f.write(generalTrajectory.GetCC(i).GenerateCPString().getBytes());
//              String tmp = generalTrajectory.GetCC(i).GenerateCPString();
//            }
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
    
    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "TrapezoidMultihead";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("A0", 2000);
        geoParameters.AddParameter("B0", 5000);
        geoParameters.AddParameter("B1", 4000);
        B2 = (geoParameters.GetValue("B0") - geoParameters.GetValue("B1")) / 2;
    }

}
