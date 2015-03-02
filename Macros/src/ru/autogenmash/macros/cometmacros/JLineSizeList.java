/*$Id: JLineSizeList.java,v 1.1 2009/04/08 06:00:29 Dymarchyk Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.util.Vector;

/**Класс, содержащий список линейных размеров.
 * @author Dymarchuk Dmitry
 * @version
 * refactor 06.09.2007 10:15:00
 */
public class JLineSizeList
{
    protected Vector instance;

    public JLineSizeList()
    {
        instance = new Vector();
        instance.clear();
    }

    public boolean Add(int _x1, int _y1, int _x2, int _y2, int _position, String _description)
    {
        // добавить линейный размер
        // вернуть false, если добавляемый размер нулевой
        if ((_x1 == _x2) && (_y1 == _y2))
            // добавляемый размер нулевой
            return false;

        instance.add(new JLineSize(_x1, _y1, _x2, _y2, _position, _description));

        return true;
    }

    public JLineSize GetLineSize(int _position)
    {
        // получиьт JLineSize из списка
        return (JLineSize)instance.get(_position);
    }

    public int GetLength()
    {
        // получиьт длину списка
        return instance.size();
    }

    public void Clear()
    {
        // очистить список
        instance.clear();
    }
}
