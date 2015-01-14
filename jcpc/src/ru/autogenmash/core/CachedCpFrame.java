package ru.autogenmash.core;

import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.Utils;

/**
 */
public class CachedCpFrame extends CpFrame implements Cloneable {
	private boolean _hasX = false;
	private boolean _hasY = false;
	private boolean _hasI = false;
	private boolean _hasJ = false;
	private boolean _hasZ = false;
	private boolean _hasA = false;
	private boolean _hasM = false;
	private boolean _hasG00 = false;
	private boolean _hasG01 = false;
	private boolean _hasG02 = false;
	private boolean _hasG03 = false;

	public CachedCpFrame(int type, CpSubFrame[] data) {
		super(type, data);
	}

	public Object clone() throws CloneNotSupportedException {
		CachedCpFrame frame = (CachedCpFrame) super.clone();

		frame.setHasG00(_hasG00);
		frame.setHasG01(_hasG01);
		frame.setHasG02(_hasG02);
		frame.setHasG03(_hasG03);

		frame.setHasX(_hasX);
		frame.setHasY(_hasY);
		frame.setHasI(_hasI);
		frame.setHasJ(_hasJ);
		frame.setHasZ(_hasZ);
		frame.setHasA(_hasA);
		frame.setHasM(_hasM);

		return frame;
	}

	public boolean equals(Object obj) {
		return super.equals((CpFrame) obj);
	}

	public void copy(CachedCpFrame frame) {
		setData(frame.getData());
		setType(frame.getType());

		_hasG00 = frame.hasG00();
		_hasG01 = frame.hasG01();
		_hasG02 = frame.hasG02();
		_hasG03 = frame.hasG03();

		_hasX = frame.hasX();
		_hasY = frame.hasY();
		_hasI = frame.hasI();
		_hasJ = frame.hasJ();
		_hasZ = frame.hasZ();
		_hasA = frame.hasA();
		_hasM = frame.hasM();
	}

	public boolean hasG00() {
		return _hasG00;
	}

	public void setHasG00(boolean hasG00) {
		_hasG00 = hasG00;
	}

	public boolean hasG01() {
		return _hasG01;
	}

	public void setHasG01(boolean hasG01) {
		_hasG01 = hasG01;
	}

	public boolean hasG02() {
		return _hasG02;
	}

	public void setHasG02(boolean hasG02) {
		_hasG02 = hasG02;
	}

	public boolean hasG03() {
		return _hasG03;
	}

	public void setHasG03(boolean hasG03) {
		_hasG03 = hasG03;
	}

	public boolean hasI() {
		return _hasI;
	}

	public void setHasI(boolean hasI) {
		_hasI = hasI;
	}

	public boolean hasZ() {
    return _hasZ;
  }
	
	public void setHasZ(boolean hasZ) {
    _hasZ = hasZ;
  }
	
	public boolean hasA() {
    return _hasA;
  }
	
	public void setHasA(boolean hasA) {
    _hasA = hasA;
  }
	
	public boolean hasJ() {
		return _hasJ;
	}

	public void setHasJ(boolean hasJ) {
		_hasJ = hasJ;
	}

	public boolean hasM() {
		return _hasM;
	}

	public void setHasM(boolean hasM) {
		_hasM = hasM;
	}

	public boolean hasX() {
		return _hasX;
	}

	public void setHasX(boolean hasX) {
		_hasX = hasX;
	}

	public boolean hasY() {
		return _hasY;
	}

	public void setHasY(boolean hasY) {
		_hasY = hasY;
	}

	public void rotate(double angle, boolean direction) {
		if (getType() == FRAME_TYPE_LINE) {
			super.rotate(angle, direction);
			_hasX = getCCByType(CC.X) != null;
			_hasY = getCCByType(CC.Y) != null;
		} else if (getType() == FRAME_TYPE_ARC) {
			super.rotate(angle, direction);
			_hasX = getCCByType(CC.X) != null;
			_hasY = getCCByType(CC.Y) != null;
			_hasI = getCCByType(CC.I) != null;
			_hasJ = getCCByType(CC.J) != null;
		} else if (getType() == FRAME_TYPE_ROTATE) {
			super.rotate(angle, direction);
			_hasX = getCCByType(CC.X) != null;
			_hasY = getCCByType(CC.Y) != null;
			_hasA = getCCByType(CC.A) != null;	
		}else if (getSubFrameByType(CpSubFrame.RC_G30_COMMAND) != null)
			super.rotate(angle, direction);
	}

	public void removeGeoSubFrame() {
		super.removeGeoSubFrame();
		setHasG00(false);
		setHasG01(false);
		setHasG02(false);
		setHasG03(false);
		setHasX(false);
		setHasY(false);
		setHasI(false);
		setHasJ(false);
		setHasA(false);
	}

	private void checkReversment() {
		if (getType() == FRAME_TYPE_ARC) {
			_hasG02 = !_hasG02;
			_hasG03 = !_hasG03;
		}
	}

	public void reverseByX() {
		checkReversment();
		super.reverseByX();
	}

	public void reverseByY() {
		checkReversment();
		super.reverseByY();
	}

	public void reverse() {
		if (getType() == CpFrame.FRAME_TYPE_LINE)
			reverseLine();
		else if (getType() == CpFrame.FRAME_TYPE_ARC)
			reverseArc();
	}

	private void reverseArc() {
		CpSubFrame arcSubFrame = getSubFrameByType(CpSubFrame.RC_GEO_ARC);

		int newG = 2;
		if (_hasG02)
			newG = 3;
		int x = Utils.toInt(getDataByType(CC.X));
		int y = Utils.toInt(getDataByType(CC.Y));
		int i = Utils.toInt(getDataByType(CC.I));
		int j = Utils.toInt(getDataByType(CC.J));

		int newX = -x;
		int newY = -y;
		int newI = i - x;
		int newJ = j - y;
		arcSubFrame.setData(MTRUtils
				.createArcSubFrame(newG, newX, newY, newI, newJ).getData());

		setHasG02(newG == 2);
		setHasG03(newG == 3);

		setHasX(newX != 0);
		setHasY(newY != 0);
		setHasI(newI != 0);
		setHasJ(newJ != 0);
	}

	private void reverseLine() {
		if (hasX()) {
			CC ccx = getCCByType(CC.X);
			ccx.setData(-ccx.getData());
		}
		if (hasY()) {
			CC ccy = getCCByType(CC.Y);
			ccy.setData(-ccy.getData());
		}
	}

}
