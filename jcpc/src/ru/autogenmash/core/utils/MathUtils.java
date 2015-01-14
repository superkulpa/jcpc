package ru.autogenmash.core.utils;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.Point;

/**
 * @author Dymarchuk Dmitry
 * 23.03.2007 15:51:29
 */
public class MathUtils
{

    private MathUtils()
    {
        //org.eclipse.jface.util.Geometry
        //org.eclipse.jface.util.Util
    }

    /** трехмерная координата задается радиусом, углом (P) и смещением по вертикали (Z) */
    public static final int COORDINATES_POLAR     = 1;
    /** трехмерная координата задается тремя точками (x,y,z) */
    public static final int COORDINATES_DEKART    = 2;

    /** Эпсилон окрестность точки или точность сравнения чисел. */
    public static final double EPS = 0.1; // TODO разобраться с точностью;

	public static double sqr(double value)
    {
		return value * value;
	}

    /**Sin.
     * @param grad угол в градусах.
     * @return
     */
    public static double sin(double grad)
    {
        return Math.sin(Math.toRadians(grad));
    }

    /**Cos.
     * @param grad угол в градусах.
     * @return
     */
    public static double cos(double grad)
    {
        return Math.cos(Math.toRadians(grad));
    }

    /**Tg.
     * @param grad угол в градусах.
     * @return
     */
    public static double tan(double grad)
    {
        return Math.tan(Math.toRadians(grad));
    }

    /**Возвращает длину отрезка.
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double length(double x1, double y1, double x2, double y2)
    {
        return Math.sqrt(sqr(x1 - x2) + sqr(y1 - y2));
    }

    public static double length(double x, double y)
    {
        return length(x, y, 0, 0);
    }

    public static double factorial(int n)
    {
        if (n < 0)
            throw new IllegalArgumentException("Negative n value: " + n);

        if (n == 0)
            return 1;
        
        double factorial = 1;
        for (int i = 1; i <= n; i++)
            factorial *= i;

        return factorial;
    }

    /**Биномиальный коэффициент (число сочетаний в комбинаторике)
     * @param i
     * @param n
     * @return <code>C<sup>i</sup>n</code>.
     */
    public static double binomK(int i, int n)
    {
        return (double)factorial(n) / factorial(i) / factorial(n - i);
    }

    /**Сравнить два вещественных числа с заданной точностью.
     * @param d1
     * @param d2
     * @param eps
     * @return
     */
    public static boolean compareDouble(double d1, double d2, double eps)
    {
        if (Math.abs(d1 - d2) < eps)
            return true;
        else
            return false;
    }

    /** повернуть координаты на угол(рад) считая от текущего положения
     * inversion == true - против часовой
     */
    public static Point rotateCoords(double radAngle, int lx, int ly, boolean inversion)
    {
    	double cosAngle = Math.cos(radAngle);
    	double sinAngle = Math.sin(radAngle);
    	short sgn;
    	if (inversion)
            sgn = -1;
        else
            sgn = 1;
    	return new Point((int)Math.round(cosAngle * lx + (double)sgn * sinAngle * ly),
                         (int)Math.round((double)-sgn * sinAngle * lx + cosAngle * ly));
    }

    /** повернуть координаты на угол(рад) считая от текущего положения(Sin,Cos посчитаны)
     * inversion == true - против часовой
     *
     * @param CosAngle
     * @param SinAngle
     * @param lx
     * @param ly
     * @param inversion
     * @return
     */
    public static Point rotateCoords(double CosAngle, double SinAngle, int lx, int ly, boolean inversion)
    {
    	short sgn;
    	if (inversion)
            sgn = -1;
        else
            sgn = 1;
    	return new Point((int)Math.round( CosAngle * lx + (double)sgn * SinAngle * ly),
                         (int)Math.round((double)-sgn * SinAngle * lx + CosAngle * ly));
    }

    
////////////////////////////////////////////////////////////////////////////////////////////
    /**смещение вектора по нормали
    * @param _pt точка, которую смещаем
    * @param _d длина нормали и направление куда смещаем(+ по часовой, - против)
    * @return _pt новые координаты точки*/
    public static Point GetNormalDot(Point _pt, int _d){
      // угол наклона вектора _pt
      double angle;
      // просто для хранения, чтоб не испортить
      Point pt = new Point(0,_d);
      //Поворот системы координат на angle
      angle = calculateAngle2(_pt.x,_pt.y);
      //Поворот на нужный угол
      pt.rotate(angle, true);
      //смещаем
      pt.x += _pt.x;
      pt.y += _pt.y;
      return pt;
    };
    
    /**Рассчитать угол между первым вектором и вторым (от первого вектора до второго против ЧС).
     * <b>В радианах.</b> Знак угла определяет направление от первого вектора ко второму (>0 значит по ЧС).
     * @param vector1
     * @param vector2
     * @return
     */
    public static double calculateCrossAngle(Point vector1, Point vector2)
    {
        double vector1Angle = calculateAngle2(vector1.x, vector1.y);
        double vector2Angle = calculateAngle2(vector2.x, vector2.y);

        double crossAngle = vector2Angle - vector1Angle;

        return crossAngle;
    }

    /**Возвращает угол (в радианах) от вектора (1,0) до заданного вектора (против ЧС).
     * @param x
     * @param y
     * @return
     */
    public static double calculateAngle2(double x, double y)
    {
        double b = 0;

        if ((x == 0) && (y == 0))
            b = 0;
        else if ((y == 0) && (x > 0))
            b = 0;
        else if ((x == 0) && (y > 0))
            b = Math.PI / 2;
        else if ((y == 0) && (x < 0))
            b = Math.PI;
        else if ((x == 0) && (y < 0))
            b = 3 * Math.PI / 2;
        else if ((x < 0) && (y < 0))
            b = Math.atan((double)y / x) + Math.PI;
        else if ((x < 0) && (y > 0))
            b = Math.PI - Math.atan((double)Math.abs(y) / Math.abs(x));
        else if ((x > 0) && (y < 0))
            b = 2 * Math.PI - Math.atan((double)Math.abs(y) / Math.abs(x));
        else if ((x > 0) && (y > 0))
            b = Math.atan((double)y / x);
        else
            throw new IllegalArgumentException("Не обработанные значения вектора x=" + x + " y=" + y);

        return b;
    }
    
