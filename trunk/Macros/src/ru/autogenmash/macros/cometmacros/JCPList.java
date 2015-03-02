/*$Id:*/
package ru.autogenmash.macros.cometmacros;

import java.util.Vector;

import ru.autogenmash.core.ArcInterpolator;
import ru.autogenmash.core.Point;

/**Реализует управляющую программу, содержит список управляющих команд.
 * Также может описывать контур и траекторию.
 * <p><b>old implementation</b>
 * @author Dymarchuk Dmitry
 * @version
 * refactor 14.09.2007 9:03:21
 */
public class JCPList
{

    /** Тип контура (траектории) - внешний. */
    public static final int SHAPE = 1;
    /** Тип контура (траектории) - внутренний. */
    public static final int HOLE = 2;

    protected Vector _storage;

    /** Тип контура. */
    protected int _type;

    /** Подход. */
    protected JCC _leadIn;

    /** Отход. */
    protected JCC _leadOut;

//    /** Основной внешний контур. */
//    protected boolean _isMain;

    /** Вектор от отверстия до верхнего левого угла габаритов контура. */
    protected Point _toULC;

    /** Вектор от точки подхода до верхнего левого угла контура. */
    protected Point _fromLeadInToULC;

    /** Вектор от точки отхода до верхнего левого угла контура. */
    protected Point _fromLeadOutToULC;

    public JCPList(int type)
    {
        _storage = new Vector();
        _storage.clear();
        _type = type;
        _toULC = new Point(0, 0);
        _fromLeadInToULC = new Point(0, 0);
        _fromLeadOutToULC = new Point(0, 0);
    }

    public void clear()
    {
        _storage.clear();
    }

//    public boolean IsMain()
//    {
//        // является ли контур основным или нет
//        return _isMain;
//    }

    public void remove(int position)
    {
        _storage.removeElementAt(position);
    }

    public void add(int position, JCC cc)
    {
        _storage.add(position, cc);
    }

    public void setMovementFromLeadInToULC(Point fromLeadInToULC)
    {
        _fromLeadInToULC = fromLeadInToULC;
    }

    public void setMovementFromLeadInToULC(int x, int y)
    {
        _fromLeadInToULC.x = x;
        _fromLeadInToULC.y = y;
    }

    public Point getMovementFromLeadInToULC()
    {
        return _fromLeadInToULC;
    }

    public void SetMovementFromLeadOutToULC(Point fromLeadOutToULC)
    {
        _fromLeadOutToULC = fromLeadOutToULC;
    }

    public void setMovementFromLeadOutToULC(int x, int y)
    {
        _fromLeadOutToULC.x = x;
        _fromLeadOutToULC.y = y;
    }

    public Point getMovementFromLeadOutToULC()
    {
        return _fromLeadOutToULC;
    }

    public void setMovementToULC(Point toULC)
    {
        _toULC = toULC;
    }

    public void setMovementToULC(int x, int y)
    {
        _toULC.x = x;
        _toULC.y = y;
    }

    public Point getMovementToULC()
    {
        return _toULC;
    }

    public JCC getLeadIn()
    {
        return _leadIn;
    }

    protected void setLeadIn(JCC leadIn)
    {
        _leadIn = leadIn;
    }

    public JCC getLeadOut()
    {
        return _leadOut;
    }

    protected void setLeadOut(JCC leadOut)
    {
        _leadOut = leadOut;
    }

    public int getType()
    {
        return _type;
    }

    public void push(JCC cc)
    {
        add(0, cc);
    }

    public void pushBack(JCC cc)
    {
        add(getLength(), cc);
    }

    public int getLength()
    {
        return _storage.size();
    }

    /**Возвращает данные, находящиеся внутри управляющей команды,
     * расположенной на заданной позиции CP листа.
     * @param position
     * @return
     */
    public int[] getData(int position)
    {
        return GetCC(position).GetData();
    }

    public String getDescription(int position)
    {
        return GetCC(position).GetDescription();
    }

    // FIXME если в списке нет геометрии, то приведет к зацикливанию
    public JCC getFirstMovementAfterPosition(int position)
    {
        int i = 0;
        for (i = position; i < getLength() + position; i++)
            if ((getCCType(i % getLength()) == JCC.Line) || (getCCType(i % getLength()) == JCC.Arc))
                return GetCC(i % getLength());
        return null;
    }

    // FIXME если в списке нет геометрии, то приведет к зацикливанию
    public JCC getFirstMovementBeforePosition(int position)
    {
        int i = 0;
        for (i = getLength() + position; i > position; i--)
            if ((getCCType(i % getLength()) == JCC.Line) || (getCCType(i % getLength()) == JCC.Arc))
                return GetCC(i % getLength());
        return null;
    }

    public int getCCType(int position)
    {
        return ((JCC)_storage.get(position)).type;
    }

    public JCC GetCC(int position)
    {
        return (JCC)_storage.get(position);
    }

    /**Возвращает позицию CP листа, соответствующую выбранной точке пробивки.
     * @param pirsingPoint
     * @return
     */
    public int locatePirsingPoint(int pirsingPoint)
    {
        int[] data = new int[1];
        for (int i = 0; i < getLength(); i++)
        {
            if (getCCType(i) == JCC.StartPoint)
            {
                data = getData(i);
                if (data[0] == pirsingPoint)
                    return i;
            }
        }
        return -1;
    }

