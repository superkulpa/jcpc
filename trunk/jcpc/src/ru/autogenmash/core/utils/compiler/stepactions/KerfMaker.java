package ru.autogenmash.core.utils.compiler.stepactions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.Point;
import ru.autogenmash.core.Shape;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.MathUtils;
import ru.autogenmash.core.utils.Utils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;

public class KerfMaker extends StepActionBase {

  private static final int ANGLE_INSIDE = 0;
  private boolean _approximateLeadsByLines;
  private ArrayList _shapes;
  private CachedCpFrame[] _exitFrames;
  private double _kerfMinAuxArcLength;
  
  private String errStr;
  
  public KerfMaker(Compiler compiler, List warnings) {
    super(compiler, warnings);
  }

  /**Рассчитать угол сопряжения 2-х GEO команд, зная соответствующие нормали
   * (в градусах)
   * @param vector1
   * @param vector2
   * @return
   */
  public static double calculateKerfCrossAngle(Point vector1, Point vector2)
  {
      double vector2Angle = MathUtils.calculateAngle2(vector2.x, vector2.y);

      Point newVector1 = MathUtils.rotateCoords(vector2Angle, vector1.x, vector1.y, false);

      double crossAngle = Math.toDegrees(MathUtils.calculateAngle2(newVector1.x, newVector1.y));

      return 180 - crossAngle;
  }
  