    /**Возвращает угол (в радианах) от вектора (1,0) до заданного вектора.
     * @param x
     * @param y
     * @param direction true == против ЧС.
     * @return
     */
    public static double calculateAngle2(double x, double y, boolean direction)
    {
        double angle = calculateAngle2(x, y);
        if (direction == false)
            angle = Math.PI * 2 - angle;
        
        return angle;
    }

    /** вычислить угол в радианах от горизонтали до заданного вектора против ЧС
     * @deprecated use calculateAngle2
     * @param _x
     * @param _y
     * @return
     */
    public static double calculateAngle(int _x, int _y) {
    	double b = 0;

    	if( (_x == 0) && (_y > 0) ) {
    		b = Math.PI/2;
    	} else if( (_x == 0) && (_y < 0) ) {
    		b = 3 * Math.PI/2;
    	} else if( (_y == 0) && (_x > 0) ) {
    		b = 0;
    	} else if( (_y == 0) && (_x < 0) ) {
    		b = Math.PI;
    	} else if( (_x == 0) && (_y == 0) ) {
    		b = 0;
    	} else if( (_x < 0) && (_y < 0) ) {
    		b = Math.atan((double)_y / _x) + Math.PI;
    	} else if( (_x < 0) && (_y > 0) ) {
    		b = Math.atan((double)Math.abs(_x) / Math.abs(_y)) + Math.PI/2;
    	} else if( (_x > 0) && (_y < 0) ) {
    		b = Math.atan((double)Math.abs(_x) / Math.abs(_y)) + 3*Math.PI/2;
    	} else {
    		b = Math.atan((double)_y / _x);
    	}
    	return b;
    }

//    /** перевести градусы в радианы
//     *
//     * @param _grad
//     * @return
//     */
//    public static double gradToRad(int _grad) {
//    	return 	Math.PI * _grad / 180;
//    }
//
//    /** перевести градусы в радианы
//     *
//     * @param _grad
//     * @return
//     */
//    public static double gradToRad(double _grad) {
//    	return 	Math.PI * _grad / 180;
//    }
//
//    public static double radToGrad(double rad)
//    {
//        return  rad * 180 / Math.PI;
//    }

//    /** перевернуть перемещение по направлению
//     *
//     * @param _data
//     * @return
//     */
//    public static int[] reverseMovement(int[] _data) {
//    	int data[] = new int[_data.length];
//
//    	if(_data.length == 2) {
//    		data[0] = - _data[0];
//    		data[1] = - _data[1];
//    	} else {
//    		data[0] = - _data[0];
//    		data[1] = - _data[1];
//    		data[2] = _data[2] - _data[0];
//    		data[3] = _data[3] - _data[1];
//    		data[4] = _data[4];
//    	}
//
//    	return data;
//    }

    
    //установить значение aValue
    public static double ArcRadian_Value(double _value){
      double aValue = 0;
    //aValue привести к формату 0-2Pi
      if(_value == 0) aValue = 0;//0=0 остальное либо угол либо 2Pi}
      else{
        //выбираем число целых 2Pi, и удаляем их
        int del = (int)(_value / (2 * Math.PI));
        if(del < 0) del = -del;
        aValue = _value - (del * (2 * Math.PI));
        //приводим к плюсу
        if(aValue < 0) aValue = -aValue ;
        if(aValue == 0) aValue = (2 * Math.PI);
      };
      return aValue;
    };
    
      /**
       * @param x
       * @param y
       * @param i
       * @param j
       * @param direction G02 == true
       * @return
       */
      
    public static double calculateArcLength(int x, int y, int i, int j, boolean direction)
    {

      double r = length(i, j); double Pi_2 = 2 * Math.PI; 
      if(x == 0 && y == 0) return Pi_2 * r;
      //вычисляем вектор от середины к концу
      int x1 = x - i; int y1 = y - j;
      //int proizvedenie = -i * x1 - j * y1;
      
      double beginAngle = Math.atan2(-j, -i);
      if( (beginAngle) < 0) beginAngle += (2 * Math.PI); //преобразуем диапазон от [-П;+П] к [0;2П]
      double endAngle = Math.atan2(y1, x1);
      if( (endAngle) < 0) endAngle += (2 * Math.PI); //преобразуем диапазон от [-П;+П] к [0;2П]
      if(direction == true){
        if (beginAngle <= endAngle) endAngle = ArcRadian_Value(Pi_2 - (endAngle - beginAngle));
        else endAngle = ArcRadian_Value(endAngle - beginAngle);
      }else{
        if (beginAngle >= endAngle) endAngle = ArcRadian_Value(Pi_2 - (beginAngle - endAngle));
        else endAngle = ArcRadian_Value(beginAngle - endAngle);
      };
      
//      //скалярные произведения
//      double cosAlpha = proizvedenie / (length(i, j) * length(x1, y1)); 
//      
//      double alpha = Math.acos(cosAlpha);
      
      return endAngle * r; 
    }
    
//    public static double calculateArcLength2(int x, int y, int i, int j, boolean direction)
//    {
//        // FIXME xxx;
//        ArcInterpolator2 ai2 = new ArcInterpolator2(direction, x, y, i, j, 10);
//        return ai2.getRemainedS();
//    }

    public static Point correctArcCenter(boolean g2, int x, int y, int i, int j)
    {
        double xyLength = length(x, y);
        double R1 = length(i, j);
        double R2 = length(x, y, i, j);
        //double maxR = Math.max(R1, R2);
        //int maxR = (int)Math.round((R1 + R2)/2);
        double maxR = (R1 + R2) / 2;

        if (xyLength / (maxR * 2) > 1)
            maxR = xyLength / 2;

        double xyAngle = calculateAngle2(x, y); // угол между векторами (1,0) и (x,y)
        double a2 = Math.acos(CheckCosValue(xyLength / (maxR * 2)));
        
        double ijAngle = calculateAngle2(i, j); // угол между векторами (1,0) и (i,j)

        Point tmpXY = rotateCoords(-ijAngle, x, y, true);
        if (tmpXY.y < 0)
            ijAngle = xyAngle + a2;
        else
            ijAngle = xyAngle - a2;

//        if (ijAngle > Math.PI*2)
//        {
//            ijAngle = ijAngle - Math.PI * 2;
//            System.out.println("!");
//        }
        if (Double.isNaN(ijAngle))
            throw new ArithmeticException("Ошибка при выравнивании центра дуги");

        return rotateCoords(ijAngle, (int)Math.round(maxR), 0, true);
//        double koef = maxR / xyLength;
//        Point tmp = new Point((int)(x * koef),(int)(y * koef));
//        if(CheckDirectionMove(new Point(i,j), new Point(x - i,y - j))) {
//          tmp.rotate(a2, false);
//        }else {
//          tmp.rotate(a2, true);
//        };
//        return tmp;
    }

