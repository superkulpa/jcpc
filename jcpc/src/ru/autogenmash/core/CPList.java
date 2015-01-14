package ru.autogenmash.core;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class CPList implements Cloneable {

	/** Длина номера подкадра (например: 0000000003, 0000000102 и т.п.). */
	public static final int SUB_FRAME_NUMBER_LENGTH = 10;

	public static CachedCpFrame getFirstGeoFrame(CachedCpFrame[] frames, int type) {
		CachedCpFrame firstFrame = null;
		for (int k = 0; k < frames.length; k++)
			if ((type < 0 && frames[k].isGeo())
					|| (type >= 0 && frames[k].getType() == type)) {
				firstFrame = frames[k];
				break;
			}

		return firstFrame;
	}

	public static CachedCpFrame getFirstGeoFrame(CachedCpFrame[] frames) {
		return getFirstGeoFrame(frames, -1);
	}

	public static CachedCpFrame getLastGeoFrame(CachedCpFrame[] frames) {
		CachedCpFrame lastFrame = null;
		for (int k = frames.length - 1; k >= 0; k--)
			if (frames[k].isGeo()) {
				lastFrame = frames[k];
				break;
			}

		return lastFrame;
	}

	private CachedCpFrame[] _data;
	/** Габариты детали(контура) с холостыми перемещениями. */
	private Point _shapeSize;
	/** Вектор от начальной точки УП до конечной точки УП. */
	private Point _endPosition;

	/** Минимальная позиция (по X и Y) относительно начала контура. */
	private Point _minPosition;
	/** Максимальная позиция (по X и Y) относительно начала контура. */
	private Point _maxPosition;

	/**
	 * Вспомогательная информация (например информация о наличии циклов,
	 * подпрограмм).
	 */
	private HashMap _auxData = new HashMap();

	public CPList() {
		this(new CachedCpFrame[0]);
	}

	public CPList(CachedCpFrame[] data) {
		setData(data);
	}

	public CPList(List data) {
		setData(data);
	}

	public Object clone() throws CloneNotSupportedException {
		if (this.getClass().equals(CPList.class) == false)
			throw new IllegalArgumentException("some problems in cloning cp lists");

		CPList cpList = (CPList) super.clone();
		CachedCpFrame[] frames = new CachedCpFrame[getLength()];
		for (int i = 0; i < frames.length; i++)
			frames[i] = (CachedCpFrame) getFrame(i).clone();

		cpList.setData(frames);

		cpList._auxData = (HashMap) _auxData.clone();
		if (_shapeSize != null)
			cpList._shapeSize = (Point) _shapeSize.clone();
		if (_endPosition != null)
			cpList._endPosition = (Point) _endPosition.clone();
		if (_minPosition != null)
			cpList._minPosition = (Point) _minPosition.clone();
		if (_maxPosition != null)
			cpList._maxPosition = (Point) _maxPosition.clone();

		return cpList;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (obj instanceof CPList == false)
			return false;

		CPList cpList2 = (CPList) obj;

		if (_data.length != cpList2.getLength())
			return false;

		// if (_endPosition != cpList2.getEndPosition() |
		// _maxPosition != cpList2.getMaxPosition() |
		// _minPosition != cpList2.getMinPosition() |
		// _shapeSize != cpList2.getShapeSize())
		// return false;
		//
		// if (_endPosition.equals(cpList2.getEndPosition()) &
		// _maxPosition.equals(cpList2.getMaxPosition()) &
		// _minPosition.equals(cpList2.getMinPosition()) &
		// _shapeSize.equals(cpList2.getShapeSize()) == false)
		// return false;

		// if (_auxData.equals(cpList2.getAuxData()))

		for (int i = 0; i < _data.length; i++) {
			if (_data[i].equals(cpList2.getData()[i]) == false)
				return false;
		}

		return true;
	}

	public void setData(CachedCpFrame[] data) {
		_data = data;
		_shapeSize = null;
	}

	public void setData(List data) {
		_data = (CachedCpFrame[]) data.toArray(new CachedCpFrame[0]);
		_shapeSize = null;

		// // old implementation
		// int size = data.size();
		// _data = new CachedCpFrame[size];
		// int i = 0;
		//
		// Iterator iterator = data.iterator();
		// while (iterator.hasNext())
		// {
		// Object object = iterator.next();
		// if (object instanceof CachedCpFrame)
		// _data[i] = (CachedCpFrame)object;
		// else
		// throw new IllegalArgumentException("Элементами списка \"" + data +
		// "\" должны быть экземпляры класса \"" + CachedCpFrame.class.getName() +
		// "\"");
		// i++;
		// }
	}

	/**
	 * Добавить вспомогательную информацию (переписать если такая уже есть).
	 * Например: key="loops" value="true" - в УП есть циклы.
	 * 
	 * @param key
	 * @param value
	 */
	public void addAuxData(String key, Object value) {
		_auxData.put(key, value);
	}

	public Object getAuxData(String key) {
		return _auxData.get(key);
	}

	public CachedCpFrame[] getData() {
		return _data;
	}

	public int getLength() {
		return _data.length;
	}

	public int getGeoFramesCount() {
		int count = 0;
		for (int i = 0; i < _data.length; i++)
			if (_data[i].isGeo())
				count++;

		return count;
	}

	public CachedCpFrame getFrame(int position) {
		if (position >= _data.length || position < 0)
			return null;
		return _data[position];
	}

	public boolean setFrame(int position, CachedCpFrame frame) {
		if (position >= _data.length)
			return false;

		if (frame == null || frame.getType() == CpFrame.FRAME_TYPE_ARC
				|| frame.getType() == CpFrame.FRAME_TYPE_LINE)
			_shapeSize = null;

		_data[position] = frame;
		return true;
	}

	protected void printToFile(String fileName, boolean decompile, String tag)
			throws FileNotFoundException, IOException {
		FileOutputStream file = new FileOutputStream(fileName);

		if (tag != null && tag.trim().equals("") == false)
			file.write((tag + "\n").getBytes());

		for (int i = 0; i < getLength(); i++) {
			final String str = "N" + (i + 1) + " "
					+ getFrame(i).generateCpString(decompile) + "\n";
			file.write(str.getBytes());
		}

		file.close();
	}

	public void printToFile(String fileName, String tag)
			throws FileNotFoundException, IOException {
		printToFile(fileName, false, tag);
	}

	public void printToFile(String fileName) throws FileNotFoundException,
			IOException {
		printToFile(fileName, false, null);
	}

	public void decompileToFile(String fileName, String tag)
			throws FileNotFoundException, IOException {
		printToFile(fileName, true, tag);
	}

	public void decompileToFile(String fileName) throws FileNotFoundException,
			IOException {
		printToFile(fileName, true, null);
	}

	public static String generateNumber(int frame, int subFrame) {
		return "0x" + Integer.toHexString(frame) + subFrame;
	}

	public void generateCCP(Writer ccp) throws IOException {
		int subFrameNumber = 0;

		// ccp.write("cp\n");
		// ccp.write("0x" + StringUtils.extractNumber(1, SUB_FRAME_NUMBER_LENGTH,
		// '0') +
		// ":" + CpSubFrame.RC_TERMINAL_COMMAND + "\n");
		ccp.write(generateNumber(0, 0) + ":" + CpSubFrame.RC_TERMINAL_COMMAND
				+ "\n");

		for (int i = 0; i < getLength(); i++) {
			subFrameNumber = 0;
			CpFrame frame = getFrame(i);
			for (int j = 0; j < frame.getLength(); j++) {
				CpSubFrame subFrame = frame.getSubFrame(j);
				if (subFrame.getType() > 0) // подкадры с типом <= 0 (например RC_NULL)
																		// не обрабатываются
				{
					subFrameNumber++;
					String ccpString = subFrame.generateCcpString();
					if (ccpString == null)
						continue;

					// десятичный вариант
					// String extractedSubFrameNumber = StringUtils.extractNumber(number,
					// SUB_FRAME_NUMBER_LENGTH, '0');
					// шестнадцатиричный вариант
					String line = generateNumber(i + 1, subFrameNumber) + ":" + ccpString
							+ "\n";
					ccp.write(line);
				}
			}
		}

		String endTerminalLine = generateNumber(getLength() + 1, subFrameNumber)
				+ ":" + CpSubFrame.RC_TERMINAL_COMMAND;
		// String endTerminalLine = "0x" + StringUtils.extractNumber(number,
		// SUB_FRAME_NUMBER_LENGTH, '0') +
		// ":" + CpSubFrame.RC_TERMINAL_COMMAND;
		ccp.write(endTerminalLine);

		ccp.close();
	}

	/**
	 * @param radiansAngle
	 * @param direction
	 *          - true против ЧС
	 */
	public void rotate(double radiansAngle, boolean direction) {
		for (int i = 0; i < _data.length; i++)
			_data[i].rotate(radiansAngle, direction);

		_shapeSize = null;
	}

	/**
	 * Be carefull !!! Данный X - декартовый, а не координата машины
	 */
	public void reverseByX() {
		for (int i = 0; i < _data.length; i++)
			_data[i].reverseByX();

		_shapeSize = null;
	}

	/**
	 * Be carefull !!! Данный Y - декартовый, а не координата машины
	 */
	public void reverseByY() {
		for (int i = 0; i < _data.length; i++)
			_data[i].reverseByY();

		_shapeSize = null;
	}

	public void scale(double k) {
		for (int i = 0; i < _data.length; i++)
			_data[i].scale(k);

		_shapeSize = null;
	}

	/**
	 * Удалить null - кадры и перестроить ЦП лист. Ресурсоемкий метод.
	 * Использовать только при "реальной" необходимости.
	 * 
	 * @return количество удаленных null - кадров
	 */
	public int defragment() {
		int nullFramesCount = 0;

		Vector newData = new Vector(_data.length);
		for (int i = 0; i < _data.length; i++) {
			if (_data[i] == null) {
				nullFramesCount++;
				continue;
			} else
				newData.add(_data[i]);
		}

		setData(newData);

		return nullFramesCount;
	}

	/**
	 * Вычисление габаритов контура (вместе с подходом/отходом)
	 * 
	 */
	protected void calculateShapeSize() {
		Point min = new Point(0, 0);
		Point max = new Point(0, 0);
		Point currPosition = new Point(0, 0);
		Point endPoint = new Point(0, 0);
		Point centerPoint = new Point(0, 0);
		Point stepPoint = new Point(0, 0);

		ArcInterpolator arcInterpolator;

		for (int p = 0; p < getLength(); p++) {
			CachedCpFrame frame = getFrame(p);

			if (frame.getType() == CpFrame.FRAME_TYPE_LINE) {
				CpSubFrame[] subFrames = frame.getData();
				for (int k = 0; k < subFrames.length; k++) {
					CpSubFrame subFrame = subFrames[k];
					if (subFrame.getType() == CpSubFrame.RC_GEO_LINE
							|| subFrame.getType() == CpSubFrame.RC_GEO_FAST) {
						Integer x = subFrame.getDataByType(CC.X);
						Integer y = subFrame.getDataByType(CC.Y);

						currPosition.x += (x == null ? 0 : x.intValue());
						currPosition.y += (y == null ? 0 : y.intValue());
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
			} else if (frame.getType() == CpFrame.FRAME_TYPE_ARC) {
				int direction = frame.hasG02() ? -1 : 1;

				CpSubFrame[] subFrames = frame.getData();
				for (int k = 0; k < subFrames.length; k++) {
					CpSubFrame subFrame = subFrames[k];
					if (subFrame.getType() == CpSubFrame.RC_GEO_ARC) {
						Integer x = subFrame.getDataByType(CC.X);
						Integer y = subFrame.getDataByType(CC.Y);
						Integer i = subFrame.getDataByType(CC.I);
						Integer j = subFrame.getDataByType(CC.J);

						endPoint.x = (x == null ? 0 : x.intValue());
						endPoint.y = (y == null ? 0 : y.intValue());
						centerPoint.x = (i == null ? 0 : i.intValue());
						centerPoint.y = (j == null ? 0 : j.intValue());
						arcInterpolator = new ArcInterpolator(direction, endPoint,
								centerPoint);
						double sizeFactor = (double) arcInterpolator.getRemainedS() / 1000;
						if (sizeFactor < 1)
							sizeFactor = 1;

						while (arcInterpolator.getRemainedS() > 0) {
							arcInterpolator.doStep(Math.round(sizeFactor
									* ArcInterpolator.ARC_FEED), stepPoint);
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
			}
		}

		// габариты
		_minPosition = min;
		_maxPosition = max;
		_shapeSize = new Point(max.x - min.x, max.y - min.y);
		_endPosition = currPosition;
	}

	public Point getShapeSize() {
		checkShapeSize();
		return _shapeSize;
	}

	/**
	 * Если габариты контура не посчитаны, то посчитать их.
	 * 
	 */
	private void checkShapeSize() {
		if (_shapeSize == null)
			calculateShapeSize();
	}

	/**
	 * Получить вектор от начальной точки УП до конечной точки УП
	 * 
	 * @return
	 */
	public Point getEndPosition() {
		checkShapeSize();
		return _endPosition;
	}

	public Point getMaxPosition() {
		checkShapeSize();
		return _maxPosition;
	}

	public Point getMinPosition() {
		checkShapeSize();
		return _minPosition;
	}

	public CachedCpFrame getFirstGeoFrame() {
		return getFirstGeoFrame(getData());
	}

	public CachedCpFrame getLastGeoFrame() {
		return getLastGeoFrame(getData());
	}

	public void addCPList(CPList cpList) {
		int newLength = getLength() + cpList.getLength();
		CachedCpFrame[] dataTmp = new CachedCpFrame[newLength];

		for (int i = 0; i < getLength(); i++) {
			dataTmp[i] = _data[i];
		}
		for (int i = getLength(); i < newLength; i++) {
			dataTmp[i] = cpList.getFrame(i - getLength());
		}

		_data = dataTmp;

		// CachedCpFrame[] newData =
		// (CachedCpFrame[])Arrays.copyOf(cpList.getData(), newLength);
		_shapeSize = null;
	}
}
