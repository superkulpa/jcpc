/*$Id: SlantRectangle.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 17.09.2007 14:28:25
 */
public class SlantRectangle extends JMacros
{
    public static final String NAME = "Ребро";

    public boolean CheckForCorrectSize()
    {
        if ((geoParameters.GetValue("A0") <= 0) || (geoParameters.GetValue("B0") <= 0))
            return false;
        return true;
    }

    public void FillLineSizeList()
    {
        lineSizeList.Add(0, 0, 0, -geoParameters.GetValue("B1"), JLineSize.POS_UP, "B1");
        lineSizeList.Add(-geoParameters.GetValue("A0") + geoParameters.GetValue("A1"), -geoParameters.GetValue("B0"),
                -geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"), JLineSize.POS_RIGHT, "A1");
        lineSizeList.Add(-geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"),
                -geoParameters.GetValue("A0"), 0, JLineSize.POS_DOWN, "B0");
        lineSizeList.Add(-geoParameters.GetValue("A0"), 0, 0, 0, JLineSize.POS_LEFT, "A0");
    }

    public void FillShapeList()
    {
        Clear();
        JCPList CPList = new JCPList(JCPList.SHAPE);
        CPList.setMovementToULC(0, 0);

        CPList.addStartPoint(1, "StartPoint");
        CPList.addLine(0, geoParameters.GetValue("B1"), "");

        CPList.addStartPoint(2, "StartPoint");
        CPList.addLine(geoParameters.GetValue("A0") - geoParameters.GetValue("A1"),
                geoParameters.GetValue("B0") - geoParameters.GetValue("B1"), "");

        CPList.addStartPoint(3, "StartPoint");
        CPList.addLine(geoParameters.GetValue("A1"), 0, "");

        CPList.addStartPoint(4, "StartPoint");
        CPList.addLine(0, -geoParameters.GetValue("B0"), "");

        CPList.addStartPoint(5, "StartPoint");
        CPList.addLine(-geoParameters.GetValue("A0"), 0, "");

        shapeList.add(CPList);
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "SlantRectangle";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("A0", 1500);
        geoParameters.AddParameter("A1", 700);
        geoParameters.AddParameter("B0", 1000);
        geoParameters.AddParameter("B1", 500);
    }

}
