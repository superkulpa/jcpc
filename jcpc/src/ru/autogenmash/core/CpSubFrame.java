package ru.autogenmash.core;

import java.util.ArrayList;
import java.util.Arrays;

import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;

/**
 */
public class CpSubFrame implements Cloneable {
	/** Максимальное количество суппортов разводки. */
	public static final int G30_MAX_SUPP_COUNT = 4;

	public static final int RC_NULL = 0;
	public static final int RC_GEO_FAST = 20;
	public static final int RC_GEO_LINE = 21;
	public static final int RC_GEO_ARC = 22;
	public static final int RC_GEO_ROTATE = 41;
	public static final int RC_GEO_WAIT = 24;
	public static final int RC_FEED_COMMAND = 25;
	public static final int RC_M_COMMAND = 26;
	public static final int RC_D_COMMAND = 27;
	public static final int RC_G30_COMMAND = 28;
	public static final int RC_TERMINAL_COMMAND = 30;
	public static final int RC_LOOP_COMMAND = 31;
	public static final int RC_GEO_INFO = 103;
	// public static final int RC_FILTER = 100;
	// public static final int RC_GEO_PARK = 101;
	// public static final int RC_FEED_CORRECTION = 102;

	/** Например: RC_GEO_LINE, RC_FEED_COMMAND и тп. */
	private int _type;
	private CC[] _data;

	public CpSubFrame(int commandType, CC[] data) {
		_data = data;
		_type = commandType;
	}

	public Object clone() throws CloneNotSupportedException {
		CpSubFrame subFrame = (CpSubFrame) super.clone();
		CC[] ccs = new CC[getLength()];
		for (int i = 0; i < ccs.length; i++)
			ccs[i] = (CC) getCC(i).clone();

		subFrame.setData(ccs);
		subFrame.setType(_type);

		return subFrame;
	}

