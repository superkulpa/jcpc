/*$Id: JEar.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.additional;

import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;
import ru.autogenmash.macros.cometmacros.JMath;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 9:50:00
 */
public class JEar extends JMacros
{
    public static final String NAME = "Проушина";

    public void FillLineSizeList()
    {
        lineSizeList.Add(-geoParameters.GetValue("A0"), 0, -geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"), JLineSize.POS_DOWN, "B0");
        lineSizeList.Add(-geoParameters.GetValue("A0"), 0, 0, -geoParameters.GetValue("B0") / 2, JLineSize.POS_LEFT, "A0");
        if (geoParameters.GetValue("R1") > 0)
        {
            lineSizeList.Add(-geoParameters.GetValue("A0"), 0, geoParameters.GetValue("A1") - geoParameters.GetValue("A0"), -geoParameters.GetValue("B1"), JLineSize.POS_UP, "B1");
            lineSizeList.Add(-geoParameters.GetValue("A0"), -geoParameters.GetValue("B0"), geoParameters.GetValue("A1") - geoParameters.GetValue("A0"), -geoParameters.GetValue("B1"), JLineSize.POS_RIGHT, "A1");
        }
    }

    public void FillShapeList()
    {
        Clear();
        JCPList CPList;
        if (geoParameters.GetValue("R1") > 0)
        {
            CPList = new JCPList(JCPList.HOLE);
            CPList.setMovementToULC(-geoParameters.GetValue("A0") + geoParameters.GetValue("A1"),
                    -geoParameters.GetValue("B1") + geoParameters.GetValue("R1"));

            CPList.addStartPoint(1, "StartPoint");
            CPList.addArc(0, 2 * geoParameters.GetValue("R1"), 0, geoParameters.GetValue("R1"), false, "");
            CPList.addArc(0, -2 * geoParameters.GetValue("R1"), 0, -geoParameters.GetValue("R1"), false, "R1");

            shapeList.add(CPList);
        }

        // создать основной внешний контур
        CPList = new JCPList(JCPList.SHAPE);
        CPList.setMovementToULC(-geoParameters.GetValue("A0"), 0);
        CPList.addStartPoint(1,"StartPoint");

        double l = Math.sqrt(JMath.Sqr(geoParameters.GetValue("A0") - geoParameters.GetValue("R2")) +
                JMath.Sqr(geoParameters.GetValue("B0") / 2));
        double d = Math.sqrt(JMath.Sqr(l) - JMath.Sqr(geoParameters.GetValue("R2")));
        double alpha = Math.acos(geoParameters.GetValue("B0") / (2 * l));
        double beta = Math.asin(geoParameters.GetValue("R2") / l);
        int x = (int)Math.round(d * Math.sin(alpha + beta));
        int y = (int)Math.round(d * Math.cos(alpha + beta));

        CPList.addLine(-x, y, "");
        CPList.addStartPoint(2, "StartPoint");
        CPList.addArc(0, Math.round(2 * (geoParameters.GetValue("B0") / 2 - y)),
                Math.round(x - geoParameters.GetValue("A0") + geoParameters.GetValue("R2")),
                Math.round(geoParameters.GetValue("B0") / 2 - y), true, "R2");
        CPList.addLine(x, y, "");
        CPList.addLine(0, -geoParameters.GetValue("B0"), "");

        shapeList.add(CPList);
    }

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "Ear";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("A0", 1500);
        geoParameters.AddParameter("A1", 600);
        geoParameters.AddParameter("B0", 1500);
        geoParameters.AddParameter("B1", 750);
        geoParameters.AddParameter("R1", 300);
        geoParameters.AddParameter("R2", 400);
    }

    public boolean CheckForCorrectSize()
    {
        if (geoParameters.GetValue("R1") >= geoParameters.GetValue("A1") ||
                geoParameters.GetValue("A1") + geoParameters.GetValue("R1") >= geoParameters.GetValue("A0") ||
                geoParameters.GetValue("B1") >= geoParameters.GetValue("B0"))
            return false;

        {
            int B1 = geoParameters.GetValue("B1");
            if (B1 > geoParameters.GetValue("B0")/2)
                B1 = geoParameters.GetValue("B0") - B1;

            double AO = Math.sqrt(MathUtils.sqr(geoParameters.GetValue("A0") - geoParameters.GetValue("R2")) +
                    MathUtils.sqr(geoParameters.GetValue("B0") / 2));
            double a1 = Math.atan(2*(geoParameters.GetValue("A0") - geoParameters.GetValue("R2")) / geoParameters.GetValue("B0"));
            double a2 = Math.asin(geoParameters.GetValue("R2") / AO);
            double a3 = Math.atan(geoParameters.GetValue("A1") / B1);
            double a4 = a1 + a2 - a3;
            double LK = Math.sin(a4) * Math.sqrt(MathUtils.sqr(geoParameters.GetValue("A1")) + MathUtils.sqr(B1));

            if (LK <= geoParameters.GetValue("R1"))
                return false;
        }

        return true;
    }
}