  public CompilerError execute(CPList cpList, CpParameters cpParameters) {
//    _approximateLeadsByLines = (new Boolean((String)cpParameters.getValue(Compiler.PARAM_KERFING_APPROXIMATE_LEADS_BY_LINES))).booleanValue();
//    
//    _kerfMinAuxArcLength = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_KERFING_MIN_AUX_ARC_LENGTH));
//    _kerfMinAuxArcLength *= Compiler.SIZE_TRANSFORMATION_RATIO;

    CompilerError result = null;
    
    result = CalcCPShapes(cpList, cpParameters);

//    if (result != null)
//        return result;

    //markEqualShapes();

//    result = processShapes(cpList, cpParameters);
//    if (result != null)
//        return result;

    return result;
  }
  //выбираем контура из УП
  private CompilerError CalcCPShapes(CPList cpList, CpParameters cpParameters) {
    //команды контура
    ArrayList shapeFrames = new ArrayList();
    CachedCpFrame[] newFrames = null;
    //позициия для обсчета контура подхода и отхода
//    Point absPosition = new Point(0, 0);
    Point absStartPosition = new Point();
    //используется для слздания входа
    Point correctIn = new Point();
    boolean ekvSwitchOnShape = false;
//    Point absEndPosition = new Point(0, 0);
    CachedCpFrame in_frame = null;
    CachedCpFrame out_frame = null;
//    CachedCpFrame out_frame;
    int curEkv = 0;
    //
    int d = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_KERFING_D)); 
    int curD = d;
    CompilerError ekvErr = new CompilerError(0, "");
    //идем по всей УП
    for (int i = 0; i < cpList.getLength(); i++)
    {
      //тек команда
      CachedCpFrame cur_frame = cpList.getFrame(i);
      Integer dValue = cur_frame.getDataByType(CC.D);

      if (dValue != null)  d = dValue.intValue();
  
      int[] gData = cur_frame.getDataArrayByType(CC.G);
      Arrays.sort(gData);
      boolean g41 = Arrays.binarySearch(gData, 41) >= 0;
      boolean g42 = Arrays.binarySearch(gData, 42) >= 0;
      boolean g40 = Arrays.binarySearch(gData, 40) >= 0;
      
      //проверяем на правильность составления 
      if ((g41 && g42) ||
          (g41 && g40) ||
          (g42 && g40) ||
         ((curEkv > 0) && (g41 || g42)))
        return new CompilerError(i + 1, "слишком много команд эквидистанты");
      
      if(g41 || g42)//начало контура
      {
        //сбрасываем координаты
        absStartPosition.x = 0;
        absStartPosition.y = 0;
        ekvErr.setFrameNumber(i);//кадр начала екв контура
        if(g41) { 
          curEkv = 41;
          curD = d;
        }else {
          curEkv = 42;
          curD = -d;
        };
        if(i == 0) 
          ekvSwitchOnShape = true;
        else {
          //запоминаем кадр выхода на эквидистанту
          in_frame = cpList.getFrame(i - 1);
          int j = i - 1;
          while((j > 0)&&(!in_frame.isGeo())) {
            in_frame = cpList.getFrame(--j);
          }
          if(!in_frame.isGeo()) {
            ekvSwitchOnShape = true;
            in_frame = null;
          }
        };
      };
      //по выкл и концу программы
      if(g40) {
        if(curEkv > 0){ 
          curEkv = 0;
          if(!cur_frame.isGeo()) {
            //берем след геометрию
            int j = i;
            out_frame = cur_frame;
            while((j < cpList.getLength() - 1) && (!out_frame.isGeo())) {
              out_frame = cpList.getFrame(++j);
            }
            if(!out_frame.isGeo()) out_frame = null;
          }else
            out_frame = cur_frame;
          correctIn = BuileEkvShape(shapeFrames, curD, in_frame, out_frame, absStartPosition.length() == 0, ekvErr);
          if((ekvSwitchOnShape == true) && (correctIn.length() != 0)){
            ekvSwitchOnShape = false;
            //достраиваем вход
            newFrames = new CachedCpFrame[cpList.getLength() + 1];
            newFrames[0] = MTRUtils.createLineFrame(1, correctIn.x, correctIn.y);
            int j = 0;
            for(j = 1; j <= i; j++)
              newFrames[j] = (CachedCpFrame)cpList.getFrame(j - 1);
            //добавляем все остальные из _cpList
            for(int k = i; k < cpList.getLength(); k++)
              newFrames[j + k - i] = (CachedCpFrame)cpList.getFrame(k);
            //меняем лист и смещаемся на добавленный кадр
            cpList.setData(newFrames);
            i++;
            cur_frame = cpList.getFrame(i);
          };
        };
        //очищаем для новых контуров
        shapeFrames.clear();
      }
      if ((cur_frame.isGeo()))
      {    
        if(curEkv > 0) {
          if (cur_frame.hasX())
            absStartPosition.x += cur_frame.getDataByType(CC.X).intValue();
          if (cur_frame.hasY())
            absStartPosition.y += cur_frame.getDataByType(CC.Y).intValue();
          //сохраняем в массиве контура
          shapeFrames.add(cur_frame);
        }else{
        	if(correctIn != null) {
            if(correctIn.length() != 0) {
              correctIn.x = 0;
              correctIn.y = 0;
              shapeFrames.clear();
            };
        	}else {
        		return ekvErr;//new CompilerError(nLine, errStr);
        	}
        };
      }
    //это послежний кадр и не выкл эквидистанта
      if(i == cpList.getLength() - 1){
        if(curEkv > 0){ 
          curEkv = 0;
          correctIn = BuileEkvShape(shapeFrames, curD, in_frame,null, absStartPosition.length() == 0, ekvErr);
          if((ekvSwitchOnShape == true) && (correctIn.length() != 0)) {
            ekvSwitchOnShape = false;
            //достраиваем вход
            newFrames = new CachedCpFrame[cpList.getLength() + 1];
            newFrames[0] = MTRUtils.createLineFrame(1, correctIn.x, correctIn.y);
            int j = 0;
            for(j = 1; j <= i; j++)
              newFrames[j] = (CachedCpFrame)cpList.getFrame(j - 1);
            //добавляем все остальные из _cpList
            for(int k = i; k < cpList.getLength(); k++)
              newFrames[j + k - i] = (CachedCpFrame)cpList.getFrame(k);
            
            //меняем лист и смещаемся на добавленный кадр
            cpList.setData(newFrames);
            i++;
          }else if(correctIn == null){
        	  return ekvErr;//new CompilerError(nLine, errStr); 
          };
        };
      }
    };
    return null;
  }

  private Point BuileEkvShape(ArrayList _shapeFrames, int _d,
      CachedCpFrame _inFrame, CachedCpFrame _outFrame, boolean _close, CompilerError _ekvErr) {
    if(_d == 0) return new Point(0,0);
    //нет кадров
    if(_shapeFrames.size() == 0)  return new Point(0,0);
    CachedCpFrame cur_frame = (CachedCpFrame)_shapeFrames.get(0);
    CachedCpFrame next_frame = null;
    Point correctIn = new Point();
    Point correctOut = new Point();
    
    Point crossPoint = new Point();
    Point crossPoint1 = new Point();
    int countCorr = 0;
    for(int i = 1; i < _shapeFrames.size(); i++) {
      next_frame = (CachedCpFrame)_shapeFrames.get(i);
      if(next_frame.getXY() == null) continue;
      //проверка на петли
      if((MathUtils.length(cur_frame.getXY().x, cur_frame.getXY().y) < _d) && ((cur_frame.getType() != CpFrame.FRAME_TYPE_ARC) && (next_frame.getType() != CpFrame.FRAME_TYPE_ARC))) {
        if(i == 1) {
          //проверяем что угол внутренний
          if(MathUtils.GetTypeAngleBetweenFrame(cur_frame, next_frame, _d) == ANGLE_INSIDE) {
           cur_frame = next_frame;
           countCorr ++;
           continue;
          }
        }
      };
      
      if((MathUtils.length(next_frame.getXY().x, next_frame.getXY().y) < _d) && ((cur_frame.getType() != CpFrame.FRAME_TYPE_ARC) && (next_frame.getType() != CpFrame.FRAME_TYPE_ARC))){
        //проверяем что углы подряд идущих внутренние
        if((MathUtils.GetTypeAngleBetweenFrame(cur_frame, next_frame, _d) == ANGLE_INSIDE) 
        &&(MathUtils.GetTypeAngleBetweenFrame(cur_frame, (CachedCpFrame)_shapeFrames.get(i), _d) == ANGLE_INSIDE)){
        	if((i + 1) == _shapeFrames.size()){
        		//просто вырождаем
        		cur_frame = next_frame;
            countCorr ++;
            continue;
        	}
          //ищем точку пересечения между первым и третьим
          Point tmpPoint = MathUtils.getCrossPoint(cur_frame.getXY(),next_frame.getXY(),((CachedCpFrame)_shapeFrames.get(i + 1)).getXY());
          if(tmpPoint == null){
          	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
          	_ekvErr.setMessage("Невозможно построить эквидистантный контур, слишком большая эквидистанта");
        	  return null;
          }
          //и меняем их
          Point correct = new Point();
          correct.x = tmpPoint.x - cur_frame.getXY().x;
          correct.y = tmpPoint.y - cur_frame.getXY().y;
          correct.x = next_frame.getXY().x - correct.x + ((CachedCpFrame)_shapeFrames.get(i + 1)).getXY().x;
          correct.y = next_frame.getXY().y - correct.y + ((CachedCpFrame)_shapeFrames.get(i + 1)).getXY().y;
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
          ccs.add(new CC(CC.X,tmpPoint.x));
          ccs.add(new CC(CC.Y,tmpPoint.y));
          CpSubFrame sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));

          ccs.clear();
          sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          if(sub_frame == null){
          	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
          	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше длины перемещения");
        	  return null;
          }
          ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
          ccs.add(new CC(CC.X,0));
          ccs.add(new CC(CC.Y,0));
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
          
          ccs.clear();
          sub_frame = ((CachedCpFrame)_shapeFrames.get(i + 1)).getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if(sub_frame == null) sub_frame = ((CachedCpFrame)_shapeFrames.get(i + 1)).getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          if(sub_frame == null){
          	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
          	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше длины перемещения");
        	  return null;
          }
          ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
          ccs.add(new CC(CC.X,correct.x));
          ccs.add(new CC(CC.Y,correct.y));
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
          countCorr++;
          continue;
        }
      };
      switch(cur_frame.getType()) {
        case CpFrame.FRAME_TYPE_LINE:
          switch(next_frame.getType()) {
            case CpFrame.FRAME_TYPE_LINE:{
            	if(cur_frame.getXY().length() < 3){
            		_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
              	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше длины перемещения");
            		return null;
            	}
              double a_grad = Math.abs(MathUtils.calculateCrossAngleRad(cur_frame.getXY(), next_frame.getXY()));
              double corr1 = 0; double corr2 = 0;              
              
              if( (i - countCorr) == 1) {
                //длина
                Point new_end_point1 = MathUtils.GetNormalDot(cur_frame.getXY(), _d);
                //коррекция входа
                correctIn.x = new_end_point1.x - cur_frame.getXY().x;
                correctIn.y = new_end_point1.y - cur_frame.getXY().y;
              };

              //коррекция выхода
              if(i == _shapeFrames.size() - 1) {
                //длина
                Point new_end_point1 = MathUtils.GetNormalDot(next_frame.getXY(), _d);
                correctOut.x = new_end_point1.x - next_frame.getXY().x;
                correctOut.y = new_end_point1.y - next_frame.getXY().y;
              };
             
              if(a_grad == 0) {
              	corr1 = 1;
              	corr2 = 1;
              	//вставляем окружность между ними
              	
              }else {
              	corr1 = (Math.abs(_d) * Math.tan((Math.PI - a_grad) / 2)) / cur_frame.getXY().length();
              	corr2 = (Math.abs(_d) * Math.tan((Math.PI - a_grad) / 2)) / next_frame.getXY().length();
              };
              
              if(MathUtils.GetTypeAngleBetweenFrame(cur_frame, next_frame, _d) == ANGLE_INSIDE) {
                //внутренний угол
              	int tmp = cur_frame.getXY().x;
                crossPoint.x = (int) (tmp * (1 - corr1));
                crossPoint.y = (int) (cur_frame.getXY().y * (1 - corr1));
                crossPoint1.x = (int) (next_frame.getXY().x * (1 - corr2));
                crossPoint1.y = (int) (next_frame.getXY().y * (1 - corr2));
              }else {
                crossPoint.x = (int) (cur_frame.getXY().x * (1 + corr1));
                crossPoint.y = (int) (cur_frame.getXY().y * (1 + corr1));
                crossPoint1.x = (int) (next_frame.getXY().x * (1 + corr2));
                crossPoint1.y = (int) (next_frame.getXY().y * (1 + corr2));
              }
              Vector ccs = new Vector(3);
              CpSubFrame sub_frame = null;
              ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              ccs.clear();
              
              ccs.add(new CC(CC.G, next_frame.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint1.x));
              ccs.add(new CC(CC.Y,crossPoint1.y));
              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null) sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
            break;
            }
            case CpFrame.FRAME_TYPE_ARC:{
              //длина
            	if(cur_frame.getXY().length() < 3){
            		_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
              	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше длины перемещения");
            		return null;
            	}
              Point new_end_point = MathUtils.GetNormalDot(cur_frame.getXY(), _d);

              if( (i - countCorr) == 1) {
                correctIn.x = new_end_point.x - cur_frame.getXY().x;
                correctIn.y = new_end_point.y - cur_frame.getXY().y;
              };
              
              int centerX = Utils.toInt(next_frame.getDataByType(CC.I));
              int centerY = Utils.toInt(next_frame.getDataByType(CC.J));
              
              int endX = Utils.toInt(next_frame.getDataByType(CC.X)) - centerX;
              int endY = Utils.toInt(next_frame.getDataByType(CC.Y)) - centerY;
              
              Point centerPoint = new Point(centerX, centerY);
              Point endPoint = new Point(endX, endY);
//              Point centerPoint = MathUtils.CalcCrossLineArc(_d, cur_frame.getXY(), next_frame, true);
              
//              //TODO сделать соединение отрезком или окружностью 
//              if(centerPoint == null) return null;
//              //корректируем конечную точку
//              double corr = centerPoint.length() / MathUtils.length(centerX, centerY);
//              endPoint.x *= corr; 
//              endPoint.y *= corr;
//              
//              //коррекция выхода
//              if(i == _shapeFrames.size() - 1) {
//                correctOut.x = endPoint.x - endX;
//                correctOut.y = endPoint.y - endY;
//              };
//              
//              double corr1 = 1;
//              Point corrLine = new Point();
//              corrLine.x = centerPoint.x + centerX;
//              corrLine.y = centerPoint.y + centerY;
//              double corrAngle = Math.abs(MathUtils.calculateCrossAngleRad(cur_frame.getXY(), corrLine));
//              if((corrAngle == 0) || (corrAngle == Math.PI)) {//нет тангенса
//                corr1 = 1 - Math.abs(_d) / cur_frame.getXY().length();
//              }else {
//                if(corrAngle > Math.PI / 2) corr1 = 1 + (Math.abs(_d) * Math.tan(corrAngle - Math.PI / 2)) / cur_frame.getXY().length();
//                else corr1 = 1 - (Math.abs(_d) * Math.tan(Math.PI / 2 - corrAngle)) / cur_frame.getXY().length();
//              };

              
              centerPoint = MathUtils.CalcCrossLineArc(_d, cur_frame.getXY(), centerPoint, next_frame.hasG02());
              //TODO сделать соединение отрезком или окружностью 
              if(centerPoint == null) {
              	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
              	_ekvErr.setMessage("Невозможно построить эквидистантный контур, слишком большая эквидистанта");
            	  return null;
              }
              
              //корректируем конечную точку
              double corr = centerPoint.length() / MathUtils.length(centerX, centerY);
              endPoint.x *= corr; 
              endPoint.y *= corr;
              
              //коррекция выхода
              if(i == _shapeFrames.size() - 1) {
                correctOut.x = endPoint.x - endX;
                correctOut.y = endPoint.y - endY;
              };
              
              double corr1 = 1;
              Point corrLine = new Point(centerPoint.x + centerX, centerPoint.y + centerY);
              
              if(corrLine.length() == 0) {
                corr1 = 1;
              }else {
                double corrAngle = Math.abs(MathUtils.calculateCrossAngleRad(cur_frame.getXY(), corrLine));
                if((corrAngle == 0) || (corrAngle == Math.PI)){//нет тангенса
                  corr1 = 1 - Math.abs(_d) / cur_frame.getXY().length();
                }else {
                  if(corrAngle > Math.PI / 2) corr1 = 1 + (Math.abs(_d) * Math.tan(corrAngle - Math.PI / 2)) / cur_frame.getXY().length();
                  else corr1 = 1 - (Math.abs(_d) * Math.tan(Math.PI / 2 - corrAngle)) / cur_frame.getXY().length();
                };
              };
              crossPoint.x = (int)(cur_frame.getXY().x * corr1);
              crossPoint.y = (int)(cur_frame.getXY().y * corr1);
              crossPoint.x = (int)(cur_frame.getXY().x * corr1);
              crossPoint.y = (int)(cur_frame.getXY().y * corr1);
              
              Vector ccs = new Vector(3);
              CpSubFrame sub_frame = null;
              ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              if(centerPoint.isZero()){
              	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
              	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше радиуса");
            		return null;
              };
              
              Vector ccs1 = new Vector(5);
              ccs1.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs1.add(new CC(CC.X, endPoint.x - centerPoint.x));
              ccs1.add(new CC(CC.Y, endPoint.y - centerPoint.y));
              ccs1.add(new CC(CC.I, - centerPoint.x));
              ccs1.add(new CC(CC.J, - centerPoint.y));

              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs1.toArray(new CC[0]));
            break;
          }
        };
        break;
        case CpFrame.FRAME_TYPE_ARC:
          switch(next_frame.getType()) {
            case CpFrame.FRAME_TYPE_LINE:{
              //обратное действие к линия дуга
              next_frame.reverse();cur_frame.reverse();
              CachedCpFrame reverce_cur = next_frame;
              CachedCpFrame reverce_next = cur_frame;
              
              int centerX = Utils.toInt(reverce_next.getDataByType(CC.I));
              int centerY = Utils.toInt(reverce_next.getDataByType(CC.J));
              Point centerPoint = new Point(centerX,centerY);
              
              int endX = Utils.toInt(reverce_next.getDataByType(CC.X)) - centerX;
              int endY = Utils.toInt(reverce_next.getDataByType(CC.Y)) - centerY;
              Point endPoint = new Point(endX,endY);
              
              Point corrPoint = new Point(centerX,centerY);
              
              if( (i - countCorr) == 1) {
                centerPoint = MathUtils.CalcCrossLineArc(-_d, reverce_cur.getXY(), centerPoint, reverce_next.hasG02());
//                centerPoint = MathUtils.CalcCrossLineArc(-_d, reverce_cur.getXY(), reverce_next, true);
                //TODO сделать соединение отрезком или окружностью 
                if(centerPoint == null) {
                	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
                	_ekvErr.setMessage("Невозможно построить эквидистантный контур, слишком большая эквидистанта");
            	  return null;
                }else if(centerPoint.isZero()){
                	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
                	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше радиуса");
              	  return null;	
                }
                //корректируем конечную точку
                double corr = centerPoint.length() / MathUtils.length(centerX, centerY);
                correctIn.x = endPoint.x;
                correctIn.y = endPoint.y;
                endPoint.x *= corr; 
                endPoint.y *= corr;
                correctIn.x = endPoint.x - correctIn.x;
                correctIn.y = endPoint.y - correctIn.y;
              }else {
//                if(((_d < 0) && (reverce_next.hasG03())) ||
//                   ((_d > 0) && (reverce_next.hasG02()))){
//                  centerX =  (int)(centerX * (1 + Math.abs(_d) / corrPoint.length()));
//                  centerY =  (int)(centerY * (1 + Math.abs(_d) / corrPoint.length()));
//                }else{
//                  centerX =  (int)(centerX * (1 - Math.abs(_d) / corrPoint.length()));
//                  centerY =  (int)(centerY * (1 - Math.abs(_d) / corrPoint.length()));
//                };
//                centerPoint = MathUtils.CalcCrossLineArc(_d, reverce_cur.getXY(), reverce_next,false);
//                //TODO сделать соединение отрезком или окружностью 
//                if(centerPoint == null) return null;
                double corr = 1;
                
                if(((_d < 0) && (reverce_next.hasG03())) ||
                   ((_d > 0) && (reverce_next.hasG02()))){
                  corr = 1 + Math.abs(_d) / centerPoint.length();  
                }else {
                  corr = 1 - Math.abs(_d) / centerPoint.length();
                };
                
                corrPoint.x *= corr;
                corrPoint.y *= corr;
                
                centerPoint = MathUtils.CalcCrossLineArc(-_d, reverce_cur.getXY(), corrPoint, reverce_next.hasG02());
                //TODO сделать соединение отрезком или окружностью 
                if(centerPoint == null) {
                	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
                	_ekvErr.setMessage("Невозможно построить эквидистантный контур, слишком большая эквидистанта");
	                return null;
	              }else if(centerPoint.isZero()){
	              	_ekvErr.setFrameNumber(_ekvErr.getFrameNumber() + (i + 2));
                	_ekvErr.setMessage("Невозможно построить эквидистантный контур, эквидистанта больше радиуса");
	            	  return null;	
	              }
              };
              
              //коррекция выхода
              if(i == _shapeFrames.size() - 1) {
                //длина
                Point new_end_point1 = MathUtils.GetNormalDot(next_frame.getXY(), -_d);
                correctOut.x = new_end_point1.x - next_frame.getXY().x;
                correctOut.y = new_end_point1.y - next_frame.getXY().y;
              };
              
              double corr1 = 1;
              Point corrLine = new Point();
              corrLine.x = centerPoint.x + corrPoint.x;
              corrLine.y = centerPoint.y + corrPoint.y;
              double corrAngle = Math.abs(MathUtils.calculateCrossAngleRad(reverce_cur.getXY(), corrLine));
              if((corrAngle == 0) || (corrAngle == Math.PI)) {//нет тангенса
                corr1 = 1 + Math.abs(_d) / reverce_cur.getXY().length();
              }else {
                if(corrAngle < Math.PI / 2) corr1 = 1 - (Math.abs(_d) * Math.tan(Math.PI/2 - corrAngle)) / reverce_cur.getXY().length();
                else corr1 = 1 + (Math.abs(_d) * Math.tan(corrAngle - Math.PI/2)) / reverce_cur.getXY().length();
              };
              
              crossPoint.x = (int)(reverce_cur.getXY().x * corr1);
              crossPoint.y = (int)(reverce_cur.getXY().y * corr1);
              
              Vector ccs = new Vector(3);
              CpSubFrame sub_frame = null;
              ccs.add(new CC(CC.G, reverce_cur.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              sub_frame = reverce_cur.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null)
                sub_frame = reverce_cur.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              Vector ccs1 = new Vector(5);
              ccs1.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs1.add(new CC(CC.X, endPoint.x - centerPoint.x));
              ccs1.add(new CC(CC.Y, endPoint.y - centerPoint.y));
              ccs1.add(new CC(CC.I, - centerPoint.x));
              ccs1.add(new CC(CC.J, - centerPoint.y));

              sub_frame = reverce_next.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs1.toArray(new CC[0]));
              
              next_frame.reverse();cur_frame.reverse();
            };
            break;
            case CpFrame.FRAME_TYPE_ARC:{
              //от начала второй с первой
              int f_centerX = Utils.toInt(cur_frame.getDataByType(CC.I));
              int f_centerY = Utils.toInt(cur_frame.getDataByType(CC.J));
              
              int f_endX = Utils.toInt(cur_frame.getDataByType(CC.X)) - f_centerX;
              int f_endY = Utils.toInt(cur_frame.getDataByType(CC.Y)) - f_centerY;
              Point f_End = new Point(f_endX,f_endY);
              
              int s_centerX = Utils.toInt(next_frame.getDataByType(CC.I));
              int s_centerY = Utils.toInt(next_frame.getDataByType(CC.J));
              
              int s_endX = Utils.toInt(next_frame.getDataByType(CC.X)) - s_centerX;
              int s_endY = Utils.toInt(next_frame.getDataByType(CC.Y)) - s_centerY;
              Point s_End = new Point(s_endX,s_endY);
              
              if( (i - countCorr) == 1) {
                crossPoint = MathUtils.CalcCrossArcArc(_d, cur_frame, next_frame, f_End, s_End, true, false);
                
                //корректируем конечную точку
                double corr = f_End.length() / MathUtils.length(f_endX, f_endY);
                correctIn.x = f_centerX;
                correctIn.y = f_centerY;
                f_centerX *= corr; 
                f_centerY *= corr;
                correctIn.x -= f_centerX;
                correctIn.y -= f_centerY;
              }else {
                crossPoint = MathUtils.CalcCrossArcArc(_d, cur_frame, next_frame, f_End, s_End, false, false);
              }; 
              
              //коррекция выхода
              if(i == _shapeFrames.size() - 1) {
                correctOut.x = s_End.x - s_endX;
                correctOut.y = s_End.y - s_endY;
              };
              
              CpSubFrame sub_frame = null;
              Vector ccs = new Vector(5);
              ccs.add(new CC(CC.G, cur_frame.hasG02() ? 2 : 3));
              ccs.add(new CC(CC.X, f_centerX + f_End.x));
              ccs.add(new CC(CC.Y, f_centerY + f_End.y));
              ccs.add(new CC(CC.I, f_centerX));
              ccs.add(new CC(CC.J, f_centerY));
  
              sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              ccs.clear();
              
              ccs.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs.add(new CC(CC.X, s_centerX - crossPoint.x + s_End.x));
              ccs.add(new CC(CC.Y, s_centerY - crossPoint.y + s_End.y));
              ccs.add(new CC(CC.I, s_centerX - crossPoint.x));
              ccs.add(new CC(CC.J, s_centerY - crossPoint.y));
  
              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
            };
            break;
          };
        break;
      };
      
      cur_frame = next_frame;
    };
    if((_close) && (_shapeFrames.size() > 2)){
      //замыкаем
      cur_frame = (CachedCpFrame)_shapeFrames.get(_shapeFrames.size() - 1);
      next_frame = (CachedCpFrame)_shapeFrames.get(0);
      switch(cur_frame.getType()) {
        case CpFrame.FRAME_TYPE_LINE:
          switch(next_frame.getType()) {
            case CpFrame.FRAME_TYPE_LINE:{
              double a_grad = Math.abs(MathUtils.calculateCrossAngleRad(cur_frame.getXY(), next_frame.getXY()));
              double corr1 = 0; double corr2 = 0;              
              //длина
              corr1 = (Math.abs(_d) * Math.tan((Math.PI - a_grad) / 2)) / cur_frame.getXY().length();
              corr2 = (Math.abs(_d) * Math.tan((Math.PI - a_grad) / 2)) / next_frame.getXY().length();
              if(MathUtils.CheckDirectionMove(cur_frame.getXY(),next_frame.getXY())) {
                if(_d < 0){
                  //внутренний угол
                  crossPoint.x = (int) (cur_frame.getXY().x * (1 - corr1));
                  crossPoint.y = (int) (cur_frame.getXY().y * (1 - corr1));
                  crossPoint1.x = (int) (next_frame.getXY().x * (1 - corr2));
                  crossPoint1.y = (int) (next_frame.getXY().y * (1 - corr2));
                }else {
                  crossPoint.x = (int) (cur_frame.getXY().x * (1 + corr1));
                  crossPoint.y = (int) (cur_frame.getXY().y * (1 + corr1));
                  crossPoint1.x = (int) (next_frame.getXY().x * (1 + corr2));
                  crossPoint1.y = (int) (next_frame.getXY().y * (1 + corr2));
                }
              }else{
                if(_d > 0){
                  //внутренний угол
                  crossPoint.x = (int) (cur_frame.getXY().x * (1 - corr1));
                  crossPoint.y = (int) (cur_frame.getXY().y * (1 - corr1));
                  crossPoint1.x = (int) (next_frame.getXY().x * (1 - corr2));
                  crossPoint1.y = (int) (next_frame.getXY().y * (1 - corr2));
                }else {
                  //внешний угол
                  crossPoint.x = (int) (cur_frame.getXY().x * (1 + corr1));
                  crossPoint.y = (int) (cur_frame.getXY().y * (1 + corr1));
                  crossPoint1.x = (int) (next_frame.getXY().x * (1 + corr2));
                  crossPoint1.y = (int) (next_frame.getXY().y * (1 + corr2));
                }
              };
              
              //корректируем вход
              correctIn.x += (next_frame.getXY().x - crossPoint1.x);
              correctIn.y += (next_frame.getXY().y - crossPoint1.y);
              Vector ccs = new Vector(3);
              ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              CpSubFrame sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              //второй вектор
              ccs.clear();
              ccs.add(new CC(CC.G, cur_frame.hasG00()? 0 : 1));
              ccs.add(new CC(CC.X,crossPoint1.x));
              ccs.add(new CC(CC.Y,crossPoint1.y));
              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null) sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
            }
            break;
            case CpFrame.FRAME_TYPE_ARC:{
              //длина
              int centerX = Utils.toInt(next_frame.getDataByType(CC.I));
              int centerY = Utils.toInt(next_frame.getDataByType(CC.J));
              int endX = Utils.toInt(next_frame.getDataByType(CC.X)) - centerX;
              int endY = Utils.toInt(next_frame.getDataByType(CC.Y)) - centerY;
              double corr1 = 1;
              Point oldCenter = new Point(centerX,centerY);
              if(((_d < 0) && (next_frame.hasG03())) ||
                  ((_d > 0) && (next_frame.hasG02()))){
                corr1 = 1 - Math.abs(_d) / oldCenter.length();  
              }else {
                corr1 = 1 + Math.abs(_d) / oldCenter.length();
              };
              
              oldCenter.x *= corr1;
              oldCenter.y *= corr1;
              
              Point endPoint = new Point(endX, endY);
              
              Point centerPoint = MathUtils.CalcCrossLineArc(_d, cur_frame.getXY(), oldCenter,next_frame.hasG02());
              //TODO сделать соединение отрезком или окружностью 
              if(centerPoint == null) return null;
              
              Point corrLine = new Point(centerPoint.x + oldCenter.x, centerPoint.y + oldCenter.y);
              
              if(corrLine.length() == 0) {
                corr1 = 1;
              }else {
                double corrAngle = Math.abs(MathUtils.calculateCrossAngleRad(cur_frame.getXY(), corrLine));
                if((corrAngle == 0) || (corrAngle == Math.PI)){//нет тангенса
                  corr1 = 1 - Math.abs(_d) / cur_frame.getXY().length();
                }else {
                  if(corrAngle > Math.PI / 2) corr1 = 1 + (Math.abs(_d) * Math.tan(corrAngle - Math.PI / 2)) / cur_frame.getXY().length();
                  else corr1 = 1 - (Math.abs(_d) * Math.tan(Math.PI / 2 - corrAngle)) / cur_frame.getXY().length();
                };
              };
              crossPoint.x = (int)(cur_frame.getXY().x * corr1);
              crossPoint.y = (int)(cur_frame.getXY().y * corr1);
              
              Vector ccs = new Vector(3);
              CpSubFrame sub_frame = null;
              ccs.add(new CC(CC.G, 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null)
                sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              Vector ccs1 = new Vector(5);
              ccs1.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs1.add(new CC(CC.X, endPoint.x - centerPoint.x));
              ccs1.add(new CC(CC.Y, endPoint.y - centerPoint.y));
              ccs1.add(new CC(CC.I, - centerPoint.x));
              ccs1.add(new CC(CC.J, - centerPoint.y));

              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs1.toArray(new CC[0]));
              
              //корректируем вход
              correctIn.x = corrLine.x;
              correctIn.y = corrLine.y;
            }
            break;
          };
        break;
        case CpFrame.FRAME_TYPE_ARC:
          switch(next_frame.getType()) {
            case CpFrame.FRAME_TYPE_LINE:{
            //обратное действие к линия дуга
              next_frame.reverse();cur_frame.reverse();
              CachedCpFrame reverce_cur = next_frame;
              CachedCpFrame reverce_next = cur_frame;
              
              int centerX = Utils.toInt(reverce_next.getDataByType(CC.I));
              int centerY = Utils.toInt(reverce_next.getDataByType(CC.J));
              int endX = Utils.toInt(reverce_next.getDataByType(CC.X)) - centerX;
              int endY = Utils.toInt(reverce_next.getDataByType(CC.Y)) - centerY;
              Point endPoint = new Point(endX,endY);
              Point centerPoint = new Point();
              Point corrPoint = new Point(centerX,centerY);

              if(((_d < 0) && (reverce_next.hasG03())) ||
                 ((_d > 0) && (reverce_next.hasG02()))){
                centerX =  (int)(centerX * (1 + Math.abs(_d) / corrPoint.length()));
                centerY =  (int)(centerY * (1 + Math.abs(_d) / corrPoint.length()));
              }else{
                centerX =  (int)(centerX * (1 - Math.abs(_d) / corrPoint.length()));
                centerY =  (int)(centerY * (1 - Math.abs(_d) / corrPoint.length()));
              };
              
              centerPoint = MathUtils.CalcCrossLineArc(_d, reverce_cur.getXY(), reverce_next,false);
              //TODO сделать соединение отрезком или окружностью 
              if(centerPoint == null) return null;
              //корректируем конечную точку
              double corr = centerPoint.length() / corrPoint.length();
              endPoint.x *= corr; 
              endPoint.y *= corr;
              
              double corr1 = 1;
              Point corrLine = new Point();
              corrLine.x = centerPoint.x + centerX;
              corrLine.y = centerPoint.y + centerY;
             
              double corrAngle = Math.abs(MathUtils.calculateCrossAngleRad(reverce_cur.getXY(), corrLine));
              if((corrAngle == 0) || (corrAngle == Math.PI)) {//нет тангенса
                corr1 = 1 + Math.abs(_d) / reverce_cur.getXY().length();
              }else {
                if(corrAngle < Math.PI / 2) corr1 = 1 - (Math.abs(_d) * Math.tan(Math.PI / 2 - corrAngle)) / reverce_cur.getXY().length();
                else corr1 = 1 + (Math.abs(_d) * Math.tan(corrAngle - Math.PI / 2)) / reverce_cur.getXY().length();
              };
              
              crossPoint.x = (int)(reverce_cur.getXY().x * corr1);
              crossPoint.y = (int)(reverce_cur.getXY().y * corr1);
              
              Vector ccs = new Vector(3);
              CpSubFrame sub_frame = null;
              ccs.add(new CC(CC.G, 1));
              ccs.add(new CC(CC.X,crossPoint.x));
              ccs.add(new CC(CC.Y,crossPoint.y));
              sub_frame = reverce_cur.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
              if(sub_frame == null)
                sub_frame = reverce_cur.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              Vector ccs1 = new Vector(5);
              ccs1.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs1.add(new CC(CC.X, endPoint.x - centerPoint.x));
              ccs1.add(new CC(CC.Y, endPoint.y - centerPoint.y));
              ccs1.add(new CC(CC.I, - centerPoint.x));
              ccs1.add(new CC(CC.J, - centerPoint.y));

              sub_frame = reverce_next.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs1.toArray(new CC[0]));
              
              //корректируем вход
              correctIn.x = corrLine.x;
              correctIn.y = corrLine.y;
              
              next_frame.reverse();cur_frame.reverse();
            }
            break;
            case CpFrame.FRAME_TYPE_ARC:{
              //от начала второй с первой
              int f_centerX = Utils.toInt(cur_frame.getDataByType(CC.I));
              int f_centerY = Utils.toInt(cur_frame.getDataByType(CC.J));
              int f_endX = Utils.toInt(cur_frame.getDataByType(CC.X)) - f_centerX;
              int f_endY = Utils.toInt(cur_frame.getDataByType(CC.Y)) - f_centerY;
              Point f_End = new Point(f_endX,f_endY);
              
              int s_centerX = Utils.toInt(next_frame.getDataByType(CC.I));
              int s_centerY = Utils.toInt(next_frame.getDataByType(CC.J));
              int s_endX = Utils.toInt(next_frame.getDataByType(CC.X)) - s_centerX;
              int s_endY = Utils.toInt(next_frame.getDataByType(CC.Y)) - s_centerY;
              Point s_End = new Point(s_endX,s_endY);
              
              crossPoint = MathUtils.CalcCrossArcArc(_d, cur_frame, next_frame, f_End, s_End, false, true);

              CpSubFrame sub_frame = null;
              Vector ccs = new Vector(5);
              ccs.add(new CC(CC.G, cur_frame.hasG02() ? 2 : 3));
              ccs.add(new CC(CC.X, f_centerX + f_End.x));
              ccs.add(new CC(CC.Y, f_centerY + f_End.y));
              ccs.add(new CC(CC.I, f_centerX));
              ccs.add(new CC(CC.J, f_centerY));
  
              sub_frame = cur_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              ccs.clear();
              
              ccs.add(new CC(CC.G, next_frame.hasG02() ? 2 : 3));
              ccs.add(new CC(CC.X, s_centerX - crossPoint.x + s_End.x));
              ccs.add(new CC(CC.Y, s_centerY - crossPoint.y + s_End.y));
              ccs.add(new CC(CC.I, s_centerX - crossPoint.x));
              ccs.add(new CC(CC.J, s_centerY - crossPoint.y));
  
              sub_frame = next_frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
              sub_frame.setData((CC[])ccs.toArray(new CC[0]));
              
              //корректируем вход
              correctIn.x += crossPoint.x;
              correctIn.y += crossPoint.y;
            };
            break;
          };
        break;
      };
      correctOut = new Point(correctIn);
    }
    //копируем чтоб вернуть
    Point res = new Point(correctIn);
    //корректируем вход
    if(_inFrame != null) {
      switch(_inFrame.getType()) {
        case CpFrame.FRAME_TYPE_LINE:{
          correctIn.x += _inFrame.getXY().x;
          correctIn.y += _inFrame.getXY().y;
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, _inFrame.hasG00()? 0 : 1));
          ccs.add(new CC(CC.X,correctIn.x));
          ccs.add(new CC(CC.Y,correctIn.y));
          CpSubFrame sub_frame = _inFrame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if(sub_frame == null) sub_frame = _inFrame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
        }
        break;
        case CpFrame.FRAME_TYPE_ARC:{
          correctIn.x += _inFrame.getXY().x;
          correctIn.y += _inFrame.getXY().y;
          
          //наклоняе систему координат и возвращаем
          Point center = new Point(0,0);
          center.x = Utils.toInt(_inFrame.getDataByType(CC.I)) - _inFrame.getXY().x; 
          center.y = Utils.toInt(_inFrame.getDataByType(CC.J)) - _inFrame.getXY().y;
          
          double angle = MathUtils.calculateAngle2(center.x,center.y) - Math.PI / 2;
          center.rotate(angle,false);
          correctIn.x = - correctIn.x;
          correctIn.y = - correctIn.y;
          correctIn.rotate(angle, false);
          Point newR = new Point(0,0);
          if(correctIn.y == 0) newR.x = correctIn.x / 2;
          else newR.y = (int)Math.abs((correctIn.length() * correctIn.length()) / (2 * correctIn.y));
          newR.rotate(angle, true);
          correctIn.rotate(angle,true);
          
          Vector ccs = new Vector(5);
          ccs.add(new CC(CC.G, _inFrame.hasG02() ? 2 : 3));
          ccs.add(new CC(CC.X, -correctIn.x));
          ccs.add(new CC(CC.Y, -correctIn.y));
          ccs.add(new CC(CC.I, newR.x - correctIn.x));
          ccs.add(new CC(CC.J, newR.y - correctIn.y));
          CpSubFrame sub_frame = _inFrame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
        }
        break;
      };
    };
    if(_outFrame != null) {
      switch(_outFrame.getType()) {
        case CpFrame.FRAME_TYPE_LINE:{
          correctOut.x = _outFrame.getXY().x - correctOut.x; 
          correctOut.y = _outFrame.getXY().y - correctOut.y; 
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, _outFrame.hasG00()? 0 : 1));
          ccs.add(new CC(CC.X,correctOut.x));
          ccs.add(new CC(CC.Y,correctOut.y));
          CpSubFrame sub_frame = _outFrame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if(sub_frame == null) sub_frame = _outFrame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
        }
        break;
        case CpFrame.FRAME_TYPE_ARC:{
          correctOut.x = _outFrame.getXY().x - correctOut.x; 
          correctOut.y = _outFrame.getXY().y - correctOut.y;
          _outFrame.getXY().y -= correctOut.y;
          Vector ccs = new Vector(5);
          ccs.add(new CC(CC.G, _outFrame.hasG02() ? 2 : 3));
          ccs.add(new CC(CC.X, correctOut.x));
          ccs.add(new CC(CC.Y, correctOut.y));
          ccs.add(new CC(CC.I, correctOut.x / 2));
          ccs.add(new CC(CC.J, correctOut.y / 2));
          CpSubFrame sub_frame = _outFrame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
          sub_frame.setData((CC[])ccs.toArray(new CC[0]));
        }
        break;
    };
    };
    //возвращаем чтоб поправить выход
    return res;
  }


  private CompilerError splitCpList(CPList cpList, CpParameters cpParameters)
  {
//      ArrayList shapeFrames = new ArrayList();
//      ArrayList entranceFrames = new ArrayList();
//      CachedCpFrame leadInFrame = null;
//      CachedCpFrame leadOutFrame = null;
//
//      CachedCpFrame tmpGeoFrame = null;
//
//      int shapeIndex = 0;
//
//      Point absPosition = new Point(0, 0);
//      Point absStartPosition = null;
//      Point absEndPosition = null;
// 
//      Boolean g41Global = null;
//      int d = 0;
//      boolean hasDComand = false;
//
//      boolean inKerfBlock = false;
//
//      Shape shape = null;
//
//      for (int i = 0; i < cpList.getLength(); i++)
//      {
//          CachedCpFrame frame = cpList.getFrame(i);
//
//          Integer dValue = frame.getDataByType(CC.D);
//          if (dValue != null)
//          {
//              hasDComand = true;
//              d = dValue.intValue();
//          }
//
//          int[] gData = frame.getDataArrayByType(CC.G);
//          Arrays.sort(gData);
//          boolean g41 = Arrays.binarySearch(gData, 41) >= 0;
//          boolean g42 = Arrays.binarySearch(gData, 42) >= 0;
//          boolean g40 = Arrays.binarySearch(gData, 40) >= 0;
//
////          TODO
////          if (g41 || g42 || g40)
////          {
////             удалить команды эквидистанты;
////             подкадр или весь кадр;
////             если удалили весь кадр, то НЕ РЕСУРСОЕМКИМ методом "очистить" cpList;
////          }
//          
//          if ((g41 && g42) ||
//              (g41 && g40) ||
//              (g42 && g40))
//              return new CompilerError(i + 1, "слишком много команд эквидистанты");
//
//          if (g40 ||
//             (i == cpList.getLength() - 1 && g41Global != null))
//          {
//              if (g41Global == null)
//                  _warnings.add(new CompilerError(i + 1, "команда G40 используется без команд G41 G42"));
//              else
//              {
//                  inKerfBlock = false;
//
//                  absEndPosition = new Point(absPosition.x, absPosition.y);
//
////                  if (g40 == true)
//                      leadOutFrame = frame;
////                  else
////                      shapeFrames.add(frame);
//                  //если frame не геокадр тогда доделать
//
//                  if (shapeFrames.size() > 0)
//                  {
//                      for (int k = entranceFrames.size() - 1; k >= 0; k--)
//                      {
//                          CachedCpFrame eFrame = (CachedCpFrame)entranceFrames.get(k);
//                          if (eFrame.isGeo())
//                          {
//                              entranceFrames.remove(k);
//                              break;
//                          }
//                          else
//                          {
//                              _log.warn("Опасность в контуре №" + shapeIndex + ": Кадр \"" + eFrame + "\" перенесен из захода в контур");
//                              shapeFrames.add(0, eFrame);
//                              entranceFrames.remove(k);
//                          }
//                      }
//
//                      if (hasDComand == false)
//                      {
//                          d = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_KERFING_D));
//                          d *= Compiler.SIZE_TRANSFORMATION_RATIO;
//                      }
//
//                      shape = new Shape(
//                              (CachedCpFrame[])shapeFrames.toArray(new CachedCpFrame[0]),
//                              g41Global.booleanValue(), !g41Global.booleanValue(), d,
//                              (CachedCpFrame[])entranceFrames.toArray(new CachedCpFrame[0]),
//                              leadInFrame, leadOutFrame, absStartPosition, absEndPosition);
//                      _shapes.add(shape);
//                      shapeFrames.clear();
//                      entranceFrames.clear();
//                      tmpGeoFrame = null;
//                      absEndPosition = null;
//                      absStartPosition = null;
//                      g41Global = null;
//                  }
//              }
//          }
//
//          if (g41 || g42)
//          {
//              if (g41Global != null)
//              {
//                  int gTmp = 42;
//                  if (g41)
//                      gTmp = 41;
//                  _warnings.add(new CompilerError(i + 1, "некорректно используется команда \"G" + gTmp + "\""));
//              }
//              else
//              {
//                  g41Global = new Boolean(g41);
//                  inKerfBlock = true;
//                  shapeIndex++;
//                  leadInFrame = tmpGeoFrame;
//                  absStartPosition = new Point(absPosition);
//              }
//          }
//
//          if (frame.isGeo())
//          {
//              tmpGeoFrame = frame;
//              if (frame.hasX())
//                  absPosition.x += frame.getDataByType(CC.X).intValue();
//              if (frame.hasY())
//                  absPosition.y += frame.getDataByType(CC.Y).intValue();
//          }
//
//          if (g40 == false)
//          {
//              if (inKerfBlock)
//                  shapeFrames.add(frame);
//              else 
//                  //if (i != cpList.getLength() - 1)
//                      entranceFrames.add(frame);
//          }
//      }
//
//      if (g41Global != null)
//      {
//          int gValue = 41;
//          if (g41Global.booleanValue() == false)
//              gValue = 42;
//          _warnings.add(new CompilerError(shapeIndex + 1, -1, "Команда \"G" + gValue + "\" используется без команды \"G40\""));
//      }
//
//      if (entranceFrames.size() > 0)
//          _exitFrames = (CachedCpFrame[])entranceFrames.toArray(_exitFrames);

      return null;
  }

  private CompilerError processShapes(CPList cpList, CpParameters cpParameters)
  {
      CompilerError result = null;

      int kerfedShapesCount = 0;

      result = updateLeads();
      if (result != null)
          return result;

      int initalKValue = Integer.parseInt((String)cpParameters.getValue(Compiler.PARAM_KERFING_K));
      if (initalKValue < 3)
          _log.warn("Первоначальное значение K: " + initalKValue);

      int index = 0;
      Iterator iterator = _shapes.iterator();
      while (iterator.hasNext())
      {
          index++;
          Shape shape = (Shape)iterator.next();
          if (shape.getDValue() == 0)
              continue;
          kerfedShapesCount++;

          int K = getK(shape, initalKValue);

          result = calculateKerf(shape, index);
          if (result != null)
          {
              result.setShapeNumber(index);
              return result;
          }

          _log.info("Значение параметра K установлено в " + K);
          result = removeHitches(shape, K);
          if (result != null)
          {
              result.setShapeNumber(index);
              return result;
          }
      }

      // TODO impl correctKerfedShapes(); // устранение накопления погрешности

      _log.debug("Обработано контуров: " + index);

      if (kerfedShapesCount > 0)
          cpList.setData(combineShapes().getData());

      return null;
  }

  private int getK(Shape shape, int initalK)
  {
      int geoFramesCount = shape.getGeoFramesCount();

      final int buffer = 4; // 4 = 2+2. По 2 дополнительных кадра на подход и отход (на случай если каряво составлена УП-ха)

      if (geoFramesCount > initalK + buffer)
          return initalK;

      if (initalK < geoFramesCount && geoFramesCount < initalK + buffer)
          geoFramesCount += buffer;

      if (geoFramesCount >= 2 && geoFramesCount <= buffer)
      {
          boolean onlyArcs = true;
          CachedCpFrame[] frames = shape.getData();
          for (int i = 0; i < frames.length; i++)
          {
              CachedCpFrame frame = frames[i];
              if (frame.getType() == CpFrame.FRAME_TYPE_LINE)
              {
                  onlyArcs = false;
                  break;
              }
          }
          if (onlyArcs)
              return 0;
      }

      if (geoFramesCount <= 1)
          return 0;

      if (geoFramesCount <= buffer)
          return 2;

      if (initalK > geoFramesCount / 2)
          return geoFramesCount / 2;

      return initalK;
  }

  protected CompilerError updateLeads()
  {
      for (int l = 0; l < _shapes.size(); l++)
      {
          Shape shape = (Shape)_shapes.get(l);

          if (shape.getDValue() == 0)
              continue;

          CachedCpFrame leadInFrame = shape.getLeadInFrame();
          CachedCpFrame leadOutFrame = shape.getLeadOutFrame();

          CachedCpFrame[] frames = new CachedCpFrame[] {
                  CPList.getFirstGeoFrame(shape.getData()),
                  CPList.getLastGeoFrame(shape.getData()) };

          for (int k = 0; k < frames.length; k++)
          {
              Boolean g2 = null;
              if (frames[k].hasG02() == true || frames[k].hasG03() == true)
                  g2 = new Boolean(frames[k].hasG02());

              int x = Utils.toInt(frames[k].getDataByType(CC.X));
              int y = Utils.toInt(frames[k].getDataByType(CC.Y));
              int i = Utils.toInt(frames[k].getDataByType(CC.I));
              int j = Utils.toInt(frames[k].getDataByType(CC.J));

              Point kerfNormal1 = new Point(0, 0);
              Point kerfNormal2 = new Point(0, 0);
              calculateKerfNormals(x, y, i, j, g2, shape.isG41(),
                      shape.getDValue(), kerfNormal1, kerfNormal2);

              if (k == 0)
              {
                  // подход
                  if (leadInFrame == null)
                      continue;
                  if (leadInFrame.isGeo())
                  {
                      if (updateLead(leadInFrame, kerfNormal1, false) == false)
                          shape.setLeadInFrame(null);
//                      updateLead(leadInFrame, kerfNormal1, new Boolean(false));
                  }
                  else
                  {
                      CachedCpFrame lastEntranceGeoFrame = CPList.getLastGeoFrame(shape.getEntranceFrames());
                      if (lastEntranceGeoFrame != null)
                      {
                          if (updateLead(lastEntranceGeoFrame, kerfNormal1, false) == false)
                              System.err.println("getLastGeoFrame violation");// shape.getEntranceFrames()[position.intValue()] = null;
//                          updateLead(lastEntranceGeoFrame, kerfNormal1, null);
                      }
                      else
                      {
                          if (l == 0)
                              continue;
                          CachedCpFrame prevLeadOutFrame = ((Shape)_shapes.get(l - 1)).getLeadOutFrame();
                          if (prevLeadOutFrame.isGeo())
                          {
                              if (updateLead(prevLeadOutFrame, kerfNormal1, false) == false)
                                  ((Shape)_shapes.get(l - 1)).setLeadOutFrame(null);
//                              updateLead(prevLeadOutFrame, kerfNormal1, new Boolean(true));
                          }
                          else
                              _warnings.add(new CompilerError("Между контурам №" + (l+1) + " и №" + l +
                              " не содержится ни одного перемещения"));
                      }
                  }
              }
              else
              {
                  // отход
                  if (leadOutFrame.isGeo())
                  {
                      if (updateLead(leadOutFrame, kerfNormal2, true) == false)
                          shape.setLeadOutFrame(null);
//                      updateLead(leadOutFrame, kerfNormal2, new Boolean(true));
                  }
                  else
                  {
                      CachedCpFrame[] leadOutFrames;
                      CachedCpFrame nextFirstEnteranceFrame = null;
                      if (l + 1 < _shapes.size())
                          leadOutFrames = ((Shape)_shapes.get(l + 1)).getEntranceFrames();
                      else
                          leadOutFrames = _exitFrames;

                      nextFirstEnteranceFrame = CPList.getFirstGeoFrame(leadOutFrames);

                      if (nextFirstEnteranceFrame != null)
                      {
                          if (updateLead(nextFirstEnteranceFrame, kerfNormal2, true) == false)
                              System.err.println("getFirstGeoFrame violation"); // leadOutFrames[position.intValue()] = null;
//                          updateLead(nextFirstEnteranceFrame, kerfNormal2, null);
                      }
                      else
                      {
                          if (l == _shapes.size() - 1)
                              continue;
                          CachedCpFrame nextLeadInFrame = ((Shape)_shapes.get(l + 1)).getLeadInFrame();
                          if (nextLeadInFrame.isGeo())
                          {
                              if (updateLead(nextLeadInFrame, kerfNormal2, true) == false)
                                  ((Shape)_shapes.get(l + 1)).setLeadInFrame(null);
//                              updateLead(nextLeadInFrame, kerfNormal2, new Boolean(false));
                          }
                          else
                              _warnings.add(new CompilerError("Между контурам №" + (l+1) + " и №" + l +
                              " не содержится ни одного перемещения"));
                      }
                  }
              }
          }
      }

      return null;
  }
  private boolean updateLead(CachedCpFrame leadFrame, Point kerfNormal, boolean leadOut)
  {
      if (leadFrame.isGeo() == false)
          throw new IllegalArgumentException("Lead frame is not geo: " + leadFrame.toString());

      boolean g2 = leadFrame.hasG02();

      int x = Utils.toInt(leadFrame.getDataByType(CC.X));
      int y = Utils.toInt(leadFrame.getDataByType(CC.Y));
      int i = Utils.toInt(leadFrame.getDataByType(CC.I));
      int j = Utils.toInt(leadFrame.getDataByType(CC.J));

      checkKerfNormal(leadFrame, kerfNormal, new Boolean(leadOut), g2, x, y, i, j);

      x += kerfNormal.x;
      y += kerfNormal.y;
      if (x == 0 && y == 0)
      {
          String leadStr = "Подход";
          if (leadOut)
              leadStr = "Отход";
          _log.warn(leadStr + " вырожден (эквидистанта равна ему)");
          if (leadFrame.getLength() == 1)
              return false;
          else
          {
              leadFrame.removeGeoSubFrame();
              return true;
          }
      }

      if (leadFrame.getType() == CpFrame.FRAME_TYPE_LINE)
      {
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, leadFrame.hasG00() ? 0 : 1));
          if (x != 0)
              ccs.add(new CC(CC.X, x));
          if (y != 0)
              ccs.add(new CC(CC.Y, y));

          CpSubFrame geoSubFrame = leadFrame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if (geoSubFrame == null)
              geoSubFrame = leadFrame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
          geoSubFrame.setData((CC[])ccs.toArray(new CC[0]));
      }
      else if (leadFrame.getType() == CpFrame.FRAME_TYPE_ARC)
      {
          if (_approximateLeadsByLines)
              return approximateArcLeadByLine(leadFrame, x, y);
          else
          {
              // FIXME требует доработки
              double l = MathUtils.length(x, y);
              double r = MathUtils.length(i, j) * 1.5;
              double tmpX = l / 2;
              double tmpY = Math.sqrt(r*r - l*l/4);
              double alphaRad = MathUtils.calculateAngle2(x, y);
              Point endPoint = MathUtils.rotateCoords(alphaRad,
                      (int)Math.round(tmpX), (int)Math.round(tmpY), !g2);
              i = endPoint.x;
              j = endPoint.y;

              Vector ccs = new Vector(5);
              ccs.add(new CC(CC.G, leadFrame.hasG02() ? 2 : 3));
              if (x != 0)
                  ccs.add(new CC(CC.X, x));
              if (y != 0)
                  ccs.add(new CC(CC.Y, y));
              if (i != 0)
                  ccs.add(new CC(CC.I, i));
              if (j != 0)
                  ccs.add(new CC(CC.J, j));

              leadFrame.getSubFrameByType(CpSubFrame.RC_GEO_ARC).
              setData((CC[])ccs.toArray(new CC[0]));

              leadFrame.setHasI(i != 0);
              leadFrame.setHasJ(j != 0);
          }
      }
      else
          System.err.println("Achtung! Lead frame is not geo");

      leadFrame.setHasX(x != 0);
      leadFrame.setHasY(y != 0);

      return true;
  }

  /** Преобразовать подход/отход (дугу) в линию
   */
  private boolean approximateArcLeadByLine(CachedCpFrame leadFrame, int x, int y)
  {
      Vector ccs = new Vector(5);
      ccs.add(new CC(CC.G, 1));
      if (x != 0)
          ccs.add(new CC(CC.X, x));
      if (y != 0)
          ccs.add(new CC(CC.Y, y));

      int k = -1;
      CpSubFrame subFrame = null;
      for (k = 0; k < leadFrame.getLength(); k++)
      {
          subFrame = leadFrame.getSubFrame(k);
          if (subFrame.isGeo())
              break;
      }
      if (ccs.size() == 1)
      {
          leadFrame.setCpSubFrame(k, null);
          _log.warn("Вырождение подхода/отхода при обновлении");
          return false;
      }

      subFrame.setType(CpSubFrame.RC_GEO_LINE);
      subFrame.setData((CC[])ccs.toArray(new CC[0]));
      leadFrame.setType(CpFrame.FRAME_TYPE_LINE);
      leadFrame.setHasG01(true);
      leadFrame.setHasG02(false);
      leadFrame.setHasG03(false);
      leadFrame.setHasX(x != 0);
      leadFrame.setHasY(y != 0);
      leadFrame.setHasI(false);
      leadFrame.setHasJ(false);

      return true;
  }

  private void checkKerfNormal(CachedCpFrame leadFrame, Point kerfNormal,
          Boolean leadOut, boolean g2, int x, int y, int i, int j)
  {
      // FIXME !!! this method can have a BUG, be carefull!
      // если внешний контур, то инвертить!!! (?) также зависит от подход или отход

      if (leadOut == null)
      {
          kerfNormal.inverse();
          return;
      }

      double a1 = MathUtils.calculateAngle2(x, y);
      double a2 = MathUtils.calculateAngle2(kerfNormal.x, kerfNormal.y);
      double alpha = Math.toDegrees(a1 - a2);

      alpha = Math.abs(alpha);

//      if (270 < alpha || alpha < 90)
//          kerfNormal.inverse();

      if (leadOut.booleanValue())
          kerfNormal.inverse();

//      if ((270 < alpha || alpha < 90) || leadOut.booleanValue())
//          kerfNormal.inverse();
  }

  /**Use abs positions:
      CachedCpFrame lineFrame = CPList.getFirstGeoFrame(entranceFrames, CpFrame.FRAME_TYPE_LINE);
      if (lineFrame != null)
      {
          CC ccX = lineFrame.getCCByType(CC.X);
          CC ccY = lineFrame.getCCByType(CC.Y);

          if (prevShape == null)
          {
              ccX.setData(shape.getStartPoint().x);
              ccY.setData(shape.getStartPoint().y);
          }
          else
          {
              ccX.setData(shape.getStartPoint().x - prevShape.getEndPoint().x);
              ccY.setData(shape.getStartPoint().y - prevShape.getEndPoint().y);
              // + Utils.toInt(shape.getLeadInFrame().getDataByType(CC.X))
          }
      }
   */
  protected CPList combineShapes()
  {
      CPList combinedCpList = null;

      Shape prevShape = null;

      int newLength = 0;
      Iterator iterator = _shapes.iterator();
      while (iterator.hasNext())
      {
          Shape shape = (Shape)iterator.next();
          newLength += shape.getFullLength();
          if (prevShape != null && prevShape.getLeadOutFrame().equals(shape.getLeadInFrame()))
              newLength--;
          prevShape = shape;
      }
      newLength += _exitFrames.length;

      int k = 0;
      CachedCpFrame[] newFrames = new CachedCpFrame[newLength];

      prevShape = null;
      int index = 0;
      iterator = _shapes.iterator();
      while (iterator.hasNext())
      {
          index++;
          Shape shape = (Shape)iterator.next();

          CachedCpFrame[] entranceFrames = shape.getEntranceFrames();
          if (entranceFrames.length > 0)
              for (int i = 0; i < entranceFrames.length; i++)
                  newFrames[k++] = entranceFrames[i];

          if (shape.getLeadInFrame() != null)
          {
              if ( (prevShape == null) || (prevShape != null && prevShape.getLeadOutFrame().equals(shape.getLeadInFrame()) == false) )
                  newFrames[k++] = shape.getLeadInFrame();
          }
          else
          {
              _log.warn("leadIn is null");
          }

          CachedCpFrame[] dataFrames = shape.getData();
          if (dataFrames.length > 0)
          {
              for (int i = 0; i < dataFrames.length; i++)
                  newFrames[k++] = dataFrames[i];
          }
          else
              _log.warn("Контур №" + index + " не содержит управляющих команд");

          if (shape.getLeadOutFrame() != null)
              newFrames[k++] = shape.getLeadOutFrame();
          else
          {
              _log.warn("leadOut is null");
          }

          prevShape = shape;
      }

      for (int i = 0; i < _exitFrames.length; i++)
          newFrames[k++] = _exitFrames[i];

      combinedCpList = new CPList(newFrames);

      return combinedCpList;
  }
  protected CompilerError calculateKerf(Shape shape, int index)
  {
      _log.info("Контур №" + index + " ----------------------------------------------");

      if (shape.isKerfed() == false)
      {
          _log.debug("Контур не содержит команд эквидистантны (G41, G42)");
          return null;
      }
      if (shape.getDValue() == 0)
      {
          _log.debug("Контур содержит нулевую эквидистанту (D=0)");
          return null;
      }

      if (shape.isSpaceless() == false)
      {
          // TODO impl
          _log.info("Контур разомкнутый");
          _warnings.add(new CompilerError(index, -1, "контур разомкнутый"));
      }

      Point prevKerfNormal = null; // в относительных координатах (относительно crossPoint)
      Point nextKerfNormal = null; // в относительных координатах (относительно crossPoint)

      CachedCpFrame arcFrame = null;

      CachedCpFrame firstFrame = CPList.getFirstGeoFrame(shape.getData());
      if (firstFrame == null)
          return new CompilerError(index, -1, "контур не содержит геометрических команд");

      Vector kerfedFrames = new Vector();
      for (int k = 0; k < shape.getLength() + 1; k++)
      {
          boolean reducedFrame = false; // вырожденный кадр или нет (бывает с дугами, когда величина D = радиусу дуги)
          CachedCpFrame frame;
          if (k == shape.getLength()) // последнее перемещение сопрягается с первым
              frame = firstFrame;
          else
              frame = shape.getFrame(k);

          if (frame.isGeo() == false)
          {
              kerfedFrames.add(frame);
              continue;
          }

          int x = 0, y = 0, i = 0, j = 0;
          Boolean g2 = null;
          Point kerfNormal1 = new Point(0, 0);
          Point kerfNormal2 = new Point(0, 0);

          if (frame.hasG02() == true || frame.hasG03() == true)
              g2 = new Boolean(frame.hasG02());

          if (frame.hasX() || frame.hasY() || frame.hasI() || frame.hasJ())
          {
              // process GEO
              for (int l = 0; l < frame.getLength(); l++)
              {
                  CpSubFrame subFrame = frame.getSubFrame(l);
                  if (subFrame.getType() == CpSubFrame.RC_GEO_LINE ||
                      subFrame.getType() == CpSubFrame.RC_GEO_FAST ||
                      subFrame.getType() == CpSubFrame.RC_GEO_ARC)
                  {
                      x = Utils.toInt(subFrame.getDataByType(CC.X));
                      y = Utils.toInt(subFrame.getDataByType(CC.Y));
                      i = Utils.toInt(subFrame.getDataByType(CC.I));
                      j = Utils.toInt(subFrame.getDataByType(CC.J));

                      calculateKerfNormals(x, y, i, j, g2, shape.isG41(),
                              shape.getDValue(), kerfNormal1, kerfNormal2);
                      if (g2 != null)
                      {
                          if (prevKerfNormal == null)
                          {
                              prevKerfNormal = new Point(kerfNormal2);
                              continue;
                          }

                          int checkResult = checkArcRadius(MathUtils.length(i, j), (double)shape.getDValue(), shape.isG41(), g2.booleanValue());
                          if (checkResult == -1)
                              return new CompilerError(index, k + 1, "Величина эквидистанты больше чем внутренний радиус дуги");
                          else if (checkResult == 0)
                              _warnings.add(new CompilerError(index, k + 1, "Величина эквидистанты равна внутреннему радиусу дуги"));

                          // модифицировать дуги
                          if (updateArc(frame, subFrame, g2, x, y, i, j, kerfNormal1, kerfNormal2) == false)
                          {
                              // если дуга выродилась, т.е. r дуги = D (эквидистанте)
                              // сделать две вспомогательных дуги и вставить их в контур вместо выродившейся дуги;
                              reducedFrame = true;
                          }
                      }

                      nextKerfNormal = kerfNormal1;
                      if (prevKerfNormal == null)
                          prevKerfNormal = nextKerfNormal;
                      //********************
                      double crossAngle = calculateKerfCrossAngle(prevKerfNormal, nextKerfNormal);
                      _log.debug("crossAngle = " + crossAngle);
                      double unsignedCrossAngle = crossAngle;
                      if (unsignedCrossAngle < 0)
                          unsignedCrossAngle += 360;
                      if (MathUtils.compareDouble(180, unsignedCrossAngle, MathUtils.EPS) == true)
                      {
                          prevKerfNormal = new Point(kerfNormal2);
                          continue;
                      }
                      boolean arcDirection = calculateAuxiliaryArcDirection(crossAngle);

                      if (k < shape.getLength() || (reducedFrame && k == shape.getLength()))
                          arcFrame = createAuxiliaryArcFrame(prevKerfNormal, nextKerfNormal, arcDirection);
                      else
                      {
                          // сопряжение первого перемещения с последним
                          // вместо скругления сделать пересечение
                          arcFrame = null;
                          if (shape.isNormal()) // if (shape.isSpaceless())
                          {
                              CompilerError ce = intersectLastCoupling(shape, index, prevKerfNormal, nextKerfNormal, kerfedFrames, crossAngle);
                              if (ce != null)
                                  return ce;
                          }
                      }

                      //********************
                      prevKerfNormal = new Point(kerfNormal2);
                  }
              }
          }

          if (arcFrame != null)
          {
              kerfedFrames.add(arcFrame);
              arcFrame = null;
          }

          if (k != shape.getLength())
          {
              if (frame != null && reducedFrame == false)
                  kerfedFrames.add(frame);
          }
          else
          {
              if (reducedFrame)
              {
                  // если первый кадр вырождается, то занулить гео подкадр (или весь кадр)
                  if (frame.getLength() == 1)
                      kerfedFrames.remove(0);
                  else
                      frame.removeGeoSubFrame();
              }
          }
      }
      shape.setData((CachedCpFrame[])kerfedFrames.toArray(new CachedCpFrame[0]));

      return null;
  }

  /**сопряжение первого перемещения с последним, вместо скругления сделать пересечение
   * @param shape
   * @param index
   * @param prevKerfNormal
   * @param nextKerfNormal
   * @param kerfedFrames
   * @param crossAngle
   * @return
   */
  private CompilerError intersectLastCoupling(Shape shape, int index,
          Point prevKerfNormal, Point nextKerfNormal,
          Vector kerfedFrames, double crossAngle)
  {
      CachedCpFrame firstFrame = CPList.getFirstGeoFrame((CachedCpFrame[])kerfedFrames.toArray(new CachedCpFrame[0]));
      CachedCpFrame lastFrame = CPList.getLastGeoFrame((CachedCpFrame[])kerfedFrames.toArray(new CachedCpFrame[0]));

      if (firstFrame.contains(CC.G, 41) ||
          firstFrame.contains(CC.G, 42))
          firstFrame.removeSubFrameByType(CpSubFrame.RC_D_COMMAND);
      
      if (lastFrame.contains(CC.G, 40))
          lastFrame.removeSubFrameByType(CpSubFrame.RC_D_COMMAND);
      
      double nextKerfNormalLength = Math.sqrt(MathUtils.sqr(nextKerfNormal.x) + MathUtils.sqr(nextKerfNormal.y));
      double prevKerfNormalLength = Math.sqrt(MathUtils.sqr(prevKerfNormal.x) + MathUtils.sqr(prevKerfNormal.y));

      Point firstFrameTangent = new Point(); // касательная к первому гео кадру в точке пересечения
      Point lastFrameTangent = new Point(); // касательная к последнему гео кадру в точке пересечения
      MathUtils.normalToLine(nextKerfNormal, (int)Math.round(nextKerfNormalLength), true, firstFrameTangent);
      MathUtils.normalToLine(prevKerfNormal, (int)Math.round(prevKerfNormalLength), true, lastFrameTangent);

      double koef = 0;
      if (crossAngle != 0)
          koef = (double)1/MathUtils.tan(crossAngle/2);
      else
          _log.debug("ATTENTION: crossAngle = 0");

      Point firstFrameAddition = new Point((int)Math.round(koef * firstFrameTangent.x),
              (int)Math.round(koef * firstFrameTangent.y));
      Point lastFrameAddition = new Point((int)Math.round(koef * lastFrameTangent.x),
              (int)Math.round(koef * lastFrameTangent.y));

      if (firstFrame.getType() == CpFrame.FRAME_TYPE_ARC)
      {
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, 1));
          if (firstFrameAddition.x != 0)
              ccs.add(new CC(CC.X, firstFrameAddition.x));
          if (firstFrameAddition.y != 0)
              ccs.add(new CC(CC.Y, firstFrameAddition.y));
          if (firstFrameAddition.isZero() == false)
          {
              CachedCpFrame additionalFrame = MTRUtils.createLineFrame(1, firstFrameAddition.x, firstFrameAddition.y);
              kerfedFrames.add(0, additionalFrame);
          }
      }
      else
          updateFrame(firstFrame, firstFrameAddition);

      if (lastFrame.getType() == CpFrame.FRAME_TYPE_ARC)
      {
          Vector ccs = new Vector(3);
          ccs.add(new CC(CC.G, 1));
          if (lastFrameAddition.x != 0)
              ccs.add(new CC(CC.X, lastFrameAddition.x));
          if (lastFrameAddition.y != 0)
              ccs.add(new CC(CC.Y, lastFrameAddition.y));
          if (lastFrameAddition.isZero() == false)
          {
              CachedCpFrame additionalFrame = MTRUtils.createLineFrame(1, lastFrameAddition.x, lastFrameAddition.y);
              
              for (int p = kerfedFrames.size() - 1; p > 0; p--)
              {
                  CachedCpFrame frame = (CachedCpFrame)kerfedFrames.get(p);
                  if (frame.isGeo())
                  {
                      if (p == kerfedFrames.size() - 1)
                          kerfedFrames.add(additionalFrame);
                      else
                          kerfedFrames.add(p + 1, additionalFrame);
                      break;
                  }
              }
          }
      }
      else
          updateFrame(lastFrame, lastFrameAddition);

      firstFrameAddition.inverse();
      lastFrameAddition.inverse();

      if (shape.getLeadInFrame() == null || shape.getLeadInFrame().isGeo() == false)
      {
          _warnings.add(new CompilerError(index, -1, "Контур не имеет подхода"));
          _log.debug("Контур №" + index + " не имеет подхода");
      }
      else
      {
          if (firstFrameAddition.isZero() == false)
              updateFrame(shape.getLeadInFrame(), firstFrameAddition);
      }
      if (shape.getLeadOutFrame() == null || shape.getLeadOutFrame().isGeo() == false)
      {
          _log.debug("Контур №" + index + " не имеет отхода");
          if (lastFrameAddition.isZero() == false)
          {
              Shape nextShape = getNextShape(shape);
              if (nextShape != null)
              {
                  if (nextShape.getEntranceFrames().length > 1)
                  {
                      final String warnMsg = "В переезде(кадры между контурами) содержится более одного гео кадра";
                      _log.warn(warnMsg);
                      _warnings.add(new CompilerError(index, warnMsg));
                  }
                  CachedCpFrame zahodFrame = CPList.getFirstGeoFrame(nextShape.getEntranceFrames(), CpFrame.FRAME_TYPE_LINE);
                  if (zahodFrame == null)
                      return new CompilerError(index, "В переезде между контурами нет линейного перемещения");
                  else
                      updateFrame(zahodFrame, lastFrameAddition);
              }
          }
      }
      else
      {
          if (lastFrameAddition.isZero() == false)
              updateFrame(shape.getLeadOutFrame(), lastFrameAddition);
      }

      return null;
  }

