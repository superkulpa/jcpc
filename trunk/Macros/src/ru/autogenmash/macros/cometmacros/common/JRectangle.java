/*$Id: JRectangle.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 9:59:31
 */
public class JRectangle extends JMacros
{
    public static final String NAME = "Прямоугольник";

    public void FillLineSizeList()
    {
        lineSizeList.Add(0, -geoParameters.GetValue("B"),
                -geoParameters.GetValue("A"), -geoParameters.GetValue("B"),
                JLineSize.POS_RIGHT, "A");
        lineSizeList.Add(-geoParameters.GetValue("A"), 0,
                -geoParameters.GetValue("A"), -geoParameters.GetValue("B"),
                JLineSize.POS_DOWN, "B");
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(0, 0);
        cpList.addStartPoint(1, "StartPoint");
        cpList.addLine(0, geoParameters.GetValue("B"), "");
        cpList.addStartPoint(2, "StartPoint");
        cpList.addLine(geoParameters.GetValue("A"), 0, "");
        cpList.addStartPoint(3, "StartPoint");
        cpList.addLine(0, -geoParameters.GetValue("B"), "");
        cpList.addStartPoint(4, "StartPoint");
        cpList.addLine(-geoParameters.GetValue("A"), 0, "");
        shapeList.add(cpList);
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "Rectangle";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("A", 1500);
        geoParameters.AddParameter("B", 1000);
    }

    public boolean CheckForCorrectSize()
    {
        if ((geoParameters.GetValue("A") <= 0) || (geoParameters.GetValue("B") <= 0))
            return false;
        return true;
    }
}