    public static void reverseArc(Point endPoint, Point centerPoint, boolean direction,
            Point newEndPoint, Point newCenterPoint, Boolean newDirection)
    {
        newEndPoint.x = -endPoint.x;
        newEndPoint.y = -endPoint.y;
        newCenterPoint.x = centerPoint.x - endPoint.x;
        newCenterPoint.y = centerPoint.y - endPoint.y;
        newDirection = new Boolean(!direction); // FIXME ?
    }

    /**Вычисление нормали подхода/отхода по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая линию
     * @param length
     * @param leadInLeadOut подход: leadInLeadOut - true; отход: leadInLeadOut - false;
     * @param newEndPoint - вектор искомой нормали
     */
    public static void normalToLine(Point endPoint, int length, boolean leadInLeadOut, Point newEndPoint)
    {
        Point normal = new Point(0, length);

        double b = calculateAngle2(endPoint.x, endPoint.y);

        normal = rotateCoords(b, normal.x, normal.y, true);

        newEndPoint.x = normal.x;
        newEndPoint.y = normal.y;

        if (leadInLeadOut)
            newEndPoint.inverse();
    }

    /**Вычисление касательной подхода/отхода по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая линию
     * @param length
     * @param newEndPoint - вектор искомой касательной
     */
    public static void tangentToLine(Point endPoint, int length, Point newEndPoint)
    {
        double length2 = endPoint.length();

        newEndPoint.x = (int)Math.round((double)endPoint.x * length / length2);
        newEndPoint.y = (int)Math.round((double)endPoint.y * length / length2);
    }

    /**Вычисление нормали подхода/отхода по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая дугу
     * @param centerPoint - центральная точка, определяющая дугу
     * @param direction - направление дуги
     * @param length
     * @param leadInLeadOut подход: leadInLeadOut - true; отход: leadInLeadOut - false;
     * @param isShape - если true? то внешний контур, иначе внутренний
     * @param newEndPoint - вектор искомой нормали
     */
    public static void normalToArc(Point endPoint, Point centerPoint, boolean direction,
            int length, boolean leadInLeadOut, boolean isShape, Point newEndPoint)
    {
        Point line = new Point(0, 0);

        if (leadInLeadOut == true)
        {
            line.x = centerPoint.x;
            line.y = centerPoint.y;
        }
        else
        {
            line.x = endPoint.x - centerPoint.x;
            line.y = endPoint.y - centerPoint.y;
        }

        if (isShape == false || direction == false) // TODO проверить направление direction
            line.inverse();

        tangentToLine(line, length, newEndPoint);
    }


    /**Вычисление касательной в начальной/конечной точке (подход/отход) по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая дугу
     * @param centerPoint - центральная точка, определяющая дугу
     * @param direction - направление дуги
     * @param length
     * @param leadInLeadOut начало (подход): leadInLeadOut - true; конец (отход): leadInLeadOut - false;
     * @param isShape - если true? то внешний контур, иначе внутренний
     * @param newEndPoint - вектор искомой касательной
     */
    public static void tangentToArc(Point endPoint, Point centerPoint, boolean direction,
            int length, boolean leadInLeadOut, boolean isShape, Point newEndPoint)
    {
        Point line = new Point(0, 0);
        if (leadInLeadOut)
        {
            line.x = centerPoint.x;
            line.y = centerPoint.y;
        }
        else
        {
            line.x = centerPoint.x - endPoint.x;
            line.y = centerPoint.y - endPoint.y;
        }

        if (isShape == false || direction == false)//FIXME Attention
            line.inverse();

        normalToLine(line, length, false, newEndPoint);
    }

    /**Вычисление дуги подхода/отхода по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая линию
     * @param length
     * @param angle - в градусах
     * @param leadInLeadOut подход: leadInLeadOut - true; отход: leadInLeadOut - false;
     * @param newEndPoint - конечная точка, определяющая искомую дугу
     * @param newCenterPoint - центральная точка, определяющая искомую дугу
     * @param newDirection - направление отхода искомой дуги
     */
    public static void arcToLine(Point endPoint, int length, int angle,
            boolean leadInLeadOut, Point newEndPoint, Point newCenterPoint, Boolean newDirection)
    {
        int r = (int)Math.round(180 * length / (Math.PI * angle)); // радиус дуги
        newEndPoint = new Point(0, -r);
        newCenterPoint = new Point(0, 0); // это начало координат

        // 1) поворачиваем на angle
        newEndPoint = rotateCoords(Math.toRadians(angle), newEndPoint.x, newEndPoint.y, !leadInLeadOut);

        // 2) вычисляем угол b - угол между вертикалью и прямой, к которой строится дуга
        double b = calculateAngle2(endPoint.x, endPoint.y);

        // 3) переносим центр координат в точку сопряжения прямой и дуги
        newEndPoint.y += r;
        newCenterPoint.y += r;

        // 4) поворачиваем на угол b
        newEndPoint = rotateCoords(b, newEndPoint.x, newEndPoint.y, true);
        newCenterPoint = rotateCoords(b, newCenterPoint.x, newCenterPoint.y, true);

        if (leadInLeadOut)
            reverseArc(newEndPoint, newCenterPoint, newDirection.booleanValue(),
                    newEndPoint, newCenterPoint, newDirection);
    }

