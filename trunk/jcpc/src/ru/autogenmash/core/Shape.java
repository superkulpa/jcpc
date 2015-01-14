package ru.autogenmash.core;

import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;

/**
 * @author Dymarchuk Dmitry moving date: 31.07.2009 16:09:43
 */
public class Shape extends CPList {

	/** Кадр(ы) захода на контур. */
	private CachedCpFrame[] _entranceFrames;
	/** Подход (в относительных координатах). */
	private CachedCpFrame _leadInFrame;
	/** Отход (в относительных координатах). */
	private CachedCpFrame _leadOutFrame;
	/** Начальная точка контура (в абсолютных координатах). */
	private Point _startPoint;
	/** Конечная точка контура (в абсолютных координатах). */
	private Point _endPoint;
	// /** Начальная точка контура (в абсолютных координатах) с учетом
	// эквидистанты. */
	// private Point _kerfedStartPoint;
	// /** Конечная точка контура (в абсолютных координатах) с учетом
	// эквидистанты. */
	// private Point _kerfedEndPoint;
	//
	// /** Внешний контур или внутренний. */
	// private boolean _isOuter;

	private int _d;

	private boolean _g41;
	private boolean _g42;

	private Boolean _isSpaceless = null;

	public Shape(CachedCpFrame[] data, boolean g41, boolean g42, int d,
			CachedCpFrame[] entranceFrames, CachedCpFrame leadInFrame,
			CachedCpFrame leadOutFrame, Point startPoint, Point endPoint) {
		super(data);
		_g41 = g41;
		_g42 = g42;
		_d = d;
		_entranceFrames = entranceFrames;
		_leadInFrame = leadInFrame;
		_leadOutFrame = leadOutFrame;
		_startPoint = startPoint;
		_endPoint = endPoint;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			throw new IllegalArgumentException("Shape can not be null");

		if (obj instanceof Shape == false)
			return false;

		if (super.equals((CPList) obj) == false)
			return false;

		Shape shape2 = (Shape) obj;
		if (shape2.getFullLength() != getFullLength())
			return false;

		if (_d == shape2.getDValue() & _g41 == shape2.isG41()
				& _g42 == shape2.isG42() & _leadInFrame.equals(shape2.getLeadInFrame())
				& _leadOutFrame.equals(shape2.getLeadOutFrame()) == false)
			return false;

		return true;
	}

	public void setLeadInFrame(CachedCpFrame leadInFrame) {
		_leadInFrame = leadInFrame;
	}

	public void setLeadOutFrame(CachedCpFrame leadOutFrame) {
		_leadOutFrame = leadOutFrame;
	}

	public int getFullLength() {
		int length = getLength();
		if (_leadInFrame != null)
			length++;
		if (_leadOutFrame != null)
			length++;
		length += _entranceFrames.length;

		return length;
	}

	public int getDValue() {
		return _d;
	}

	public boolean isKerfed() {
		return !(_g41 == false && _g42 == false);
	}

	/**
	 * Замкнутый контур или нет.
	 * 
	 * @return true, если замкнутый, false иначе
	 */
	public boolean isSpaceless() {
		if (_isSpaceless != null)
			return _isSpaceless.booleanValue();

		if (MathUtils.compareDouble((double) _startPoint.x, (double) _endPoint.x,
				100)
				&& MathUtils.compareDouble((double) _startPoint.y,
						(double) _endPoint.y, 100))
			_isSpaceless = new Boolean(true);
		else {
			CachedCpFrame firstFrame = getFirstGeoFrame(getData());

			int driftX = _endPoint.x - _startPoint.x;
			int driftY = _endPoint.y - _startPoint.y;
			int firstFrameX = Utils.toInt(firstFrame.getDataByType(CC.X));
			int firstFrameY = Utils.toInt(firstFrame.getDataByType(CC.Y));

			if (driftX == firstFrameX && driftY == firstFrameY) {
				_isSpaceless = new Boolean(false);
				return _isSpaceless.booleanValue();
			}

			CachedCpFrame lastFrame = getLastGeoFrame(getData());

			int x = Utils.toInt(lastFrame.getDataByType(CC.X));
			int y = Utils.toInt(lastFrame.getDataByType(CC.Y));
			int xAbs = _endPoint.x - _startPoint.x - x;
			int yAbs = _endPoint.y - _startPoint.y - y;

			Point intersectionPoint = MathUtils.calculateIntersectionPoint(
					firstFrame, lastFrame, xAbs, yAbs);
			if (intersectionPoint != null)
				_isSpaceless = new Boolean(true);
			else
				_isSpaceless = new Boolean(false);
		}

		return _isSpaceless.booleanValue();
	}

	/**
	 * Нормальный контур или нет (т.е. совпадает ли начальная точка с конечной)
	 * 
	 * @return true, если нормальный, false иначе
	 */
	public boolean isNormal() {
		if (_startPoint.x == _endPoint.x && _startPoint.y == _endPoint.y)
			return true;

		return false;
	}

	// public boolean isOuter()
	// {
	// return _isOuter;
	// }

	public boolean isG41() {
		return _g41;
	}

	public boolean isG42() {
		return _g42;
	}

	public Point getEndPoint() {
		return _endPoint;
	}

	public Point getStartPoint() {
		return _startPoint;
	}

	public CachedCpFrame[] getEntranceFrames() {
		return _entranceFrames;
	}

	public CachedCpFrame getLeadInFrame() {
		return _leadInFrame;
	}

	public CachedCpFrame getLeadOutFrame() {
		return _leadOutFrame;
	}

}