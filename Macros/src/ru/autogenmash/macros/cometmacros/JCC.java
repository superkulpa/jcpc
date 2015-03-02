package ru.autogenmash.macros.cometmacros;

// class - ControlComand
// класс, реализующий одну управляющую команду (любую)
public class JCC {

	public static final int Line			= 1; // тип уп команды - Линия
	public static final int Arc		      	= 2; // тип уп команды - Дуга
	public static final int Fast			= 3; // тип уп команды - Ускоренно
	public static final int D				= 4; // тип уп команды - Эквидистанта
	public static final int M				= 5; // тип уп команды - М-команда
	public static final int R				= 6; // тип уп команды - Угол наклона резки
	public static final int StartPoint		= 7; // точка первоначальной пробивки
	public static final int G				= 8; // тип уп команды - Подготовительная функция

	protected int type;
	protected int[] data;
	protected String description;

	public JCC(int _type, int[] _data, String _description) {
		// конструктор
		// для каждого типа

		switch (_type) {
		case Line:
			data = new int[2];
			// x, y
			break;
		case Arc:
			data = new int[5];
			// x, y, i, j, direction = {1, -1} -1 - по часовой(true)
			break;
		case Fast:
			data = new int[1];
			// f
			break;
		case D:
			data = new int[1];
			// d
			break;
		case M:
			data = new int[1];
			// номер команды
			break;
		case R:
			data = new int[1];
			// угол наклона
			break;
		case StartPoint:
			data = new int[1];
			// номер точки пробивки
			break;
		case G:
			data = new int[1];
			// подготовительная функция
			break;
		default:
			try {
				throw new Exception("JCC with _type = " + _type
						+ " not supported!");
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		type = _type;
		data = _data;
		description = _description;
	}

	public int[] GetData() {
		//
		return data;
	}

	public String GetDescription() {
		//
		return description;
	}

	public boolean CheckFor0Movement() {
		// проверить является ли CC нулевым перемещением или нет
		if (type == JCC.Line) {
			if ((data[0] == 0) && (data[1] == 0))
				return true;
		}
		if (type == JCC.Arc) {
			if ((data[0] == 0) && (data[1] == 0) && (data[2] == 0)
					&& (data[3] == 0))
				return true;
		}
		return false;
	}

	public String GenerateCPString() {
		// преобразование управляющей команды в строку

		String res = "";

		switch (type) {
		case Line:
			// x, y
			if (data[0] != 0)
				res += "X" + data[0];
			if (data[1] != 0)
				res += "Y" + data[1];
			if ((data[0] == 0) && (data[1] == 0))
				break;
			res += "\n";
			break;
		case Arc:
			// x, y, i, j, direction = {1, -1} 1 - по часовой
			if (data[4] == -1)
				res += "G02";
			else
				res += "G03";
			if (data[0] != 0)
				res += "X" + data[0];
			if (data[1] != 0)
				res += "Y" + data[1];
			if (data[2] != 0)
				res += "I" + data[2];
			if (data[3] != 0)
				res += "J" + data[3];
			if ((data[0] == 0) && (data[1] == 0) && (data[2] == 0)
					&& (data[3] == 0))
				break;
			res += "\n";
			break;
		case Fast:
			// f
			if (data[0] != 0)
				res += "F" + data[0];
			else
				break;
			res += "\n";
			break;
		case D:
			res += "D" + data[0] + "\n";
			break;
		case M:
			// номер команды
            res += "M";
            if (data[0] < 10)
                res += "0";
            res += data[0] + "\n";
			break;
		case R:
			// угол наклона
			if (data[0] != 0)
				res += "С" + data[0];
			else
				break;
			res += "\n";
			break;
		case G:
            res += "G";
			if (data[0] >= 0 && data[0] < 10)
				res += "0";
        res += data[0];
        if (data[0] == 40 || data[0] == 41 || data[0] == 42)
            res += "\n";
			break;
		default:
			break;
		}

		return res;
	}
}
