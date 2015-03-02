/*$Id: JNHoledRectangle.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros.additional;

import ru.autogenmash.macros.cometmacros.JCPList;
import ru.autogenmash.macros.cometmacros.JLineSize;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 9:44:07
 */
public final class JNHoledRectangle extends JMacros
{
    public static final String NAME = "Обойма";

    public void InitAdditionalParameters()
    {
        macrosName = NAME;
        CPName = "NHoledRectangle";
    }

    public void InitGeoParameters()
    {
        setDistanceBetweenShapes(300); // расстояние между деталями
        geoParameters.AddParameter("A0", 1500);
        geoParameters.AddParameter("A1", 400);
        geoParameters.AddParameter("A2", 400);
        geoParameters.AddParameter("B0", 1500);
        geoParameters.AddParameter("B1", 220);
        geoParameters.AddParameter("B2", 220);
        geoParameters.AddParameter("B3", 220);
        geoParameters.AddParameter("R1", 300);
        geoParameters.AddParameter("R2", 120);
        geoParameters.AddParameter("N", 2);
    }

    public boolean CheckForCorrectSize()
    {
        if ((geoParameters.GetValue("A0") <= 0) || (geoParameters.GetValue("B0") <= 0))
            return false;
        int N = geoParameters.GetValue("N");
        if (N > 0)
        {
            if (geoParameters.GetValue("B3") <= 0)
                return false;
            if (geoParameters.GetValue("A1") + 2 * geoParameters.GetValue("R1") +
                    geoParameters.GetValue("A2") >= geoParameters.GetValue("A0"))
                return false;
        }
        if (N == 1)
        {
            if (geoParameters.GetValue("B1") + N * (2*geoParameters.GetValue("R2") + geoParameters.GetValue("B2"))
                    >= geoParameters.GetValue("B0"))
                return false;
        }
        else if (N > 1)
        {
            if (geoParameters.GetValue("B1") + N * (2*geoParameters.GetValue("R2") + geoParameters.GetValue("B2")) +
                    (N - 1)* geoParameters.GetValue("B3") >= geoParameters.GetValue("B0"))
                return false;
            if ( (geoParameters.GetValue("B2") == 0 && geoParameters.GetValue("R2") == 0) ||
                    (geoParameters.GetValue("A2") == 0 && geoParameters.GetValue("R2") == 0) )
                return false;
        }
        if (2 * geoParameters.GetValue("R1") >= Math.min(geoParameters.GetValue("A0"), geoParameters.GetValue("B0")))
            return false;

        return true;
    }

    public void FillLineSizeList()
    {
        lineSizeList.Add(0, -geoParameters.GetValue("B0") + geoParameters.GetValue("R1"),
                -geoParameters.GetValue("A0"), -geoParameters.GetValue("B0") + geoParameters.GetValue("R1"),
                JLineSize.POS_RIGHT, "A0");
        lineSizeList.Add(0, -geoParameters.GetValue("R1"),
                -geoParameters.GetValue("A1"), -geoParameters.GetValue("B1") - geoParameters.GetValue("R2"),
                JLineSize.POS_LEFT, "A1");
        lineSizeList.Add(-geoParameters.GetValue("A1") - geoParameters.GetValue("R2"), -geoParameters.GetValue("B1"),
                -geoParameters.GetValue("A1") - geoParameters.GetValue("A2") - geoParameters.GetValue("R2"), -geoParameters.GetValue("B1"),
                JLineSize.POS_LEFT, "A2");
        lineSizeList.Add(-geoParameters.GetValue("A0") + geoParameters.GetValue("R1"), 0,
                -geoParameters.GetValue("A0") + geoParameters.GetValue("R1"), -geoParameters.GetValue("B0"),
                JLineSize.POS_DOWN, "B0");
        lineSizeList.Add(-geoParameters.GetValue("R1"), 0,
                -geoParameters.GetValue("A1") - geoParameters.GetValue("R2"), -geoParameters.GetValue("B1"),
                JLineSize.POS_UP, "B1");
        lineSizeList.Add(-geoParameters.GetValue("A1"), -geoParameters.GetValue("B1") - geoParameters.GetValue("R2"),
                -geoParameters.GetValue("A1"), -geoParameters.GetValue("B1") - geoParameters.GetValue("B2") - geoParameters.GetValue("R2"),
                JLineSize.POS_UP, "B2");
        lineSizeList.Add(-geoParameters.GetValue("A1") - geoParameters.GetValue("R2"),
                -geoParameters.GetValue("B1") - geoParameters.GetValue("B2") - 2 * geoParameters.GetValue("R2"),
                -geoParameters.GetValue("A1") - geoParameters.GetValue("R2"),
                -geoParameters.GetValue("B1") - geoParameters.GetValue("B2") - 2 * geoParameters.GetValue("R2") - geoParameters.GetValue("B3"),
                JLineSize.POS_UP, "B3");
    }

