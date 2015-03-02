/*$Id: JRing.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.common;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 10:00:13
 */
public class JRing extends JMacros
{
    public static final String NAME = "Фланец";

    public void FillLineSizeList()
    {
        //Ring hasn't any line size except shape and hole radiuses
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList;
        if (geoParameters.GetValue("R1") > 0)
        {
            cpList = new JCPList(JCPList.HOLE);
            cpList.setMovementToULC(-geoParameters.GetValue("R2"), -geoParameters.GetValue("R2") + geoParameters.GetValue("R1"));
            cpList.addStartPoint(1, "StartPoint");
            cpList.addArc(0, 2 * geoParameters.GetValue("R1"), 0, geoParameters.GetValue("R1"), false, "");
            cpList.addArc(0, -2 * geoParameters.GetValue("R1"), 0, -geoParameters.GetValue("R1"), false, "R1");
            shapeList.add(cpList);
        }
        cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(-geoParameters.GetValue("R2"), 0);
        cpList.addStartPoint(1, "StartPoint");
        cpList.addArc(0, 2 * geoParameters.GetValue("R2"), 0, geoParameters.GetValue("R2"), true, "");
        cpList.addStartPoint(2, "StartPoint");
        cpList.addArc(0, -2 * geoParameters.GetValue("R2"), 0, -geoParameters.GetValue("R2"), true, "R2");
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
        CPName = "Ring";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("R1", 200);
        geoParameters.AddParameter("R2", 500);
    }
}
