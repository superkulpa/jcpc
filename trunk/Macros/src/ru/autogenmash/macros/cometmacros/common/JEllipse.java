/*$Id: JEllipse.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 9:55:03
 */
public class JEllipse extends JMacros
{
    public static final String NAME = "Овал";

    public void FillLineSizeList()
    {
        lineSizeList.Add(-geoParameters.GetValue("R"), -geoParameters.GetValue("R"),
                -geoParameters.GetValue("R"), -geoParameters.GetValue("R") - geoParameters.GetValue("B"),
                JLineSize.POS_DOWN, "B");
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(-2 * geoParameters.GetValue("R"), -geoParameters.GetValue("R"));

        cpList.addStartPoint(1, "StartPoint");
        cpList.addArc(-2 * geoParameters.GetValue("R"), 0,
                -geoParameters.GetValue("R"), 0, true, "R");
        cpList.addStartPoint(2, "StartPoint");
        cpList.addLine(0, geoParameters.GetValue("B"), "");
        cpList.addStartPoint(3, "StartPoint");
        cpList.addArc(2 * geoParameters.GetValue("R"), 0,
                geoParameters.GetValue("R"), 0, true, "R");
        cpList.addStartPoint(4, "StartPoint");
        cpList.addLine(0, -geoParameters.GetValue("B"), "");
        shapeList.add(cpList);
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "Ellipse";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("B", 500);
        geoParameters.AddParameter("R", 500);
    }

    public boolean CheckForCorrectSize()
    {
        if (geoParameters.GetValue("R") <= 0)
            return false;
        return true;
    }
}
