package ru.autogenmash.core;

/**
 * Дуговой 2D интерполятор.
 * <p>
 * Imported from C++.
 * 
 * @author unknown (import by Dymarchuk Dmitry)
 * @version 23.05.2007 16:33:03
 */
public class ArcInterpolator {
	public static final double _2PI_DIGIT = Math.PI * 2;
	public static final double _PI_DIGIT_DIV180 = Math.PI / 180;
	public static final double EPSILON = 0.0001;
	public static final int R_30 = 30;
	public static final int R_60 = 60;

	/** Длина шага интерполирования дуги. */
	public static final int ARC_FEED = 10;

	public static boolean floatEq(double x, double v) {
		if (((v - EPSILON) < x) && (x < (v + EPSILON)))
			return true;
		else
			return false;
	}

	public static double getAngle(double x, double y) {
		double res = Math.atan2((double) y, (double) x);
		if ((res) < 0)
			res += _2PI_DIGIT; // преобразуем диапазон от [-П;+П] к [0;2П]
		return res;
	}

	public static long getDistance(double x, double y) {
		return Math.round(Math.sqrt(x * x + y * y));
	}

	/**
	 * Расчет угла между векторами в градусах 0-180.
	 * 
	 * @param firstvect
	 * @param secondvect
	 * @return
	 */
	public static int solveAnglePt(Point firstvect, Point secondvect) {
		// на основе двух векторов считаем угол меж ними
		double anglerad = Math.atan2((double) secondvect.y, (double) secondvect.x)
				- Math.atan2((double) firstvect.y, (double) firstvect.x);
		// переводим в градусы
		int angle = (int) Math.round(anglerad / _PI_DIGIT_DIV180);
		// приводим угол в 0-180
		if (angle < 0)
			angle = -angle;
		if (angle > 180)
			angle = 360 - angle;
		return angle;
	}

	public static int solveRadius(int r, int sector) {
		if (r < R_30) {// 3мм
			if (sector > 5)
				return 120;
		} else if (r < R_60) {
			if (sector > 3)
				return 80;
		}

		return sector;
	}

	/** Круговая подача, угол шага: в радианах. */
	protected double _anFeed;

	/** направление обхода: -1 по ЧС, 1 против ЧС. */
	protected int _arcDirection;

	/** СК круговая: начальный угол интерполирования, в радианах. */
	protected double _beginAngle;

	/** интерполяция закончена, конец. */
	protected boolean _bEndOfInterp;

	/** выполняется последний шаг интерполяции, как выполнится - все, конец. */
	protected boolean _bIsLastStep;

	/** СК от начала: точка центра дуги. */
	protected Point _centerPoint;

	/** СК круговая: текущий угол интерполирования, в радианах. */
	protected double _currAngle;

	/** точки интерполяции. */
	protected Point _currPoint;

	/** СК от центра: текущая точка интерполирования. */
	protected Point _currPointCenter;

	/** СК круговая: конечный угол интерполирования, в радианах. */
	protected double _endAngle;

	protected Point _endPoint;

	/** СК от центра: конечная точка дуги. */
	protected Point _endPointCenter;

	/** коэффициет корректировки поседнего шага. */
	protected double _lastStepFactor;

	protected Point _normVect;

	/** остаток интерполирования, курсовой изменияется от "пути команды"->0. */
	protected double _remainedS;

	/** Линейная подача, шаг. */
	protected double _stFeed;

	/** радиус дуги. */
	protected long R;

	// конструктор, он же подготавливает интерполятор
	// точка 0,0 - полная окружность
	public ArcInterpolator(int isCW, final Point point1, final Point point2) {
		_centerPoint = new Point(0, 0);
		_endPointCenter = new Point(0, 0);
		_currPointCenter = new Point(0, 0);
		_currPoint = new Point(0, 0);
		_arcDirection = 0;
		R = 0;
		_currAngle = 0;
		_endAngle = 0;
		_beginAngle = 0;
		_anFeed = 0;
		_stFeed = 0;

		// подготовить внутренние поля,
		// шаг 1. конечн точка, тек - исходная СК -
		_endPoint = point1;
		_centerPoint = point2;
		// шаг 2. конечная, текущая точка - СК от центра
		_endPointCenter.x = point1.x - point2.x;
		_endPointCenter.y = point1.y - point2.y;
		_currPointCenter.x = -point2.x;
		_currPointCenter.y = -point2.y;

		// направление дуги -1: по часовой, 1: против
		_arcDirection = isCW;
		// if(isCW){_arcDirection=-1;}else{_arcDirection=1;};

		// радиус окр
		R = getDistance((double) point2.x, (double) point2.y);
		// шаг 3. определить позиции начала и конца в круговой СК: углы дуги в
		// абсолютных: 0-2Pi
		// угол начальной точки, от 0, 0-2Pi}
		_beginAngle = getAngle((double) _currPointCenter.x,
				(double) _currPointCenter.y);
		// угол конечной точки, от 0, 0-2Pi}
		_endAngle = getAngle((double) _endPointCenter.x, (double) _endPointCenter.y);

		// шаг4 перевести круговую СК относительно начального угла.
		// пересчитать конечный угол, относительно начального, учитывая
		// направление обхода
		// т.е. получаем строго конечУгол>= начального, но в границах 0-2Pi
		if ((_endPoint.x == 0) && (_endPoint.y == 0)) {
			// случай конечной точки=начальной (0,0)
			_endAngle = 0;
		} else if (_arcDirection < 0) {
			if (_beginAngle <= _endAngle)
				_endAngle = (setValue(_2PI_DIGIT - (_endAngle - _beginAngle)));
			else
				_endAngle = (setValue(_endAngle - _beginAngle));
		} else {
			if (_beginAngle >= _endAngle)
				_endAngle = (setValue(_2PI_DIGIT - (_beginAngle - _endAngle)));
			else
				_endAngle = (setValue(_beginAngle - _endAngle));
		}

		_currAngle = 0; // текущая точка в круговой СК, с НК в начальном угле

		// перемещение можно строить от angle(=0)->EndAngle(=число от 0-2pi)
		// считая пройденный путь в углах

		// расчитать путь интерполяции, исходя из формулы:
		// l = a*R*PI/180, где a-угловое растояние интерполирования, R - радиус
		// дуги
		// угловое растояние интерполирования = _endAngle, тк оно отноистелльно
		// начального угла
		_remainedS = _endAngle * R;

		if (_remainedS == 0) {
			_bIsLastStep = true;
			_beginAngle = 0;
			_endAngle = 0;
			R = 0;
		}
	}

