package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.List;

import org.apache.commons.logging.Log;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.StringUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

/**
 * Формат маркировки: M33 [C<угол>] {L<текст> | L'<текст>'}
 */
public class MarkiningAction extends StepActionBase {
	public static final String AUX_DATA_MARKING = "Marking";

	private Point _supportDrift = new Point();

	private boolean _isM33Open = false;

	public MarkiningAction(Compiler compiler, List warnings) {
		super(compiler, warnings);
	}

	public CompilerError execute(CPList cpList, CpParameters cpParameters) {
		String warn = "Warning: " + this.getClass().getName()
				+ " has some problems (for detail see logs)";
		try {
			Boolean hasMarkining = (Boolean) cpList.getAuxData(AUX_DATA_MARKING);
			if (hasMarkining != null && hasMarkining.booleanValue() == false)
				return null;

			_supportDrift = getMarkerSupportDrift(cpParameters, _log);
		} catch (Throwable t) {
			String err = "Ошибка при получении вспомогательной информации";
			_log.error(err, t);
			System.err.println(warn);
			return new CompilerError(err);
		}

		correctSupportDrift(cpParameters);

		try {
			CompilerError res = processMarkining(cpList);
			if (res != null)
				return res;
		} catch (Throwable t) {
			String err = "Ошибка при обработке маркировки";
			_log.error(err, t);
			System.err.println(warn);
			return new CompilerError(err);
		}

		return null;
	}

	private void correctSupportDrift(CpParameters cpParameters) {
		if (_supportDrift.isZero())
			return;

		int scale = Integer.parseInt((String) cpParameters
				.getValue(Compiler.PARAM_RSI_SCALE));
		double angle = Double.parseDouble((String) cpParameters
				.getValue(Compiler.PARAM_RSI_ROTATION_ANGLE));
		boolean inverse = (new Boolean((String) cpParameters
				.getValue(Compiler.PARAM_RSI_INVERSE))).booleanValue();

		// scale = scale / Compiler.SIZE_TRANSFORMATION_RATIO;
		// angle = angle / Compiler.SIZE_TRANSFORMATION_RATIO / 10;

		Point supportDriftTmp = new Point(_supportDrift.x, _supportDrift.y);
		if (angle != 0)
			_supportDrift.rotate(Math.toRadians(angle / 10), true);
		if (scale != 100)
			_supportDrift.scale(scale);
		if (inverse == true)
			_supportDrift.y = -_supportDrift.y;

		if (_supportDrift.equals(supportDriftTmp) == false) {
			_log.info("Параметр \"" + Compiler.PARAM_MARKING_SUPPORT_DRIFT
					+ "\" был изменен с: " + supportDriftTmp + " на: " + _supportDrift);
		}
	}

	public static Point getMarkerSupportDrift(CpParameters cpParameters, Log log) {
		String supportDriftString = (String) cpParameters
				.getValue(Compiler.PARAM_MARKING_SUPPORT_DRIFT);
		if (supportDriftString == null)
			log
					.warn("Смещение суппорта маркировки не определено. Установлено по умолчанию (0, 0)");
		else {
			String[] drifts = StringUtils.split(supportDriftString.trim(), ",");
			String warnInvalidDrift = "Смещение суппорта маркировки определено некорректно. Установлено по умолчанию (0, 0)";
			if (drifts.length != 2)
				log.warn(warnInvalidDrift);
			else {
				try {
					return new Point(Integer.parseInt(drifts[0].trim())
							* Compiler.SIZE_TRANSFORMATION_RATIO, Integer.parseInt(drifts[1]
							.trim())
							* Compiler.SIZE_TRANSFORMATION_RATIO);
				} catch (NumberFormatException e) {
					log.warn(warnInvalidDrift);
				}
			}
		}

		return new Point();
	}

	private CompilerError processMarkining(CPList cpList)
			throws CloneNotSupportedException {
		for (int i = 0; i < cpList.getLength(); i++) {
			CachedCpFrame frame = cpList.getFrame(i);
			if (frame.hasM() && frame.contains(CC.M, 33)) {
				CC ccM = frame.getCCByType(CC.M);
				if (ccM.getDescription().length() < 2)
					return new CompilerError(i + 1,
							"некорректно задана маркировка, отсутствует L команда");
				// CC ccL = frame.getCCByType(CC.L);
				// if (ccL == null)
				// return new CompilerError(i + 1,
				// "некорректно задана маркировка, отсутствует L команда");

				String lData = ccM.getDescription().trim();
				if (lData.equals(""))
					return new CompilerError(i + 1,
							"некорректно задана маркировка, отсутствует текст у L команда");

				int cData = 0; // угол поворота маркировки, в десятых долях градуса (как
												// обычно)
				CC ccC = frame.getCCByType(CC.C);
				if (ccC != null)
					cData = ccC.getData();

				// CC ccM = frame.getCCByType(CC.M);

				int firstQuoter = lData.indexOf('\'');
				if (firstQuoter < 0)
					ccM.setData(32);
				else if (firstQuoter > 0) {
					return new CompilerError(i + 1, "некорректно задана маркировка, "
							+ "значение команды L: " + lData);
				} else {
					int secondQuoter = lData.indexOf('\'', firstQuoter + 1);
					if (secondQuoter < 0)
						return new CompilerError(i + 1, "некорректно задана маркировка, "
								+ "значение команды L не содержит закрывающнго апострофа: "
								+ lData);

					lData = lData.replace('\'', ' ').trim();
				}

				ccM.setDescription(cData + " " + lData);

				frame.setData(new CpSubFrame[] { new CpSubFrame(
						CpSubFrame.RC_M_COMMAND, new CC[] { ccM }) });

				CompilerError error = correctMarkingLeadIn(cpList, i, true);
				if (error != null)
					return error;
				_isM33Open = true;
			} else if (frame.hasM() && frame.contains(CC.M, 34)) {
				CompilerError error = correctMarkingLeadIn(cpList, i, false);
				if (error != null)
					return error;
				_isM33Open = false;
			}
		}

		return null;
	}

	private CompilerError correctMarkingLeadIn(CPList cpList, int i,
			boolean direction) {
		if (direction == false)
			_supportDrift.inverse();
		else if (_isM33Open)
			return null;

		String message = "Перед командой маркировки отсутствует холостое перемещение";

		if (i == 0)
			_log.warn(message);
		else {
			if (direction)
				i--;
			else
				i++;

			CachedCpFrame g00Frame = cpList.getFrame(i);
			if (g00Frame == null)
				return null;
			
			int g0x = -1;
			if ((g00Frame.hasG00() == true))
				g0x = CpSubFrame.RC_GEO_FAST;
			else
			if ((g00Frame.hasG01() == true))
				g0x = CpSubFrame.RC_GEO_LINE;
			
				
			if (g0x == -1)
				//перед дугами не смещаем
				_log.warn(message);
			else 
			{
				
				CpSubFrame subFrame = new CpSubFrame(g0x, new CC[] {});
				int pos = g00Frame.getSubFrameByType(g0x, subFrame);
				int x = Utils.toInt(subFrame.getDataByType(CC.X));
				int y = Utils.toInt(subFrame.getDataByType(CC.Y));
				x += _supportDrift.x;
				y += _supportDrift.y;
				g00Frame.setCpSubFrame(pos, MTRUtils.createLineSubFrame(0, x, y));
			}
		}

		return null;
	}

	public String getDescription() {
		return "Обработка маркировки";
	}

}