	public void copy(CpSubFrame subFrame) {
		setType(subFrame.getType());
		setData(subFrame.getData());
	}

	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof CpSubFrame == false)
			return false;

		CpSubFrame subFrame2 = (CpSubFrame) obj;
		if (getLength() != subFrame2.getLength())
			return false;

		if (_type != subFrame2.getType())
			return false;

		for (int i = 0; i < _data.length; i++) {
			if (_data[i].equals(subFrame2.getData()[i]) == false)
				return false;
		}

		return true;
	}

	public String toString() {
		return generateCpString(true);
	}

	public CC getCC(int position) {
		if (position >= getLength())
			return null;

		return _data[position];
	}

	public CC getCCByType(int type) {
		for (int i = 0; i < this.getLength(); i++) {
			if (_data[i].getType() == type)
				return _data[i];
		}
		return null;
	}

	public int getLength() {
		return _data.length;
	}

	public boolean hasCommand(int type) {
		for (int i = 0; i < this.getLength(); i++)
			if (_data[i].getType() == type)
				return true;

		return false;
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
		for (int j = 0; j < this.getLength(); j++) {
			final CC cc = this.getCC(j);
			if (cc == null)
				continue;

			if (cc.getType() == commandType && cc.getData() == value)
				return true;
		}
		return false;
	}

	public Integer getDataByType(int type) {
		for (int i = 0; i < _data.length; i++)
			if (_data[i].getType() == type)
				return new Integer(_data[i].getData());
		return null;
	}

	public int[] getDataArrayByType(int type) {
		ArrayList list = new ArrayList(5);

		for (int i = 0; i < this.getLength(); i++) {
			final CC cc = this.getCC(i);
			if (cc.getType() == type)
				list.add(new Integer(cc.getData()));
		}
		if (list.size() == 0)
			return null;
		int[] data = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			data[i] = ((Integer) list.get(i)).intValue();

		return data;
	}

	public String generateCcpString() {
		String dataStr = "";

		int x = 0, y = 0, i = 0, j = 0, z = 0, a = 0;
		int g = 0;

		switch (_type) {
		case RC_GEO_LINE:
		case RC_GEO_FAST:
			x = Utils.toInt(getDataByType(CC.X));
			y = Utils.toInt(getDataByType(CC.Y));
			dataStr += x + "\t" + y;
			break;
		case RC_GEO_ARC:
			x = Utils.toInt(getDataByType(CC.X));
			y = Utils.toInt(getDataByType(CC.Y));
			i = Utils.toInt(getDataByType(CC.I));
			j = Utils.toInt(getDataByType(CC.J));
			g = Utils.toInt(getDataByType(CC.G));
			dataStr += (g == 2 ? "1" : "0") + "\t" + x + "\t" + y + "\t" + i + "\t"
					+ j;
			break;
		case RC_GEO_ROTATE:	
			a = Utils.toInt(getDataByType(CC.A));
			dataStr += a;
		break;	
		case RC_FEED_COMMAND:
			dataStr += getDataByType(CC.F).intValue();
			break;
		case RC_M_COMMAND:
			String mString = generateMCommandCcpString();
			if (mString == null)
				return null;
			dataStr = mString;
			break;
		case RC_G30_COMMAND:
			int[] rData = getDataArrayByType(CC.R);
			for (int it = 0; it < rData.length; it++)
				dataStr += "\t" + rData[it];
			for (int it = 0; it < G30_MAX_SUPP_COUNT - rData.length; it++)
				dataStr += "\t" + 0;
			break;
		// case RC_FILTER:
		// for (int i = 0; i < _data.length; i++)
		// if (_data[i].getType() == CC.FILTER)
		// dataStr += _data[i].getDescription();
		// break;
		// case RC_GEO_PARK:
		// for (int i = 0; i < _data.length; i++)
		// if (_data[i].getType() == CC.PARK)
		// dataStr += _data[i].getDescription();
		// break;
		// case RC_FEED_CORRECTION:
		// dataStr += getDataByType(CC.FCORRECTION).intValue();
		// break;
		case RC_GEO_INFO:{
		  for (int k = 0; k < _data.length; k++)
		     if (_data[k].getType() == CC.G)
		       dataStr += _data[k].getDescription();
		  break;
		}
		case RC_NULL:
		case RC_D_COMMAND:
			return null;
		}

		return _type + ":\t" + dataStr;
	}

	private String generateMCommandCcpString() {
		String result = "";

		int mValue = getDataByType(CC.M).intValue();

		if (mValue == 2)
			return null;
		else if (mValue == 32 || mValue == 33) {
			_type = mValue;
			result = getCCByType(CC.M).getDescription();
			return result;
		} else if (mValue == 34)
			result = "102";
		else if (mValue == 0)
			result = "12";
		else if (mValue == 19)
			result = "15";
		else if (mValue == 45)
		  result = "21";
		else if (mValue == 46)
      result = "23";
		else if (mValue == 28)
      result = "28";
		else if (mValue == 29)
      result = "29";
		else if (mValue == 90)
      result = "90";
		else if (mValue == 76 || mValue == 77)
			result = String.valueOf(mValue + 20);
		else if (mValue >= 91 && mValue <= 96) {
		  if(mValue == 91) result = "80";//0x50-portal_on
		  else if(mValue == 92) result = "82";//0x52-forward_on
		  else if(mValue == 93) result = "84";//0x54-backward_on
		  else if(mValue == 94) result = "81";//0x53-portal_off
		  else if(mValue == 95) result = "83";//0x54-forward_off
		  else if(mValue == 96) result = "85";//0x55-backward_off
		}else{
			if (mValue == 71 || mValue == 81)
				result = "3";
			else if (mValue == 72 || mValue == 82 || mValue == 78)
				result = "4";
			else if (mValue == 73)
				result = "5";
			else if (mValue == 74 || mValue == 79 || mValue == 83 || mValue == 30)
				result = "6";
			else if (mValue == 75)
				result = "7";
			else if (mValue == 84)
				result = "100";
			else if (mValue == 700 || mValue == 70 || mValue == 80)
			  result = "17";
			else
			  result = "6";

		}
		
		int[] sub = getDataArrayByType(CC.SUB); 
    if (sub != null) {
      for (int it = 0; it < sub.length; it++)
        result += "\t" + sub[it];
    } else
      result += "\t0";
		
		int[] tData = getDataArrayByType(CC.T);
		if (tData != null) {
			for (int it = 0; it < tData.length; it++)
				result += "\t" + tData[it];
		} else
			result += "\t0";

		int[] z = getDataArrayByType(CC.Z); 
		if (z != null) {
      for (int it = 0; it < z.length; it++)
        result += "\t" + z[it];
    } else
      result += "\t0";
		
		int[] h = getDataArrayByType(CC.H); 
    if (h != null) {
      for (int it = 0; it < h.length; it++)
        result += "\t" + h[it];
    } else
      result += "\t0";
		return result;
	}

	public String generateCpString(boolean decompile) {
		String result = "";
		int rCount = 0;
		for (int i = 0; i < _data.length; i++) {
			CC cc = _data[i];
			try {
				String dataString = null;
				int type = cc.getType();
				if (type == CC.L  || type == CC.INFO/*
													 * || type == CC.R || type == CC.FILTER || type ==
													 * CC.USER || type == CC.PARK
													 */)
					dataString = cc.getDescription();
				else {
					int data = cc.getData();
					if (decompile)
						if (Arrays.binarySearch(CC.GEO_COMMANDS, type) >= 0 || type == CC.D) // TODO
																																									// refactored
							data = Math.round(data / 100);
					dataString = String.valueOf(data);
				}
				if (type == CC.R) {
					String suf = "+";
					if (cc.getData() < 0)
						suf = "-";
					result += CC.getCommandName(cc.getType()) + (++rCount) + suf
							+ dataString;
				} else
					result += CC.getCommandName(cc.getType()) + dataString;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean isGeo() {
		if (_type == RC_GEO_LINE || _type == RC_GEO_ARC || _type == RC_GEO_FAST ||
		// _type == RC_GEO_PARK ||
				_type == RC_GEO_WAIT) {
			return true;
		}

		return false;
	}

	public int getType() {
		return _type;
	}

	public void setType(int type) {
		_type = type;
	}

	public void setData(CC[] data) {
		_data = data;
	}

	public CC[] getData() {
		return _data;
	}

	public void setData(int position, CC cc) {
		_data[position] = cc;
	}

	public void setDataByType(int type, int data) {
		for (int i = 0; i < _data.length; i++)
			if (_data[i].getType() == type)
				_data[i].setData(data);
	}

	public double getEuclidLength() {
		if (_type == RC_GEO_LINE || _type == RC_GEO_FAST) {
			Point xy = getXY();
			return MathUtils.length(xy.x, xy.y);
		} else if (_type == RC_GEO_ARC) {
			int g = 0;
			int x = 0;
			int y = 0;
			int i = 0;
			int j = 0;
			for (int k = 0; k < _data.length; k++) {
				CC cc = _data[k];
				switch (cc.getType()) {
				case CC.X:
					x = cc.getData();
					break;
				case CC.Y:
					y = cc.getData();
					break;
				case CC.I:
					i = cc.getData();
					break;
				case CC.J:
					j = cc.getData();
					break;
				case CC.G:
					g = cc.getData();
					break;
				}
			}

			return MathUtils.calculateArcLength(x, y, i, j, g == 2);
		}

		return -1;
	}

	public Point getXY() {
		int x = 0;
		int y = 0;

		for (int l = 0; l < _data.length; l++) {
			if (_data[l].getType() == CC.X)
				x = _data[l].getData();
			else if (_data[l].getType() == CC.Y)
				y = _data[l].getData();
		}

		//if (x != 0 || y != 0)
		return new Point(x, y);

		//return null;
	}

	public void addCommand(CC cc) {
		CC[] data = new CC[getLength() + 1];
		for (int i = 0; i < getLength(); i++)
			data[i] = _data[i];
		data[getLength()] = cc;
		_data = data;
	}

	public void rotate(double angle, boolean direction) {
		Integer x = null;
		Integer y = null;
		Integer i = null;
		Integer j = null;

		if (_type == RC_GEO_LINE || _type == RC_GEO_FAST) {
			x = getDataByType(CC.X);
			y = getDataByType(CC.Y);
			Point point = MathUtils.rotateCoords(angle,
					(x == null ? 0 : x.intValue()), (y == null ? 0 : y.intValue()),
					direction);

			this.copy(MTRUtils.createLineSubFrame(_data[0].getData(), point.x,
					point.y));
		} else if (_type == RC_GEO_ARC) {
			x = getDataByType(CC.X);
			y = getDataByType(CC.Y);
			i = getDataByType(CC.I);
			j = getDataByType(CC.J);
			Point pointXY = MathUtils.rotateCoords(angle, (x == null ? 0 : x
					.intValue()), (y == null ? 0 : y.intValue()), direction);
			Point pointIJ = MathUtils.rotateCoords(angle, (i == null ? 0 : i
					.intValue()), (j == null ? 0 : j.intValue()), direction);

			this.copy(MTRUtils.createArcSubFrame(_data[0].getData(), pointXY.x,
					pointXY.y, pointIJ.x, pointIJ.y));
		} else if (_type == RC_G30_COMMAND) {
			// разводка
			CC[] data = getData();
			for (int k = 0; k < data.length; k++) {
				CC command = data[k];
				if (command.getType() != CC.R)
					continue;
				double r = command.getData();
				command.setData((int) Math.round(r / Math.cos(angle)));
			}
		}
	}

	public void reverseByX() {
		reverse(CC.X, CC.I);
	}

	public void reverseByY() {
		reverse(CC.Y, CC.J);
	}

	private void reverse(int axis1Type, int axis2Type) {
		if (_type == RC_GEO_LINE || _type == RC_GEO_FAST) {
			invertAxisValue(axis1Type);
		} else if (_type == RC_GEO_ARC) {
			invertAxisValue(axis1Type);

			invertAxisValue(axis2Type);

			CC ccG = getCCByType(CC.G);
			if (ccG != null) {
				int g = ccG.getData();
				g = 2 + (g + 1) % 2;
				ccG.setData(g);
			}
		}
	}

	private void invertAxisValue(int axisType) {
		CC cc = getCCByType(axisType);
		if (cc != null)
			cc.invert();
	}

	public void scale(double k) {
		for (int i = 0; i < _data.length; i++)
			_data[i].scale(k);
	}
}