    /**Вычисление дуги подхода/отхода по направлению к заданному перемещению.
     * @param endPoint - конечная точка, определяющая дугу
     * @param centerPoint - центральная точка, определяющая дугу
     * @param direction - направление определяющее дугу
     * @param length
     * @param angle
     * @param leadInLeadOut подход: leadInLeadOut - true; отход: leadInLeadOut - false;
     * @param isShape - если true? то внешний контур, иначе внутренний
     * @param newEndPoint - конечная точка, определяющая искомую дугу
     * @param newCenterPoint - центральная точка, определяющая искомую дугу
     * @param newDirection - направление отхода искомой дуги
     */
    public static void arcToArc(Point endPoint, Point centerPoint, boolean direction,
            int length, int angle, boolean leadInLeadOut, boolean isShape,
            Point newEndPoint, Point newCenterPoint, Boolean newDirection)
    {
        Point tmpLine = new Point(0, 0);
        Point tangentEndPoint = new Point(0, 0);

        if (leadInLeadOut)
        {
            tmpLine.x = centerPoint.x;
            tmpLine.y = centerPoint.y;
        }
        else
        {
            tmpLine.x = centerPoint.x - endPoint.x;
            tmpLine.y = centerPoint.y - endPoint.y;
        }
        if (isShape == true && direction == false) //FIXME Attention3 (full block)
            tmpLine.inverse();

        normalToLine(tmpLine, length, leadInLeadOut, tangentEndPoint);
        arcToLine(tangentEndPoint, length, angle, leadInLeadOut, newEndPoint, newCenterPoint, newDirection);
    }

    /**Возвращает точку отстоящую на величину d от отрезка line под углом 90
     * от направления прямой в сторону, определяемую g41
     * @param line
     * @param g41
     * @param d
     * @return
     */
    public static Point kerfNormalToLine(Point line, boolean g41, int d)
    {
        Point kerfNormal = new Point(0, 0);

        normalToLine(line, d, !g41, kerfNormal);

        return kerfNormal;
    }

    /**
     * @param endPoint
     * @param centerPoint
     * @param direction
     * @param firstPoint
     * @param g41
     * @param d
     * @return
     */
    public static Point kerfNormalToArc(Point endPoint, Point centerPoint,
            boolean direction, boolean firstPoint, boolean g41, int d)
    {
        Point kerfNormal = new Point(0, 0);

        normalToArc(endPoint, centerPoint, direction, d, firstPoint, true, kerfNormal);

        if ( (firstPoint == true && g41 == true) || (firstPoint == false && g41 == false) )
            kerfNormal.inverse();

        return kerfNormal;
    }

    /** Возвращает точку пересечения двух гео кадров если они пересекаются, иначе null
     * @param currentFrame
     * @param checkFrame
     * @param xAbs - расстояние от начальной точки currentFrame до первой точки checkFrame (по X)
     * @param yAbs - расстояние от начальной точки currentFrame до первой точки checkFrame (по Y)
     * @return
     */
    public static Point calculateIntersectionPoint(CachedCpFrame currentFrame,
            CachedCpFrame checkFrame, int xAbs, int yAbs)
    {
        Point intersectionPoint;

        int x = Utils.toInt(currentFrame.getDataByType(CC.X));
        int y = Utils.toInt(currentFrame.getDataByType(CC.Y));
        int i = Utils.toInt(currentFrame.getDataByType(CC.I));
        int j = Utils.toInt(currentFrame.getDataByType(CC.J));

        int checkX = Utils.toInt(checkFrame.getDataByType(CC.X));
        int checkY = Utils.toInt(checkFrame.getDataByType(CC.Y));

        if (currentFrame.getType() == CpFrame.FRAME_TYPE_LINE &&
            checkFrame.getType() == CpFrame.FRAME_TYPE_LINE)
        {
            intersectionPoint = calculateIntersectionLineToLine(
                    new Point(x, y), new Point(xAbs, yAbs),
                    new Point(xAbs + checkX, yAbs + checkY), true);
        }
        else if (currentFrame.getType() == CpFrame.FRAME_TYPE_LINE &&
                checkFrame.getType() == CpFrame.FRAME_TYPE_ARC)
        {
            CPList checkArcList = new CPList(new CachedCpFrame[] {checkFrame});
            //checkArcList.calculateShapeSize();
            Point min = checkArcList.getMinPosition();
            Point max = checkArcList.getMaxPosition();
            min.x += xAbs - i;
            min.y += yAbs - j;
            max.x += xAbs - i;
            max.y += yAbs - j;

            int checkI = Utils.toInt(checkFrame.getDataByType(CC.I));
            int checkJ = Utils.toInt(checkFrame.getDataByType(CC.J));
            intersectionPoint = calculateIntersectionLineToArc(
                    new Point(x, y), new Point(xAbs + checkI, yAbs + checkJ),
                    length(checkI, checkJ),
                    min, max);
        }
        else if (currentFrame.getType() == CpFrame.FRAME_TYPE_ARC &&
                checkFrame.getType() == CpFrame.FRAME_TYPE_LINE)
        {
            CPList currentArcList = new CPList(new CachedCpFrame[] {currentFrame});
            //currentArcList.calculateShapeSize();
            Point min = currentArcList.getMinPosition();
            Point max = currentArcList.getMaxPosition();
            min.x -= i;
            min.y -= j;
            max.x -= i;
            max.y -= j;

            intersectionPoint = calculateIntersectionArcToLine(
                    length(i, j), new Point(xAbs - i, yAbs - j),
                    new Point(xAbs + checkX - i, yAbs + checkY - j),
                    min, max);
            if (intersectionPoint != null)
            {
                intersectionPoint.x += i;
                intersectionPoint.y += j;
            }
        }
        else if (currentFrame.getType() == CpFrame.FRAME_TYPE_ARC &&
                checkFrame.getType() == CpFrame.FRAME_TYPE_ARC)
        {
            int checkI = Utils.toInt(checkFrame.getDataByType(CC.I));
            int checkJ = Utils.toInt(checkFrame.getDataByType(CC.J));

            CPList currentArcList = new CPList(new CachedCpFrame[] {currentFrame});
            //currentArcList.calculateShapeSize();
            Point min1 = currentArcList.getMinPosition();
            Point max1 = currentArcList.getMaxPosition();
            min1.x -= i;
            min1.y -= j;
            max1.x -= i;
            max1.y -= j;

            CPList checkArcList = new CPList(new CachedCpFrame[] {checkFrame});
            //checkArcList.calculateShapeSize();
            Point min2 = checkArcList.getMinPosition();
            Point max2 = checkArcList.getMaxPosition();
            min2.x += xAbs - i;
            min2.y += yAbs - j;
            max2.x += xAbs - i;
            max2.y += yAbs - j;

            intersectionPoint = calculateIntersectionArcToArc(
                    length(i, j),
                    new Point(xAbs + checkI - i, yAbs + checkJ - j),
                    length(checkI, checkJ),
                    min1, max1, min2, max2);
            if (intersectionPoint != null)
            {
                intersectionPoint.x += i;
                intersectionPoint.y += j;
            }
        }
        else
            throw new IllegalArgumentException("Unknown frame (type is not geo) " +
                    "current frame: \"" + currentFrame.toString() + "\" " +
                    "check frame: \"" + checkFrame.toString() + "\"");

        return intersectionPoint;
    }