    /**Возвращает количество удаленных нулевых перемещений.
     * @return
     */
    public int removeVoidMovements()
    {
        int voidMovementCount = 0;
        for (int i = 0; i < getLength(); i++)
        {
            if (GetCC(i).CheckFor0Movement())
            {
                remove(i);
                voidMovementCount++;
            }
        }
        return voidMovementCount;
    }

    public void addLine(int x, int y, String description)
    {
        int[] data = new int[2];
        data[0] = x;
        data[1] = y;
        pushBack(new JCC(JCC.Line, data, description));
    }

    /**
     * @param x
     * @param y
     * @param i
     * @param j
     * @param direction true == -1 по ЧС
     * @param description
     */
    public void addArc(int x, int y, int i, int j, boolean direction, String description)
    {
        int[] data = new int[5];
        data[0] = x;
        data[1] = y;
        data[2] = i;
        data[3] = j;
        data[4] = (direction == true ? -1 : 1);
        pushBack(new JCC(JCC.Arc, data, description));
    }

    public void addStartPoint(int startPoint, String description)
    {
        int[] data = new int[1];
        data[0] = startPoint;
        pushBack(new JCC(JCC.StartPoint, data, description));
    }

    /**Вычисление габаритов контура (вместе с подходом/отходом).
     * @return
     */
    public Point calculateShapeSize()
    {
        Point res = new Point(0, 0);
        Point min = new Point(0, 0);
        Point max = new Point(0, 0);
        Point currPosition = new Point(0, 0);
        Point endPoint = new Point(0, 0);
        Point centerPoint = new Point(0, 0);
        Point stepPoint = new Point(0, 0);

        ArcInterpolator arcInterpolator;

        int[] data;
        for (int i = 0; i < getLength(); i++)
        {
            if (getCCType(i) == JCC.Line)
            {
                data = new int[2];
                data = GetCC(i).GetData();
                currPosition.x += data[0];
                currPosition.y += data[1];
                if (currPosition.x > max.x)
                    max.x = currPosition.x;
                if (currPosition.y > max.y)
                    max.y = currPosition.y;
                if (currPosition.x < min.x)
                    min.x = currPosition.x;
                if (currPosition.y < min.y)
                    min.y = currPosition.y;
            }
            if (getCCType(i) == JCC.Arc)
            {

                data = new int[5];
                data = GetCC(i).GetData();
                endPoint.x = data[0];
                endPoint.y = data[1];
                centerPoint.x = data[2];
                centerPoint.y = data[3];
                arcInterpolator = new ArcInterpolator(data[4], endPoint, centerPoint);
                double sizeFactor = (double)arcInterpolator.getRemainedS() / 1000;
                if (sizeFactor < 1)
                    sizeFactor = 1;

                while (arcInterpolator.getRemainedS() > 0)
                {
                    arcInterpolator.doStep(Math.round(sizeFactor * JMacros.ARC_FEED), stepPoint);
                    currPosition.x += stepPoint.x;
                    currPosition.y += stepPoint.y;
                    if (currPosition.x > max.x)
                        max.x = currPosition.x;
                    if (currPosition.y > max.y)
                        max.y = currPosition.y;
                    if (currPosition.x < min.x)
                        min.x = currPosition.x;
                    if (currPosition.y < min.y)
                        min.y = currPosition.y;
                }
            }
        }

        // габариты
        res.x = max.x - min.x;
        res.y = max.y - min.y;

        return res;
    }

//    /**Вычисление габаритов контура (вместе с подходом/отходом).
//     * @return
//     */
//    public Point calculateShapeSize()
//    {
//        Point res = new Point(0, 0);
//        Point min = new Point(0, 0);
//        Point max = new Point(0, 0);
//        Point currPosition = new Point(0, 0);
//        Point endPoint = new Point(0, 0);
//        Point centerPoint = new Point(0, 0);
//        Point stepPoint = new Point(0, 0);
//
//        ArcInterpolator2 arcInterpolator;
//
//        int[] data;
//        for (int i = 0; i < getLength(); i++)
//        {
//            if (getCCType(i) == JCC.Line)
//            {
//                data = new int[2];
//                data = GetCC(i).GetData();
//                currPosition.x += data[0];
//                currPosition.y += data[1];
//                if (currPosition.x > max.x)
//                    max.x = currPosition.x;
//                if (currPosition.y > max.y)
//                    max.y = currPosition.y;
//                if (currPosition.x < min.x)
//                    min.x = currPosition.x;
//                if (currPosition.y < min.y)
//                    min.y = currPosition.y;
//            }
//            if (getCCType(i) == JCC.Arc)
//            {
//
//                data = new int[5];
//                data = GetCC(i).GetData();
//                endPoint.x = data[0];
//                endPoint.y = data[1];
//                centerPoint.x = data[2];
//                centerPoint.y = data[3];
//                arcInterpolator = new ArcInterpolator2(data[4] == 1 ? false : true, endPoint, centerPoint, 10);
//
//                boolean interpolate = true;
//
//                while (interpolate)
//                {
//                    interpolate = arcInterpolator.doStep();
//                    stepPoint = arcInterpolator.getCurrentStepVector();
//                    currPosition.x += stepPoint.x;
//                    currPosition.y += stepPoint.y;
//                    if (currPosition.x > max.x)
//                        max.x = currPosition.x;
//                    if (currPosition.y > max.y)
//                        max.y = currPosition.y;
//                    if (currPosition.x < min.x)
//                        min.x = currPosition.x;
//                    if (currPosition.y < min.y)
//                        min.y = currPosition.y;
//                }
//            }
//        }
//
//        // габариты
//        res.x = max.x - min.x;
//        res.y = max.y - min.y;
//
//        return res;
//    }
}
