/*$Id: JSector.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 10:01:18
 */
public class JSector extends JMacros
{
    public static final String NAME = "Сектор";

    public void FillLineSizeList()
    {
        //Ring hasn't any line size except two radiuses
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(0, 0);
        cpList.addStartPoint(1, "StartPoint");
        cpList.addArc(2 * geoParameters.GetValue("R2"), 0, geoParameters.GetValue("R2"), 0, true, "R2");
        cpList.addStartPoint(2, "StartPoint");
        cpList.addLine(geoParameters.GetValue("R1") - geoParameters.GetValue("R2"), 0, "");
        cpList.addStartPoint(3, "StartPoint");
        cpList.addArc(-2 * geoParameters.GetValue("R1"), 0, -geoParameters.GetValue("R1"), 0, false, "R1");
        cpList.addStartPoint(4, "StartPoint");
        cpList.addLine(geoParameters.GetValue("R1") - geoParameters.GetValue("R2"), 0, "");
        shapeList.add(cpList);
    }

    public boolean CheckForCorrectSize()
    {
        if (geoParameters.GetValue("R1") >= geoParameters.GetValue("R2"))
            return false;
        return true;
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "Sector";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("R1", 200);
        geoParameters.AddParameter("R2", 500);
    }

}