    public void FillShapeList()
    {
        Clear();
        JCPList cpList;
        for (int i = 0; i < geoParameters.GetValue("N"); i++)
        {
            cpList = new JCPList(JCPList.HOLE);
            cpList.setMovementToULC(-geoParameters.GetValue("A1") - geoParameters.GetValue("R2"),
                    -geoParameters.GetValue("B1") - i * (2 * geoParameters.GetValue("R2") +
                            geoParameters.GetValue("B2") + geoParameters.GetValue("B3")));

            int centerPoint = (int)geoParameters.GetValue("A2") / 2;
            cpList.addLine(centerPoint, 0, "");
            cpList.addStartPoint(1, "StartPoint");
            cpList.addLine(geoParameters.GetValue("A2") - centerPoint, 0, "");
            cpList.addArc(geoParameters.GetValue("R2"), geoParameters.GetValue("R2"),
                    0, geoParameters.GetValue("R2"), false, "");
            cpList.addLine(0, geoParameters.GetValue("B2"), "");
            cpList.addArc(-geoParameters.GetValue("R2"), geoParameters.GetValue("R2"),
                    -geoParameters.GetValue("R2"), 0, false, (i == 0 ? "R2" : ""));
            cpList.addLine(-geoParameters.GetValue("A2"), 0, "");
            cpList.addArc(-geoParameters.GetValue("R2"), -geoParameters.GetValue("R2"),
                    0, -geoParameters.GetValue("R2"), false, "");
            cpList.addLine(0, -geoParameters.GetValue("B2"), "");
            cpList.addArc(geoParameters.GetValue("R2"), -geoParameters.GetValue("R2"),
                    geoParameters.GetValue("R2"), 0, false, "");
            shapeList.add(cpList);
        }

        // создать основной внешний контур
        cpList = new JCPList(JCPList.SHAPE);
        cpList.setMovementToULC(0, -geoParameters.GetValue("R1"));
        cpList.addStartPoint(1, "StartPoint");
        cpList.addLine(0, geoParameters.GetValue("B0") - 2 * geoParameters.GetValue("R1"), "");
        cpList.addArc(geoParameters.GetValue("R1"), geoParameters.GetValue("R1"),
                geoParameters.GetValue("R1"), 0, true, "");
        cpList.addStartPoint(2, "StartPoint");
        cpList.addLine(geoParameters.GetValue("A0") - 2 * geoParameters.GetValue("R1"), 0, "");
        cpList.addArc(geoParameters.GetValue("R1"), -geoParameters.GetValue("R1"),
                0, -geoParameters.GetValue("R1"), true, "R1");
        cpList.addStartPoint(3, "StartPoint");
        cpList.addLine(0, -geoParameters.GetValue("B0") + 2 * geoParameters.GetValue("R1"), "");
        cpList.addArc(-geoParameters.GetValue("R1"), -geoParameters.GetValue("R1"),
                -geoParameters.GetValue("R1"), 0, true, "");
        cpList.addStartPoint(4, "StartPoint");
        cpList.addLine(-geoParameters.GetValue("A0") + 2 * geoParameters.GetValue("R1"), 0, "");
        cpList.addArc(-geoParameters.GetValue("R1"), geoParameters.GetValue("R1"),
                0, geoParameters.GetValue("R1"), true, "");
        shapeList.add(cpList);
    }
}