    /**Возвращает истина если заданные отрезки пересекаются.
     * <p> y = k1*x + c1 <b>(I)</b>
     * <p> y = k2*x + c2 <b>(II)</b>
     * @param endPoint1 - конечная точка 1-го отрезка (начальная точка = (0,0))
     * @param startPoint2 - начальная точка 2-го отрезка
     * @param endPoint2 - конечная точка 2-го отрезка
     * @return true, если отрезки пересекаются, false - иначе
     */
    public static Point calculateIntersectionLineToLine(Point endPoint1,
            Point startPoint2, Point endPoint2, boolean _check)
    {
        if (endPoint1.isZero() || new Point(startPoint2.x - endPoint2.x,
                                            startPoint2.y - endPoint2.y).isZero())
            throw new IllegalArgumentException("Input line(s) can not be zero");

        double x = 0;
        double y = 0;
        Double k1 = null;
        Double k2 = null;
        //double c1 = 0; always
        double c2 = 0;

        if (endPoint1.x != 0)
            k1 = new Double((double)endPoint1.y / endPoint1.x);

        if (endPoint2.x - startPoint2.x != 0)
            k2 = new Double(((double)endPoint2.y - startPoint2.y) / (endPoint2.x - startPoint2.x));

        if (k1 == null && k2 == null)
        {
            if (startPoint2.x != 0)
                return null;
            else
                return checkPoint(endPoint2.x, endPoint2.y, getMin(Point.ZERO, endPoint1), getMax(Point.ZERO, endPoint1));
        }

        if (k2 == null)
            c2 = 0;
        else
            c2 = (double)startPoint2.y - k2.doubleValue() * startPoint2.x;

//        TODO разобраться с коментарием
//        if (k1 != null && k2 != null)
//            if (compareDouble(k1.doubleValue(), k2.doubleValue(), EPS) && c2 != 0)
//                return null;

        if (endPoint1.x == 0)
        {
            x = 0;
            y = c2;
        }
        else if (endPoint2.x - startPoint2.x == 0)
        {
            x = startPoint2.x;
            y = k1.doubleValue() * x;
        }
        else
        {
            x = c2 / (k1.doubleValue() - k2.doubleValue());
            y = k1.doubleValue() * x;
        }

        if(!_check) return new Point((int)Math.round(x), (int)Math.round(y));
        
        Point intersectionPoint = checkPoint(x, y, getMin(Point.ZERO, endPoint1), getMax(Point.ZERO, endPoint1));
        if (intersectionPoint != null)
            intersectionPoint = checkPoint(x, y, getMin(startPoint2, endPoint2), getMax(startPoint2, endPoint2));
        return intersectionPoint;
    }

    public static Point calculateIntersectionLineToArc(Point line,
            Point centerPoint, double r,
            Point min, Point max)
    {
        if (line.isZero() || compareDouble(0, r, 0.0001d))
            throw new IllegalArgumentException("Input line or radius can not be zero");

        double k = 0;
        double a, b, c, D;
        double x1, y1, x2, y2;
        if (line.x != 0)
        {
            k = (double)line.y / line.x;

            a = sqr(k) + 1;
            b = (double)-2*(centerPoint.x + k*centerPoint.y);
            c = sqr(centerPoint.x) + sqr(centerPoint.y) - sqr(r);
            D = sqr(b) - 4*a*c;

            if (D < 0)
            {
                //System.out.println("D < 0");
                return null;
            }

            x1 = (-b + Math.sqrt(D)) / (2*a);
            y1 = k * x1;
            x2 = (-b - Math.sqrt(D)) / (2*a);
            y2 = k * x2;
        }
        else
        {
            a = 1;
            b = (double)-2*centerPoint.y;
            c = sqr(centerPoint.x) + sqr(centerPoint.y) - sqr(r);
            D = sqr(b) - 4*a*c;

            x1 = x2 = 0;
            y1 = (-b - Math.sqrt(D)) / (2*a);
            y2 = (-b + Math.sqrt(D)) / (2*a);
        }

        Point intersectionPoint1 = checkPoint(x1, y1, getMin(Point.ZERO, line), getMax(Point.ZERO, line));
        Point intersectionPoint2 = checkPoint(x2, y2, getMin(Point.ZERO, line), getMax(Point.ZERO, line));
        if (intersectionPoint1 != null)
            intersectionPoint1 = checkPoint(x1, y1, min, max);
        if (intersectionPoint2 != null)
            intersectionPoint2 = checkPoint(x2, y2, min, max);

        if (intersectionPoint1 != null && intersectionPoint2 != null)
        {
            double length1 = length(x1, y1);
            double length2 = length(x2, y2);
            if (length1 < length2)
                return intersectionPoint1;
            else
                return intersectionPoint2;
        }

        if (intersectionPoint1 != null)
            return intersectionPoint1;

        if (intersectionPoint2 != null)
            return intersectionPoint2;

        return null;
    }

    public static Point calculateIntersectionArcToLine(double r,
            Point startPoint, Point endPoint,
            Point min, Point max)
    {
        Point line = new Point(endPoint.x - startPoint.x, endPoint.y - startPoint.y);
        Point centerPoint = new Point(0 - startPoint.x, 0 - startPoint.y);
        Point inrtersectionPoint = calculateIntersectionLineToArc(line, centerPoint, r,
                new Point(min.x - startPoint.x, min.y - startPoint.y),
                new Point(max.x - startPoint.x, max.y - startPoint.y));
        if (inrtersectionPoint != null)
        {
            inrtersectionPoint.x += startPoint.x;
            inrtersectionPoint.y += startPoint.y;
        }

        return inrtersectionPoint;
    }

