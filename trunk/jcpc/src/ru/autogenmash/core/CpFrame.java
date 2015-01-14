package ru.autogenmash.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;

/**
 * Кадр УП.
 * 
 * @author Dymarchuk Dmitry
 * @version 23.07.2007 10:40:02
 */
public abstract class CpFrame {
	public final static int FRAME_TYPE_UNKNOWN = 0;
	public final static int FRAME_TYPE_LINE = 1;
	public final static int FRAME_TYPE_ARC = 2;
	public final static int FRAME_TYPE_ROTATE = 3;
	// TODO uncomment ? public final static int FRAME_TYPE_G30 = 3; ??? !

	// private boolean _available; ? // доступный кадр или нет (например кадр
	// удаленной петли)

	protected int _type;
	protected CpSubFrame[] _data;

	// /** Номер кадра. */
	// private int _number;xxx;

	public CpFrame(int type, final CpSubFrame[] data) {
		super();

		if ((type < 0) || (type > 3))
			throw new IllegalArgumentException(
					"Не корректно задан тип кадра/подкадра");

		_type = type;
		_data = data;
	}

	public Object clone() throws CloneNotSupportedException {
		CachedCpFrame frame = (CachedCpFrame) super.clone();
		CpSubFrame[] subFrames = new CpSubFrame[getLength()];
		for (int i = 0; i < subFrames.length; i++)
			subFrames[i] = (CpSubFrame) getSubFrame(i).clone();

		frame.setData(subFrames);
		frame.setType(_type);

		return frame;
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CpFrame == false)
			return false;

		CpFrame frame2 = (CpFrame) obj;
		if (getLength() != frame2.getLength())
			return false;

		if (_type != frame2.getType())
			return false;

		for (int i = 0; i < _data.length; i++) {
			if (_data[i].equals(frame2.getData()[i]) == false)
				return false;
		}

