package ru.autogenmash.core;

import ru.autogenmash.core.utils.MathUtils;

/**
 * @author Dymarchuk Dmitry 26.03.2007 9:15:24
 */
public class Point {
	public static final Point ZERO = new Point();

	public int x;
	public int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Point() {
		this(0, 0);
	}

	public Point(Point point) {
		x = point.x;
		y = point.y;
	}

	public Object clone() throws CloneNotSupportedException {
		return new Point(x, y);
	}

	public int hashCode() {
		return x ^ y;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof Point == false)
			return false;

		Point point = (Point) obj;
		return (point.x == this.x) && (point.y == this.y);
	}

	public String toString() {
		return "Point {" + x + ", " + y + "}";
	}

	public boolean isZero() {
		return x == 0 & y == 0;
	}

	/**
	 * Повернуть вектор (x,y) на угол (в радианах)
	 * 
	 * @param inversion
	 *          true - против часовой
	 */
	public void rotate(double radAngle, boolean inversion) {
		Point point = MathUtils.rotateCoords(radAngle, x, y, inversion);
		this.x = point.x;
		this.y = point.y;
	}

	/**
	 * Повернуть вектор (x,y) на угол (в градусах)
	 * 
	 * @param inversion
	 *          true - против часовой
	 */
	public void rotate2(double degreeAngle, boolean inversion) {
		rotate(Math.toRadians(degreeAngle), inversion);
	}

	public void inverse() {
		x = -x;
		y = -y;
	}

	public void scale(double factor) {
		x = (int) Math.round(factor * x);
		y = (int) Math.round(factor * y);
	}

	/**
	 * @deprecated use lenght()
	 * @return
	 */
	public double getLength() {
		return Math.sqrt((double) x * x + (double) y * y);
	}

	public double length() {
		return MathUtils.length(x, y);
	}

	public int length2() {
		return (int) Math.round(MathUtils.length(x, y));
	}
}