    public static Point calculateIntersectionArcToArc(double r1,
            Point centerPoint, double r2,
            Point min1, Point max1, Point min2, Point max2)
    {
        // FIXME не обработан случай совпадения дуг
        if (centerPoint.isZero())
            return null;

        double x1, y1, x2, y2;

        if (centerPoint.x == 0)
        {
            y1 = y2 = ((double)sqr(centerPoint.y) +
                    sqr(r1) - sqr(r2)) / (2*centerPoint.y);
            if (y1 > r1)
                return null;
            x1 = Math.sqrt(sqr(r1) - sqr(y1));
            x2 = -x1;
        }
        else
        {
            // x = k*y + p - прямая, на которой распологаются sточки пересечения окружностей
            double k = (double)-centerPoint.y / centerPoint.x;
            double p = (sqr(centerPoint.x) + sqr(centerPoint.y) +
                    sqr(r1) - sqr(r2)) / (2*centerPoint.x);

            double a = sqr(k) + 1;
            double b = 2 * k * p;
            double c = -sqr(r1) + sqr(p);
            double D = sqr(b) - 4*a*c;

            if (D < 0)
                return null;

            y1 = (-b + Math.sqrt(D)) / (2*a);
            x1 = k * y1 + p;
            y2 = (-b - Math.sqrt(D)) / (2*a);
            x2 = k * y2 + p;
        }

        Point intersectionPoint1 = null;
        Point intersectionPoint11 = checkPoint(x1, y1, min1, max1);
        Point intersectionPoint12 = checkPoint(x1, y1, min2, max2);
        if (intersectionPoint11 != null && intersectionPoint12 != null)
            intersectionPoint1 = intersectionPoint11;

        Point intersectionPoint2 = null;
        Point intersectionPoint21 = checkPoint(x2, y2, min1, max1);
        Point intersectionPoint22 = checkPoint(x2, y2, min2, max2);
        if (intersectionPoint21 != null && intersectionPoint22 != null)
            intersectionPoint2 = intersectionPoint21;

        if (intersectionPoint1 != null && intersectionPoint2 != null)
        {
            double length1 = length(x1, y1);
            double length2 = length(x2, y2);
            if (length1 < length2)
                return intersectionPoint1;
            else
                return intersectionPoint2;
        }

        if (intersectionPoint1 != null)
            return intersectionPoint1;

        if (intersectionPoint2 != null)
            return intersectionPoint2;

        return null;
    }


    /**Проверить попадает ли точка (x,y) в квадрат
     * [(restrictionLeft.x, restrictionRight.x);(restrictionLeft.y, restrictionRight.y)]
     * @param x
     * @param y
     * @param min
     * @param max
     * @return null, если не попадает; new Point(x, y) - иначе
     */
    public static Point checkPoint(double x, double y, Point min, Point max)
    {
        // TODO
        // xxx; не пашет!

        if ((x >= (double)min.x - 1 && x <= (double)max.x + 1) &&
            (y >= (double)min.y - 1 && y <= (double)max.y + 1))
            return new Point((int)Math.round(x), (int)Math.round(y));

        return null;
    }

    /**Возвращает номер четверти (квадранта), в которой находится заданный вектор
     * @param vector
     * @return
     */
    public static int getQuadrant(Point vector)
    {
        // TODO что будет если точка лежит на оси (осях) ?
        if (vector.x >= 0 && vector.y >= 0)
            return 1;
        else if (vector.x <= 0 && vector.y >= 0)
            return 2;
        else if (vector.x <= 0 && vector.y <= 0)
            return 3;
        else
            return 4;
    }

    public static Point getMin(Point startPoint, Point endPoint)
    {
        int minX = startPoint.x;
        if (endPoint.x < minX)
            minX = endPoint.x;

        int minY = startPoint.y;
        if (endPoint.y < minY)
            minY = endPoint.y;

        return new Point(minX, minY);
    }

    public static Point getMax(Point startPoint, Point endPoint)
    {
        int maxX = startPoint.x;
        if (endPoint.x > maxX)
            maxX = endPoint.x;

        int maxY = startPoint.y;
        if (endPoint.y > maxY)
            maxY = endPoint.y;

        return new Point(maxX, maxY);
    }

    /**Преобразовать полилинию из относительных в абсолютные координаты
         * @param polyLine - в относительных координатах
         * @return полилиния в абсолютных координатах
         */
        public static Point[] convertComparativePolyLineToAbsolute(Point[] polyLine)
        {
            int xAbs = 0;
            int yAbs = 0;
    
            Point[] res = new Point[polyLine.length /*+ 1*/];
    
            //res[0] = new Point();
    
            for (int l = 0; l < polyLine.length; l++)
            {
                Point point = polyLine[l];
                xAbs += point.x;
                yAbs += point.y;
    
    //            point.x = xAbs;
    //            point.y = yAbs;
                res[l/* + 1*/] = new Point(xAbs, yAbs);
            }
    
            return res;
        }
        //определить направлениt обхода по двум векторам
        public static boolean CheckDirectionMove(Point vector1, Point vector2)
        {
          double angle = calculateAngle2(vector1.x,vector1.y);
          Point copy_vector = new Point(vector2.x,vector2.y);
          copy_vector.rotate(angle,false);
          angle = calculateAngle2(copy_vector.x,copy_vector.y);
          if((angle > 0) && (angle < Math.PI))return false;
          return true;
        }

        public static int GetTypeAngleBetweenFrame(CachedCpFrame _firstFrame,
            CachedCpFrame _secondFrame, int _d) {
          
          switch(_firstFrame.getType()) {
            case CpFrame.FRAME_TYPE_LINE:
              switch(_secondFrame.getType()) {
                case CpFrame.FRAME_TYPE_LINE:
                  if(MathUtils.CheckDirectionMove(_firstFrame.getXY(),_secondFrame.getXY())){
                    if(_d < 0){
                      return 0;
                    }else {
                      return 1;
                    }
                  }else{
                    if(_d > 0){
                      return 0;
                    }else {
                      return 1;
                    } 
                  }
                case CpFrame.FRAME_TYPE_ARC:
                break;
              };
            break;
            case CpFrame.FRAME_TYPE_ARC:
              switch(_secondFrame.getType()) {
                case CpFrame.FRAME_TYPE_LINE:
                break;
                case CpFrame.FRAME_TYPE_ARC:
                break;
              };
            break;
          };
          return 0;
        }