//  private Shape getPreviousShape(Shape currentShape)
//  {
//      Shape previousShape = null;
//
//      Iterator iterator = _shapes.iterator();
//      while (iterator.hasNext())
//      {
//          Shape shape = (Shape)iterator.next();
//          if (shape == currentShape)
//              break;
//          previousShape = shape;
//      }
//
//      return previousShape;
//  }

  private Shape getNextShape(Shape currentShape)
  {
      Iterator iterator = _shapes.iterator();
      while (iterator.hasNext())
      {
          Shape shape = (Shape)iterator.next();
          if (shape == currentShape)
          {
              if (iterator.hasNext())
                  return (Shape)iterator.next();
              break;
          }
      }

      return null;
  }

  /**Использовать только для последнего сопряжения (пересечение, а не скругление)
   * @param frame этим кадром может быть либо первый либо последний гео кадр
   * @param subFrame
   * @param addition
   */
  private void updateFrame(CachedCpFrame frame, Point addition)
  {
      CpSubFrame subFrame = null;
      int x;
      int y;
      int i;
      int j;
      Vector ccs = new Vector(5);
      x = Utils.toInt(frame.getDataByType(CC.X)) + addition.x;
      y = Utils.toInt(frame.getDataByType(CC.Y)) + addition.y;
      i = Utils.toInt(frame.getDataByType(CC.I));
      j = Utils.toInt(frame.getDataByType(CC.J));

      if (frame.hasG00())
      {
          ccs.add(new CC(CC.G, 0));
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
      }
      else if (frame.hasG01())
      {
          ccs.add(new CC(CC.G, 1));
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
      }
      else if (frame.hasG02())
      {
          ccs.add(new CC(CC.G, 2));
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
      }
      else if (frame.hasG03())
      {
          ccs.add(new CC(CC.G, 3));
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
      }

      if (x != 0)
      {
          ccs.add(new CC(CC.X, x));
          frame.setHasX(true);
      }
      if (y != 0)
      {
          ccs.add(new CC(CC.Y, y));
          frame.setHasY(true);
      }
      if (i != 0)
          ccs.add(new CC(CC.I, i));
      if (j != 0)
          ccs.add(new CC(CC.J, j));

      subFrame.setData((CC[])ccs.toArray(new CC[0]));
  }

  private CompilerError removeHitches(Shape shape, int K)
  {
      if (K < 2)
      {
          _log.info("Удаление петель на контуре выключено");
          return null;
      }

      boolean passStartPointDuringChecking = true;

      int hitchFrameCount = 0;
//      boolean stopChecking = false;
//      int checkPassedFramesCount = 0; // количество пропущенных кадров при проверке на пересечение

      for (int p = 0; p < shape.getLength(); p++)
      {
          if (hitchFrameCount > 0)
          {
              p += hitchFrameCount;
              hitchFrameCount = 0;
          }

          CachedCpFrame currentFrame = shape.getFrame(p);

          if (currentFrame == null)
              continue;

          int x = 0, y = 0;
          if (currentFrame.isGeo())
          {
              x = Utils.toInt(currentFrame.getDataByType(CC.X));
              y = Utils.toInt(currentFrame.getDataByType(CC.Y));
          }
          else
              continue;

          int xAbs = x;
          int yAbs = y;
          for (int l = 1; l <= K; l++)
          {
              int position = (p + l) % shape.getLength();
              CachedCpFrame checkFrame = shape.getFrame(position);
              if (checkFrame == null)
                  continue;
              xAbs += Utils.toInt(checkFrame.getDataByType(CC.X));
              yAbs += Utils.toInt(checkFrame.getDataByType(CC.Y));
          }

//          boolean checkForHitch = true;
//          for (int l = 1; l <= K; l++)
//          {
//              CachedCpFrame checkFrame = shape.getFrame(p + l);
//              // если присутствует "специальная" команда, то не проверять на петли
//              if (checkFrame != null)
//              {
//                  Iterator iterator = SPECIAL_COMMANDS.iterator();
//                  while (iterator.hasNext())-
//                  {
//                      CC specialCC = (CC)iterator.next();
//                      if (checkFrame.contains(specialCC))
//                      {
//                          checkForHitch = false;
//                          _log.info("В контуре присутствует \"специальная\" команда");
//                          break;
//                      }
//                  }
//              }
//
//          }

//          if (stopChecking)
//          {
//              checkPassedFramesCount++;
//              continue;
//          }

          boolean hasHitch = false;
          for (int l = K; l > 0; l--)
          {
              int position = (p + l) % shape.getLength();
//              if (p + l >= shape.getLength())
//                  continue;

//              if (stopChecking)
//                  continue;
//
//              if (position < p && shape.isSpaceless() == false)
//              {
//                  stopChecking = true;
//                  _log.warn("Проверка на петли через начало контура прервана т.к. контур разомкнут");
//                  continue;
//              }

              if (hasHitch)
              {
                  CachedCpFrame hitchFrame  = shape.getFrame(position);
                  CachedCpFrame newFrame = null;
                  if (hitchFrame != null && hitchFrame.hasM())
                  {
                      if (hitchFrame.getLength() == 1)
                          continue;
                      CpSubFrame subFrame = hitchFrame.getSubFrameByType(CpSubFrame.RC_M_COMMAND);
                      newFrame = new CachedCpFrame(CpFrame.FRAME_TYPE_UNKNOWN, new CpSubFrame[] {subFrame});
                      newFrame.setHasM(true);
                  }
                  shape.setFrame(position, newFrame);
                  continue;
              }

              if (l == 1) // FIXME в общем случае НЕВЕРНОЕ (?) соседние кадры могут пересекаться -> доработать
                  continue;

              CachedCpFrame checkFrame = shape.getFrame(position);
              if (checkFrame == null || checkFrame.isGeo() == false)
                  continue;

              int checkX = Utils.toInt(checkFrame.getDataByType(CC.X));
              int checkY = Utils.toInt(checkFrame.getDataByType(CC.Y));
              xAbs -= checkX;
              yAbs -= checkY;

              if (currentFrame == checkFrame)
                  continue;

              if (passStartPointDuringChecking)
                  if (p + l >= shape.getLength()/* && (shape.isSpaceless() == false)*/) // may be isNormal() ?
                      continue;

              Point intersectionPoint = MathUtils.calculateIntersectionPoint(currentFrame, checkFrame, xAbs, yAbs);

              if (intersectionPoint != null)
              {
                  //if (K > 2 && (intersectionPoint.isZero() || (intersectionPoint.x == x && intersectionPoint.y == y)) )
                  if (K > 2 && intersectionPoint.isZero())
                      continue;
                  hasHitch = true;
                  hitchFrameCount = l - 1;
                  if (reduceFrame(currentFrame, null, intersectionPoint) == false)
                      shape.setFrame(p, null);
                  if (reduceFrame(checkFrame, new Point(xAbs, yAbs), intersectionPoint) == false)
                      shape.setFrame(position, null);
              }
          }
      }

      Vector frames = new Vector();
      int removedFramesCount = 0;
      for (int p = 0; p < shape.getLength(); p++)
      {
          CachedCpFrame frame = shape.getFrame(p);
          if (frame != null)
              frames.add(frame);
          else
              removedFramesCount++;
      }
      CachedCpFrame[] data = (CachedCpFrame[])frames.toArray(new CachedCpFrame[0]);
      shape.setData(data);

//      if (checkPassedFramesCount > 0)
//          _log.warn("При проверке на петли пропущено " + checkPassedFramesCount + " кадров");

      _log.info("При обработке петель удалено " + removedFramesCount + " кадров");

      return null;
  }

  /**Урезать гео кадр до точки пересечения
   * @param frame
   * @param absPoint если null, то это первый кадр, иначе второй
   * @param intersectionPoint точка пересечения относительно первого кадра
   * @return false если кадр frame вырождается (например если точка пересечение - это первая точка кадра (0,0)),
   * true если все ок
   */
  public static boolean reduceFrame(CachedCpFrame frame, Point absPoint, Point intersectionPoint)
  {
      CpSubFrame subFrame;

      if (frame.getType() == CpFrame.FRAME_TYPE_LINE)
      {
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_LINE);
          if (subFrame == null)
              subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_FAST);
      }
      else if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
      {
          subFrame = frame.getSubFrameByType(CpSubFrame.RC_GEO_ARC);
      }
      else
          throw new IllegalArgumentException("Кадр \"" + frame.toString() +
                  "\" не является геометрическим");

      int g = Utils.toInt(subFrame.getDataByType(CC.G));
      int x = Utils.toInt(subFrame.getDataByType(CC.X));
      int y = Utils.toInt(subFrame.getDataByType(CC.Y));

      Point drift = new Point();
      if (absPoint != null)
      {
          drift.x = - intersectionPoint.x + absPoint.x;
          drift.y = - intersectionPoint.y + absPoint.y;
//          intersectionPoint.x = x - intersectionPoint.x + absPoint.x;
//          intersectionPoint.y = y - intersectionPoint.y + absPoint.y;
          intersectionPoint.x = x + drift.x;
          intersectionPoint.y = y + drift.y;
      }

      if (intersectionPoint.x == 0 && intersectionPoint.y == 0 &&
          frame.getType() == CpFrame.FRAME_TYPE_LINE)
      {
          frame = null;
          //_log.warn("Вырождение кадра при пересечении");
          return false;
      }


      Vector commands = new Vector(5);
      commands.add(new CC(CC.G, g));
      if (intersectionPoint.x != 0)
          commands.add(new CC(CC.X, intersectionPoint.x));
      if (intersectionPoint.y != 0)
          commands.add(new CC(CC.Y, intersectionPoint.y));

      if (frame.getType() == CpFrame.FRAME_TYPE_ARC)
      {
          int i = Utils.toInt(subFrame.getDataByType(CC.I));
          int j = Utils.toInt(subFrame.getDataByType(CC.J));
          if (absPoint != null)
          {
              i += drift.x;
              j += drift.y;
          }
          if (i != 0)
              commands.add(new CC(CC.I, i));
          if (j != 0)
              commands.add(new CC(CC.J, j));
      }

      subFrame.setData((CC[])commands.toArray(new CC[0]));
      return true;
  }

  /**Обновить кадр, содержащий дугу (в соответствии с эквидистантой,
   * заложенной в kerfNormal-ях)
   * @param frame
   * @param subFrame
   * @param g2
   * @param x
   * @param y
   * @param i
   * @param j
   * @param kerfNormal1
   * @param kerfNormal2
   * @return false если дуга выродилась
   */
  private boolean updateArc(CachedCpFrame frame, CpSubFrame subFrame,
          Boolean g2, int x, int y, int i, int j,
          Point kerfNormal1, Point kerfNormal2)
  {
      // модифицировать дуги
      x += kerfNormal2.x - kerfNormal1.x;
      y += kerfNormal2.y - kerfNormal1.y;
      i -= kerfNormal1.x;
      j -= kerfNormal1.y;

      Vector arcControlCommands = new Vector(5, 2);
      int g = 1;
      if (i == 0 && j == 0)
      {
          _log.warn("Вырождение дуги");
          return false;
      }
      else
          g = g2.booleanValue() == true ? 2 : 3;

      arcControlCommands.add(new CC(CC.G, g));

      if (x != 0)
          arcControlCommands.add(new CC(CC.X, x));
      if (y != 0)
          arcControlCommands.add(new CC(CC.Y, y));
      if (i != 0)
          arcControlCommands.add(new CC(CC.I, i));
      if (j != 0)
          arcControlCommands.add(new CC(CC.J, j));
      frame.setHasX(x != 0);
      frame.setHasY(y != 0);
      frame.setHasI(i != 0);
      frame.setHasJ(j != 0);

//      if (arcControlCommands.size() == 1)
//      {
//          //xxx;
//          x = Utils.toInt(frame.getDataByType(CC.X));
//          y = Utils.toInt(frame.getDataByType(CC.Y));
//          if (x == 0 && y == 0)
//              throw new IllegalArgumentException("Achtung!");
//
//          arcControlCommands.clear();
//          arcControlCommands.add(new CC(CC.G, 1));
//          if (x != 0)
//              arcControlCommands.add(new CC(CC.X, x));
//          if (y != 0)
//              arcControlCommands.add(new CC(CC.Y, y));
//
//          ret = false;
//      }

      subFrame.setData((CC[])arcControlCommands.toArray(new CC[0]));
      return true;
  }

  /**Возвращает -1 если эквидистанта больше внутреннего радиуса дуги
   * 0 если эквидистанта равна радиусу, 1 в остальных случаях
   * @param r
   * @param kerf
   * @param g41
   * @param g2
   * @return
   */
  private int checkArcRadius(double r, double kerf, boolean g41, boolean g2)
  {
      if ( (g41 == true && g2 == false) || (g41 == false && g2 == true) )
      {
          if (kerf > r)
              return -1;
          else if (MathUtils.compareDouble(r, kerf, MathUtils.EPS))
              return 0;
      }

      return 1;
  }

  private void calculateKerfNormals(int x, int y, int i, int j, Boolean g2,
          boolean isG41, int d, Point kerfNormal1, Point kerfNormal2)
  {
      Point tmpKerfNormal1;
      Point tmpKerfNormal2;
      if (g2 == null)
      {
          tmpKerfNormal1 = MathUtils.kerfNormalToLine(
                  new Point(x, y), isG41, d);
          tmpKerfNormal2 = tmpKerfNormal1;
      }
      else
      {
          tmpKerfNormal1 = MathUtils.kerfNormalToArc(
                  new Point(x, y), new Point(i, j),
                  g2.booleanValue(), true, isG41, d);
          tmpKerfNormal2 = MathUtils.kerfNormalToArc(
                  new Point(x, y), new Point(i, j),
                  g2.booleanValue(), false, isG41, d);
      }
      kerfNormal1.x = tmpKerfNormal1.x;
      kerfNormal1.y = tmpKerfNormal1.y;

      kerfNormal2.x = tmpKerfNormal2.x;
      kerfNormal2.y = tmpKerfNormal2.y;
  }

  /**Определить направление вспомогательной дуги
   * @param crossAngle - угол сопряжения
   * @return
   */
  private boolean calculateAuxiliaryArcDirection(double crossAngle)
  {
      boolean arcDirection = false;
      if (crossAngle + MathUtils.EPS < 180)
          arcDirection = true; // против ЧС
      else if (crossAngle - MathUtils.EPS > 180)
          arcDirection = false; // по ЧС

      if (crossAngle < 0)
          arcDirection = !arcDirection;

      return arcDirection;
  }

  /**Создать дугу (линию) между кадрами эквидистанты.
   * если длина дуги меньше _kerfMinAuxArcLength, то дуга вырождается в линию
   */
  private CachedCpFrame createAuxiliaryArcFrame(Point prevKerfNormal, 
      Point nextKerfNormal, boolean arcDirection)
  {
      int xArc = nextKerfNormal.x - prevKerfNormal.x;
      int yArc = nextKerfNormal.y - prevKerfNormal.y;
      int iArc = -prevKerfNormal.x;
      int jArc = -prevKerfNormal.y;
      
      // TODO добавил в командировке проверку: если длина дуги меньше заданного значения, то выродить в линию
      if (MathUtils.length(xArc, yArc, iArc, jArc) < _kerfMinAuxArcLength)
          return MTRUtils.createLineFrame(1, xArc, yArc);
      else
          return MTRUtils.createArcFrame(arcDirection == true ? 2 : 3, xArc, yArc, iArc, jArc);
          
      
//      TODO old implementation. It is work!
//      Vector additionalArcControlCommands = new Vector(5, 2);
//      additionalArcControlCommands.add(new CC(CC.G, (arcDirection == true ? 2 : 3), "G aa")); // "aa" - means aux arc
//      if (xArc != 0)
//          additionalArcControlCommands.add(new CC(CC.X, xArc));
//      if (yArc != 0)
//          additionalArcControlCommands.add(new CC(CC.Y, yArc));
//      if (iArc != 0)
//          additionalArcControlCommands.add(new CC(CC.I, iArc));
//      if (jArc != 0)
//          additionalArcControlCommands.add(new CC(CC.J, jArc));
//
//      CpSubFrame arcSubFrame = new CpSubFrame(
//              CpSubFrame.RC_GEO_ARC, (CC[])additionalArcControlCommands.toArray(new CC[0]));
//
//      CachedCpFrame auxFrame = new CachedCpFrame(CpFrame.FRAME_TYPE_ARC, new CpSubFrame[] {arcSubFrame});
//      auxFrame.setHasG02(arcDirection);
//      auxFrame.setHasG03(!arcDirection);
//      auxFrame.setHasX(xArc != 0);
//      auxFrame.setHasY(yArc != 0);
//      auxFrame.setHasI(iArc != 0);
//      auxFrame.setHasJ(jArc != 0);
//
//      return auxFrame;
  }

  public String getDescription()
  {
      return "Построение эквидистанты";
  }

}