	/**
	 * Сделать шаг интерполирования, от курсовой скорости feed, вернуть вектор и
	 * остаток пути.
	 * 
	 * @param feed
	 * @param step
	 * @return
	 */
	public long doStep(double feed, Point step) {
		if (_bIsLastStep) {// посл шаг уже был сделан, интерп закончена,
			// топчемся на месте
			_bEndOfInterp = true;
			step.x = 0;// нулевой вектор, нет перемещения
			step.y = 0;// нулевой вектор, нет перемещения
			_currPoint = _endPoint;
			_remainedS = 0;
			return 0;
		}

		// расчитать угол шага интерполяции
		if (!floatEq(feed, _stFeed)) {// если подача изменилась, пересчитать
			// уголовой шаг
			_anFeed = 2 * Math.asin((double) feed / (2 * R));
			_stFeed = feed;
		}

		// расчет остатка пути и проверка на последний шаг
		_remainedS -= _stFeed;// вычесть из общего пути шаг перемещения(feed)
		// koefMove 0.5 в обычных условиях, при торможении 0.1
		if (_remainedS <= (_lastStepFactor * feed)) {
			// Прошли весь путь, последний шаг перешагнул за границы дуги
			_bIsLastStep = true;
			step.x = _endPoint.x - _currPoint.x;
			step.y = _endPoint.y - _currPoint.y;
			_currPoint = _endPoint;
			_remainedS = 0;
			return 0;
		}

		// шаг не последний,
		// расчитать шаг интерполяции, через расчет угла и перевода в декартовы
		// координаты
		_currAngle += _anFeed;// вычислили уголовую позицию на дуге

		// выполнить обратное преодразование из круговых от начала -> декартовы
		// от начала
		// тек поз перевести в декартовы, СК от центра
		// тек поз получить в СК от начала интерполирования.
		if (_arcDirection == -1) {
			_currPointCenter.x = (int) ((double) R * Math.cos(_beginAngle
					- _currAngle));
			_currPointCenter.y = (int) ((double) R * Math.sin(_beginAngle
					- _currAngle));
		} else {
			_currPointCenter.x = (int) ((double) R * Math.cos(_beginAngle
					+ _currAngle));
			_currPointCenter.y = (int) ((double) R * Math.sin(_beginAngle
					+ _currAngle));
		}
		Point prevPoint = new Point(_currPoint.x, _currPoint.y);
		_currPoint.x = _centerPoint.x + _currPointCenter.x;
		_currPoint.y = _currPointCenter.y + _centerPoint.y;

		// получит шаг интерполирования
		step.x = _currPoint.x - prevPoint.x;
		step.y = _currPoint.y - prevPoint.y;

		return Math.round(_remainedS);
	};

	public double getFRemainedS(int axis) {
		switch (axis) {
		case 0:
			return _remainedS;
			// общий путь (по обоим осям)
		case 1:
			return Math.abs((double) (_endPoint.x) - (double) (_currPoint.x));// abs(xAxis.endPoint
			// -
			// (int)xAxis.currPoint);
		case 2:
			return Math.abs((double) (_endPoint.y) - (double) (_currPoint.y));// abs(yAxis.endPoint
			// -
			// (int)yAxis.currPoint);
		}
		return 0;
	}

	// длина радиуса
	int GetR() {
		return (int) R;// _centerPoint.Get_Distance();
	}

	/**
	 * Получить остаток пути интерполирования по оси, ось=0 то полный путь
	 * интерполирования.
	 * 
	 * @return
	 */
	public long getRemainedS() {
		return Math.round(_remainedS);
	} // общий путь (по обоим осям)

	// утановить значение aValue
	public double setValue(double value) {
		// aValue привести к формату 0-2Pi
		double aValue;
		if (floatEq(value, 0))
			aValue = 0;// 0=0 остальное либо угол либо 2Pi}
		else {
			// выбираем число целых 2Pi, и удаляем их
			int del = (int) (value / _2PI_DIGIT);// _trunc(_value/_2PI_DIGIT);
			if (del < 0)
				del = -del;
			aValue = value - (del * _2PI_DIGIT);
			// приводим к плюсу
			if (aValue < 0)
				aValue = -aValue;
			if (aValue == 0)
				aValue = _2PI_DIGIT;
		}
		return aValue;
	}

}