        public static Point getCrossPoint(Point _first, Point _second, Point _third) {
          // TODO Auto-generated method stub
          Point beginSecond = new Point();
          Point endSecond = new Point();
          beginSecond.x = _first.x + _second.x;
          beginSecond.y = _first.y + _second.y;
          endSecond.x = beginSecond.x + _third.x;
          endSecond.y = beginSecond.y + _third.y;
          return calculateIntersectionLineToLine(_first, beginSecond, endSecond, false);
        }

        public static Point CalcCrossLineArc(int _d, Point _firstVector,
            Point _secondVector, boolean _g02) {
          Point centerPoint = new Point(-_secondVector.x, -_secondVector.y);
          //старый радиус
          double oldR = _secondVector.length();
          //угол между вектором центра и прямой
          double angle = /*Math.PI - */Math.abs(MathUtils.calculateCrossAngleRad(_firstVector, _secondVector));
          //Math.toRadians(180 - Math.abs(calculateCrossAngleGrad(_firstVector, centerPoint)));
          //направление от вектора к центру
          boolean vectorDir = CheckDirectionMove(_firstVector,new Point(_secondVector.x,_secondVector.y));
          double corrR = 0;
          if(angle != 0)
            corrR = Math.abs(_d) / Math.sin(angle);
          //это в обратном направлении 
          if(corrR == 0) vectorDir = _g02;
          if(((_d > 0) && vectorDir) ||
             ((_d < 0) && !vectorDir)) {
            oldR += corrR;
          }else {
            oldR -= corrR;
          }
          double corr = 1;
          
          if(((_d < 0) && (!_g02)) ||
             ((_d > 0) && _g02)){
            corr = 1 + Math.abs(_d) / centerPoint.length();
          }else {
            corr = 1 - Math.abs(_d) / centerPoint.length();
          };
          
          centerPoint.x *= corr;
          centerPoint.y *= corr;
          
          if(angle == 0) return GetNormalDot(centerPoint,_d);
          //величина угла 180 = angleRotate + xAngle + angle 
          double xAngle = 0;
          xAngle = CalcSinTheoreme(centerPoint.length(), angle, oldR);

          if(xAngle == 0) return null;
          double angleRotate = 0;
          if(angle >= (Math.PI / 2)) {
            angleRotate = Math.PI - angle - xAngle;
            //поворачиваем в обратную сторону
            centerPoint.rotate(angleRotate, vectorDir);  
          }else if(angle < (Math.PI / 2)) {
            angleRotate = angle - xAngle;
            //поворачиваем в ту же сторону
            centerPoint.rotate(angleRotate, !vectorDir);
          };
          
          return centerPoint;
        };
        
        public static Point CalcCrossLineArc(int _d, Point _firstVector,
            CachedCpFrame _nextFrame, boolean _correct) {
          
          int oldCenterX = Utils.toInt(_nextFrame.getDataByType(CC.I));
          int oldCenterY = Utils.toInt(_nextFrame.getDataByType(CC.J)); 
          
          Point centerPoint = new Point(-oldCenterX, -oldCenterY);
          //старый радиус
          double oldR = centerPoint.length();
          //угол между вектором центра и прямой
          double angle = Math.PI - Math.abs(MathUtils.calculateCrossAngleRad(_firstVector, centerPoint));
          //Math.toRadians(180 - Math.abs(calculateCrossAngleGrad(_firstVector, centerPoint)));
          //направление от вектора к центру
          boolean vectorDir = CheckDirectionMove(_firstVector,new Point(oldCenterX,oldCenterY));
          if(!_correct) {
            if(vectorDir) {
              if(_nextFrame.hasG02())
                _d = -(int)Math.round((_d * (1 - Math.sin(angle))));
              else
                _d = -(int)Math.round((_d * (1 + Math.sin(angle))));
            }else {
              if(_nextFrame.hasG03())
                _d = -(int)Math.round((_d * (1 - Math.sin(angle))));
              else
                _d = -(int)Math.round((_d * (1 + Math.sin(angle))));
            };
          };
           
          double corrR = 0;
          if(angle != 0)
            corrR = Math.abs(_d) / Math.sin(angle);
          //это в обратном направлении 
          if(corrR == 0) vectorDir = _nextFrame.hasG02();
          
          //вычисляем параметры для теоремы синусов
          if(((_d > 0) && vectorDir) ||
             ((_d < 0) && !vectorDir))
            oldR += corrR;
          else
            oldR -= corrR;
          //коррекция дуги  
          double corr = 1;
          if(((_d < 0) && (_nextFrame.hasG03())) ||
             ((_d > 0) && (_nextFrame.hasG02()))){
            corr = 1 + Math.abs(_d) / centerPoint.length();
          }else {
            corr = 1 - Math.abs(_d) / centerPoint.length();
          };
          if(_correct){
            centerPoint.x *= corr;
            centerPoint.y *= corr;
          };
          if(angle == 0) return GetNormalDot(centerPoint,_d);
          //величина угла 180 = angleRotate + xAngle + angle 
          double xAngle = 0;
          xAngle = CalcSinTheoreme(centerPoint.length(), angle, oldR);

          if(xAngle == 0) return null;
          double angleRotate = 0;
          if(angle >= (Math.PI / 2)) {
            angleRotate = Math.PI - angle - xAngle;
            //поворачиваем в обратную сторону
            centerPoint.rotate(angleRotate, vectorDir);  
          }else if(angle < (Math.PI / 2)) {
            angleRotate = angle - xAngle;
            //поворачиваем в ту же сторону
            centerPoint.rotate(angleRotate, !vectorDir);
          };
          
          return centerPoint;
        }

        private static double CalcSinTheoreme(double _l1, double _angle,
            double _l2) {
            double res = _l2 * Math.sin(_angle) / _l1;
            if(res > 1) res = 1;
          return Math.asin(res);
        }
        
        /**Рассчитать угол сопряжения 2-х GEO команд, зная соответствующие нормали
         * (в градусах)
         * @param vector1
         * @param vector2
         * @return
         */
        public static double calculateCrossAngleGrad(Point vector1, Point vector2)
        {
            double vector2Angle = MathUtils.calculateAngle2(vector2.x, vector2.y);
      
            Point newVector1 = MathUtils.rotateCoords(vector2Angle, vector1.x, vector1.y, false);
      
            double crossAngle = Math.toDegrees(MathUtils.calculateAngle2(newVector1.x, newVector1.y));
      
            return 180 - crossAngle;
        }