		return true;
	}

	public String toString() {
		return generateCpString(true);
	}

	public void setData(CpSubFrame[] data) {
		_data = data;
	}

	public void setData(List data) {
		_data = (CpSubFrame[]) data.toArray(new CpSubFrame[0]);
	}

	public boolean setCpSubFrame(int position, CpSubFrame cpSubFrame) {
		if (position >= _data.length || position < 0)
			return false;
		_data[position] = cpSubFrame;
		return true;
	}

	public CpSubFrame[] getData() {
		return _data;
	}

	public CpSubFrame getSubFrame(int position) {
		if (position >= getLength())
			return null;

		return _data[position];
	}

	public CpSubFrame getGeoSubFrame() {
		for (int i = 0; i < getLength(); i++) {
			if (_data[i].isGeo())
				return _data[i];
		}

		return null;
	}

	public CpSubFrame getSubFrameByType(int type) {
		for (int i = 0; i < this.getLength(); i++) {
			if (_data[i].getType() == type)
				return _data[i];
		}

		return null;
	}

	public int getSubFrameByType(int type, CpSubFrame subFrame) {
		int pos = getSubFramePosition(type);
		if (pos >= 0)
			subFrame.setData(_data[pos].getData());

		return pos;
	}

	public int getSubFramePosition(int type) {
		for (int i = 0; i < this.getLength(); i++) {
			if (_data[i].getType() == type)
				return i;
		}

		return -1;
	}

	/**
	 * Получить первую СС, с заданным типом.
	 * 
	 * @param type
	 *          - тип СС (например CC.X)
	 * @return первую CC (если присутствует), иначе null
	 */
	public CC getCCByType(int type) {
		for (int i = 0; i < this.getLength(); i++) {
			CC cc = _data[i].getCCByType(type);
			if (cc != null)
				return cc;
		}

		return null;
	}

	public int getLength() {
		return _data.length;
	}

	public int getType() {
		return _type;
	}

	public void setType(int type) {
		_type = type;
	}

	public boolean isGeo() {
		if (_type == FRAME_TYPE_ARC || _type == FRAME_TYPE_LINE || _type == FRAME_TYPE_ROTATE)
			return true;

		return false;
	}

	public void removeGeoSubFrame() {
		Vector sfs = new Vector(3);
		CpSubFrame[] subFrames = getData();
		for (int k = 0; k < subFrames.length; k++) {
			CpSubFrame subFrame = subFrames[k];
			if (subFrame.isGeo() == false)
				sfs.add(subFrame);
		}
		setData(sfs);
		setType(CpFrame.FRAME_TYPE_UNKNOWN);
	}

	public void removeSubFrameByType(int type) {
		if (type == CpSubFrame.RC_GEO_FAST || type == CpSubFrame.RC_GEO_LINE
				|| type == CpSubFrame.RC_GEO_ARC || type == CpSubFrame.RC_M_COMMAND)
			throw new IllegalArgumentException("Removing subframe with type: " + type
					+ " denied");

		Vector sfs = new Vector(3);
		CpSubFrame[] subFrames = getData();
		for (int k = 0; k < subFrames.length; k++) {
			CpSubFrame subFrame = subFrames[k];
			if (subFrame.getType() != type)
				sfs.add(subFrame);
		}
		setData(sfs);
	}

	public String generateCpString(boolean decompile) {
		String res = "";
		for (int i = 0; i < _data.length; i++) {
			res += _data[i].generateCpString(decompile) + " ";
		}

		return res.trim();
	}

	/**
	 * Получить количество управляющих команд, с типом <b>commandType</b>.
	 * 
	 * @param commandType
	 * @return количество команд, заданного типа или -1.
	 */
	public int getCommandCount(int commandType) {
		int commandCount = 0;

		for (int i = 0; i < getLength(); i++) {
			final CpSubFrame subFrame = getSubFrame(i);
			if (subFrame == null)
				return -1;

			for (int j = 0; j < subFrame.getLength(); j++) {
				final CC cc = subFrame.getCC(j);
				if (cc == null)
					return -1;

				if (cc.getType() == commandType)
					commandCount++;
			}
		}

		return commandCount;
	}

	/**
	 * Присутствует команда с типом <b>commandType</b> (первая в списке) и
	 * значением <b>value</b>
	 * 
	 * @param commandType
	 * @param value
	 * @return
	 */
	public boolean contains(int commandType, int value) {
		for (int i = 0; i < getLength(); i++) {
			final CpSubFrame subFrame = getSubFrame(i);
			if (subFrame == null)
				continue;

			if (subFrame.contains(commandType, value))
				return true;
		}

		return false;
	}

	public boolean contains(CC cc) {
		return contains(cc.getType(), cc.getData());
	}

	/**
	 * Получить данные команды, с типом <b>commandType</b> (первая в списке).
	 * 
	 * @param commandType
	 * @return данные команды. Null если такой команды нет в кадре.
	 */
	public Integer getDataByType(int commandType) {
		for (int i = 0; i < getLength(); i++) {
			final CpSubFrame subFrame = getSubFrame(i);
			if (subFrame == null)
				return null;

			for (int j = 0; j < subFrame.getLength(); j++) {
				final CC cc = subFrame.getCC(j);
				if (cc == null)
					return null;

				if (cc.getType() == commandType)
					return new Integer(cc.getData());
			}
		}

		return null;
	}

	/**
	 * Получить данные команды, с типом <b>commandType</b> (первая в списке).
	 * 
	 * @param commandType
	 * @return данные команды. Null если такой команды нет в кадре.
	 */
	public int[] getDataArrayByType(int commandType) {
		ArrayList list = new ArrayList(5);
		// int[] data = new int[5];
		// int commandCount = 0;

		for (int i = 0; i < getLength(); i++) {
			final CpSubFrame subFrame = getSubFrame(i);
			if (subFrame == null)
				return null;

			for (int j = 0; j < subFrame.getLength(); j++) {
				final CC cc = subFrame.getCC(j);
				if (cc == null)
					return null;

				if (cc.getType() == commandType) {
					// data[commandCount] = cc.getData();
					// commandCount++;
					list.add(new Integer(cc.getData()));
				}
			}
		}

		// if (commandCount == 0)
		// return null;
		// else
		// {
		// int[] newData = new int[commandCount];
		// for (int i = 0; i < newData.length; i++)
		// newData[i] = data[i];
		// return newData;
		// }

		int[] data = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			data[i] = ((Integer) list.get(i)).intValue();

		return data;
	}

	/**
	 * @return эвклидова длина гео кадра (линии или дуги)
	 */
	public double getEuclidLength() {
		for (int l = 0; l < _data.length; l++) {
			if (_data[l].isGeo())
				return _data[l].getEuclidLength();
		}

		return -1;
	}

	/**
	 * Только для дуг.
	 * 
	 * @return -1, если линия, иначе радиус дуги.
	 */
	public double getArcRadius() {
		if (_type == FRAME_TYPE_LINE)
			return -1;

		int i = Utils.toInt(getDataByType(CC.I));
		int j = Utils.toInt(getDataByType(CC.J));

		return MathUtils.length(i, j);
	}

	public Point getXY() {
		for (int l = 0; l < _data.length; l++) {
			if (_data[l].isGeo())
				return _data[l].getXY();
		}

		return null;
	}

	public void push(CpSubFrame subFrame) {
		CpSubFrame[] subFrames = new CpSubFrame[getLength() + 1];
		subFrames[0] = subFrame;
		if (getLength() > 0)
			for (int i = 0; i < getLength(); i++)
				subFrames[i + 1] = _data[i];

		_data = subFrames;

		// Arrays.copyOf();
	}

	public void pushBack(CpSubFrame subFrame) {
		CpSubFrame[] subFrames = new CpSubFrame[getLength() + 1];
		if (getLength() > 0)
			for (int i = 0; i < getLength(); i++)
				subFrames[i] = _data[i];
		subFrames[getLength()] = subFrame;

		_data = subFrames;
		// Arrays.copyOf();
	}

	public void rotate(double angle, boolean direction) {
		// if (_type == FRAME_TYPE_LINE || _type == FRAME_TYPE_ARC) // может быть
		// разводка
		for (int i = 0; i < _data.length; i++)
			_data[i].rotate(angle, direction);
	}

	public void reverseByX() {
		if (_type == FRAME_TYPE_LINE || _type == FRAME_TYPE_ARC)
			for (int i = 0; i < _data.length; i++)
				_data[i].reverseByX();
	}

	public void reverseByY() {
		if (_type == FRAME_TYPE_LINE || _type == FRAME_TYPE_ARC)
			for (int i = 0; i < _data.length; i++)
				_data[i].reverseByY();
	}

	public void scale(double k) {
		for (int i = 0; i < _data.length; i++) {
			CpSubFrame subFrame = _data[i];
			if (subFrame.isGeo() || subFrame.getType() == CpSubFrame.RC_G30_COMMAND)
				subFrame.scale(k);
		}
	}
}
