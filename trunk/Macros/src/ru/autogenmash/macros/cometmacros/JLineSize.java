/*$Id: JLineSize.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros;

import ru.autogenmash.core.Point;

/**Класс описывающий один линейный размер.
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 10:16:57
 */
public class JLineSize
{
    public static final int POS_UP = 1;
    public static final int POS_DOWN = 2;
    public static final int POS_LEFT = 3;
    public static final int POS_RIGHT = 4;

    /** Первая точка линейного размера, относительно левого верхнего угла макроса. */
    protected Point point1;
    /** Вторая точка линейного размера, относительно левого верхнего угла макроса. */
    protected Point point2;
    /** С какой стороны рисовать размерные линии. */
    protected int position;
    /** Текстовая иформация, характеризующая размер. */
    protected String description;

    public JLineSize(Point _point1, Point _point2, int _position, String _description)
    {
        point1 = new Point(_point1.x, _point1.y);
        point2 = new Point(_point2.x, _point2.y);
        description = _description;
        position = _position;
    }

    public JLineSize(int _x1, int _y1, int _x2, int _y2, int _position, String _description)
    {
        point1 = new Point(_x1, _y1);
        point2 = new Point(_x2, _y2);
        description = _description;
        position = _position;
    }

    public Point GetPoint1()
    {
        return point1;
    }

    public Point GetPoint2()
    {
        return point2;
    }

    public int GetPosition()
    {
        return position;
    }

    public String GetDescription()
    {
        return description;
    }

    /**Вычислить линии для отрисовки, на основании shapeSize, точек привязки к левому верхнему углу и стороне
     * @param shapeSize габарит контура
     * @param delta
     * @param line1
     * @param line2
     */
    public void CalculateLines(Point shapeSize, int delta, Point line1, Point line2)
    {
        switch (position)
        {
        case POS_UP:
            line1.y = 0;
            line1.x = point1.x - delta;
            line2.y = 0;
            line2.x = point2.x - delta;
            break;
        case POS_DOWN:
            line1.y = 0;
            line1.x = shapeSize.x + point1.x + delta;
            line2.y = 0;
            line2.x = shapeSize.x + point2.x + delta;
            break;
        case POS_LEFT:
            line1.y = point1.y - delta;
            line1.x = 0;
            line2.y = point2.y - delta;
            line2.x = 0;
            break;
        case POS_RIGHT:
            line1.y = shapeSize.y + point1.y + delta;
            line1.x = 0;
            line2.y = shapeSize.y + point2.y + delta;
            line2.x = 0;
            break;
        }
    }

    //	public boolean Add(int _x1, int _y1, int _x2, int _y2, int _position, String _description) {
    //		// добавить линейный размер
    //		// вернуть false, если добавляемый размер нулевой
    //		if( (_x1 == _x2) && (_y1 == _y2) )
    //			// добавляемый размер нулевой
    //			return false;
    //
    //		point1 = new Point(_x1, _y1);
    //		point2 = new Point(_x2, _y2);
    //		description = _description;
    //		position = _position;
    //
    //		return true;
    //	}

    //	public boolean CheckFor0Size() {
    //		// проверить, чтобы линейный размер не был нулевым
    //		if(point1 == point2)
    //			return true;
    //		return false;
    //	}
}
