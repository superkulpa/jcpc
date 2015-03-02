/*$Id: JCircle.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 9:53:28
 */
public class JCircle extends JMacros
{
    public static final String NAME = "Круг";

    public void FillLineSizeList()
    {
        // Circle hasn't any line size except radius
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(-geoParameters.GetValue("R"), 0);
        cpList.addStartPoint(1, "StartPoint");
        cpList.addArc(0, 2 * geoParameters.GetValue("R"),
                0, geoParameters.GetValue("R"), true, "R");
        cpList.addArc(0, -2 * geoParameters.GetValue("R"),
                0, -geoParameters.GetValue("R"), true, "");
        shapeList.add(cpList);
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "Circle";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("R", 500);
    }

    public boolean CheckForCorrectSize()
    {
        if (geoParameters.GetValue("R") <= 0)
            return false;
        return true;
    }
}