        /**Рассчитать угол сопряжения 2-х GEO команд, зная соответствующие нормали
         * (в радианах)
         * @param vector1
         * @param vector2
         * @return
         */
        public static double calculateCrossAngleRad(Point vector1, Point vector2)
        {
            double vector2Angle = MathUtils.calculateAngle2(vector2.x, vector2.y);
      
            Point newVector1 = MathUtils.rotateCoords(vector2Angle, vector1.x, vector1.y, false);
      
            double crossAngle = MathUtils.calculateAngle2(newVector1.x, newVector1.y);
      
            return Math.PI - crossAngle;
        }
        
        public static double CheckCosValue(double _cosValue){
        	if(_cosValue > 1) return 1;
        	else if (_cosValue < -1) return -1;
        	return _cosValue;
        };
        
        public static Point CalcCrossArcLine(int _d, Point _firstVector, Point _secondVector) {
          //угол между вектором конца окружности и прямой
          double angle = Math.toRadians(Math.abs(calculateCrossAngleGrad(_firstVector, _secondVector)));
          double corrR = 0;
          if(angle != 0)
            corrR = Math.abs(_d) / Math.sin(angle);
          //направление от центра к вектору
          boolean vectorDir = CheckDirectionMove(_firstVector,_secondVector);
         
          //вычисляем параметры для теоремы синусов
          if(((_d > 0) && vectorDir) ||
             ((_d < 0) && !vectorDir))
            corrR += _firstVector.length();
          else
            corrR = _firstVector.length() - corrR;
          
          //величина угла 180 = angleRotate + xAngle + angle 
          double xAngle = CalcSinTheoreme(_firstVector.length(), angle, corrR);
          double angleRotate = Math.PI - angle - xAngle;
          //поворачивваем в обратную сторону
          _firstVector.rotate(angleRotate,false);                
          return _firstVector;
        }
        
        public static Point CalcCrossArcArc(int _d, CachedCpFrame _curFrame,
            CachedCpFrame _nextFrame, Point _newCurCenter, Point _newNextEnd, boolean _correct, boolean _cross) {
          //вычисляем старые и новые радиуса и расстояния между центрами
          //корректируем радиус
          double corrR =  _d  / _newCurCenter.length();
          Point oldCurCenter = new Point();
          if((_correct) && (!_cross)){
            if(_curFrame.hasG03()) corrR = -corrR;
            oldCurCenter.x = _newCurCenter.x;
            oldCurCenter.y = _newCurCenter.y;
            _newCurCenter.x *= (1 + corrR);
            _newCurCenter.y *= (1 + corrR);
          }else {
            if(_curFrame.hasG02()) corrR = -corrR;
            oldCurCenter.x = (int)Math.round((_newCurCenter.x * (1 + corrR)));
            oldCurCenter.y = (int)Math.round((_newCurCenter.y * (1 + corrR)));
          };
          
          //старый радиус второй окружности
          int oldNextCenterX = Utils.toInt(_nextFrame.getDataByType(CC.I));
          int oldNextCenterY = Utils.toInt(_nextFrame.getDataByType(CC.J));
          Point oldNextCenter = new Point(oldNextCenterX,oldNextCenterY);
          Point newNextCenter = new Point();
          corrR =  _d / oldNextCenter.length();
          if(!_cross) {
            //корректируем радиус
            if(_nextFrame.hasG03()) corrR =  - corrR;
            
            newNextCenter.x = (int)Math.round((oldNextCenter.x * (1 + corrR)));
            newNextCenter.y = (int)Math.round((oldNextCenter.y * (1 + corrR))); 
            _newNextEnd.x = (int)Math.round((_newNextEnd.x * (1 + corrR)));
            _newNextEnd.y = (int)Math.round((_newNextEnd.y * (1 + corrR)));
          
          }else{
            if(_nextFrame.hasG02()) corrR = -corrR;
            newNextCenter.x = oldNextCenter.x;
            newNextCenter.y = oldNextCenter.y;
            oldNextCenter.x *= (1 + corrR);
            oldNextCenter.y *= (1 + corrR);
          };
          Point distanceBetweenCenters = new Point(oldCurCenter.x + oldNextCenter.x,
                                                   oldCurCenter.y + oldNextCenter.y);
          double alpha = 0;
          double rotateAngle = 0;
          if(distanceBetweenCenters.length() != 0) {
            //вычмсляем углы по теореме косинусов
            //r2^1 =  a^2 + r1^2 - 2*a*r1*cos(alpha)
            alpha = (Math.pow(newNextCenter.length(), 2)- Math.pow(distanceBetweenCenters.length(),2) - Math.pow(_newCurCenter.length(),2))
                         / (-2 * distanceBetweenCenters.length() * _newCurCenter.length());
            
            alpha = Math.acos(CheckCosValue(alpha));
            
           
            double angle = (Math.pow(oldNextCenter.length(), 2) - Math.pow(oldCurCenter.length(),2) - Math.pow(distanceBetweenCenters.length(),2))
            / (-2 * oldCurCenter.length() * distanceBetweenCenters.length()); 
            rotateAngle = alpha - Math.acos(CheckCosValue(angle));

          };
          
          _newCurCenter.rotate(rotateAngle, CheckDirectionMove(_newCurCenter, newNextCenter));
          Point crossPoint = new Point();
          crossPoint.x = (int)(_newCurCenter.x - oldCurCenter.x);
          crossPoint.y = (int)(_newCurCenter.y - oldCurCenter.y);
          if(_cross) {
            crossPoint.x += (newNextCenter.x - oldNextCenter.x);
            crossPoint.y += (newNextCenter.y - oldNextCenter.y);
          };
//          _newNextEnd.rotate(rotateAngle, !CheckDirectionMove(_newCurCenter, newNextCenter));
//            crossPoint.x = 0;//(int)(_newCurCenter.x - oldCurCenter.x);
//            crossPoint.y = 0;//(int)(_newCurCenter.y - oldCurCenter.y);
          return crossPoint;
        }

        
}







