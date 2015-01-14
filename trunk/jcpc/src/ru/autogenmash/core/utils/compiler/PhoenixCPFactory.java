package ru.autogenmash.core.utils.compiler;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;

public class PhoenixCPFactory extends DefaultCPFactory implements ICPListBuilder{
  
  private static final int Precision = 10;
  private static DefaultCPFactory _instance = new PhoenixCPFactory();
  
  public static DefaultCPFactory getInstance(CpParameters _params)
  {
    _instance.params = _params;
    return _instance; 
  }
  
  protected CompilerError buildCpList(CPList cpList)
  {
    int commandType = CpSubFrame.RC_NULL;
    int frameCount = _frames.size();
    CachedCpFrame[] cpListData = new CachedCpFrame[frameCount];
    for (int i = 0; i < frameCount; i++)
      {
          boolean hasX = false;
          boolean hasY = false;
          boolean hasI = false;
          boolean hasJ = false;
          boolean hasU = false;
          boolean hasZ = false;
          boolean hasA = false;
          boolean hasM = false;
          boolean hasG00 = false;
          boolean hasG01 = false;
          boolean hasG02 = false;
          boolean hasG03 = false;

          final Vector frame = (Vector)_frames.get(i);
          int subFrameCount = frame.size();
          CpSubFrame[] cpSubFrames = new CpSubFrame[subFrameCount];
          for (int j = 0; j < subFrameCount; j++)
          {
              final Vector subFrame = (Vector)frame.get(j);
              int commandCount = subFrame.size();
              CC[] ccs = new CC[commandCount];
              for (int l = 0; l < commandCount; l++)
              {
                  ccs[l] = (CC)subFrame.get(l);
                  switch (ccs[l].getType())
                  {
                  case CC.X: hasX = true; break;
                  case CC.Y: hasY = true; break;
                  case CC.I: hasI = true; break;
                  case CC.J: hasJ = true; break;
                  case CC.Z: hasZ = true; break;
                  case CC.A: 
                  	hasA = true; 
                  	commandType = CpSubFrame.RC_GEO_ROTATE;
                  break;
                  case CC.M: hasM = true; commandType = CpSubFrame.RC_M_COMMAND; break;
                  case CC.T:
                  case CC.SUB:
                  //case CC.S:
                      commandType = CpSubFrame.RC_M_COMMAND; break;
                  case CC.G:
                      switch (ccs[l].getData())
                      {
                      case 0: hasG00 = true; commandType = CpSubFrame.RC_GEO_FAST; break;
                      case 1: hasG01 = true; commandType = CpSubFrame.RC_GEO_LINE; break;
                      
                      case 2: hasG02 = true; commandType = CpSubFrame.RC_GEO_ARC; break;
                      case 3: hasG03 = true; commandType = CpSubFrame.RC_GEO_ARC; break;
                      case 30: commandType = CpSubFrame.RC_G30_COMMAND; break;
                      case CC.INFO: commandType = CpSubFrame.RC_GEO_INFO; break;
                      case 40:
                      case 41:
                      case 42: commandType = CpSubFrame.RC_D_COMMAND; break;
                      default: commandType = CpSubFrame.RC_NULL; break;
                      }
                      break;
                  case CC.F: commandType = CpSubFrame.RC_FEED_COMMAND; break;
                  case CC.K:
                  case CC.D: commandType = CpSubFrame.RC_D_COMMAND; break;
                  case CC.H:
                    if(!hasM)
                      commandType = CpSubFrame.RC_LOOP_COMMAND; 
                  break;
                  case CC.INFO: commandType = CpSubFrame.RC_GEO_INFO; break;
                  //case CC.FCORRECTION: commandType = CpSubFrame.RC_FEED_CORRECTION; break;
                  //case CC.FILTER: commandType = CpSubFrame.RC_FILTER; break;
                  //default: commandType = CpSubFrame.RC_NULL; break;
                  }
              }
              cpSubFrames[j] = new CpSubFrame(commandType, ccs);
          }
          int type = CpFrame.FRAME_TYPE_UNKNOWN;
          if (hasI || hasJ){
              type = CpFrame.FRAME_TYPE_ARC;
          };
          if ( (hasX || hasY || hasU || (hasZ && !hasM)) && !hasI && !hasJ)
          {
             if (hasG00 || hasG01) {
                 type = CpFrame.FRAME_TYPE_LINE;
             }else if (hasG02 || hasG03)
               return new CompilerError(i + 1, "Некорректно определено дуговое перемещение");
          }
          //наклон
          if(hasA) {
//          	if (hasG00 || hasG01)
//          		type = CpFrame.FRAME_TYPE_ROTATE;
//        		else 
//              return new CompilerError(i + 1, "Некорректно определен кадр наклона");	
          }
          
          cpListData[i] = new CachedCpFrame(type, cpSubFrames);

          cpListData[i].setHasX(hasX);
          cpListData[i].setHasY(hasY);
          cpListData[i].setHasI(hasI);
          cpListData[i].setHasJ(hasJ);
          cpListData[i].setHasZ(hasZ);
          cpListData[i].setHasA(hasA);
          
          cpListData[i].setHasM(hasM);
          cpListData[i].setHasG00(hasG00);
          cpListData[i].setHasG01(hasG01);
          cpListData[i].setHasG02(hasG02);
          cpListData[i].setHasG03(hasG03);
      }

      cpList.setData(cpListData);
      cpList.addAuxData(KEY_LOOPS, new Boolean(_hasLoops));
      cpList.addAuxData(KEY_SUBPROGRAMS, new Boolean(_hasSubprograms));
      cpList.addAuxData(KEY_MARKING, new Boolean(_hasMarking));
      cpList.addAuxData(KEY_FULLARCS, new Boolean(_hasFullArcs));
      
      return null;
  }

  protected CompilerError buildPrefatoryCpList(List source, List warnings)
  {
      String paramName = null;
      String valueStr = null;

      //boolean endOfCp = false;
      int g00_03Data = 1;
      int g10_12Data = -1;
      int g40_42Data = -1;// int g41_42Data = -1;
      int g90_900Data = 91;
      int m71_85Data = -1;
      int subData = -1;
      int m91_96Data = -1;
      int m19_45Data = -1;
      int mData = -1;
      Vector tData = new Vector(2);
      //int s = -1;
      Vector rData = new Vector(4);
      Integer d = null;
      int f = -1;
      Integer c = null;
      int hData = 0;
      String lData = null;
      String g59Data = null;
      boolean hasG30 = false;
      boolean hasG59 = false;
      boolean hasG43 = false;
      boolean hasG97 = false;
      
      int g00_03Count = 0;
      int g10_12Count = 0;
      int g40_42Count = 0;
      int g90_900Count = 0;
      int m71_85Count = 0;
      int m91_96Count = 0;
      
      int x = 0, y = 0, u = 0, z = 0, i = 0, j = 0, a = 0;
      boolean hasA = false;
      for (int iterator = 0; iterator < source.size(); iterator++)
      {
          hasG59 = false;
          hasG43 = false;
          hasG97 = false;
          hasA = false;
          _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);
          m71_85Data = -1;
          m91_96Data = -1;
          m19_45Data = -1;
          g90_900Data = -1;
          g40_42Data = -1;
          mData = -1;
          subData = 0;
          f = -1;
          //d = null;
          c = null;
          lData = null;
          hData = 0;
          tData = new Vector(2);
          //s = -1;
          rData = new Vector(4);
          g00_03Count = 0;
          g10_12Count = 0;
          g40_42Count = 0;
          g90_900Count = 0;
          hasG30 = false;
          m71_85Count = 0;
          m91_96Count = 0;
          x = y = u = z = i = j = a = 0;

          //int Precision = Integer.parseInt(params.getValue(Compiler.PARAM_PRECISION_GEO_VALUES).toString());
          //проверяем на FA
          String srcTmp = (String)source.get(iterator);
          if(srcTmp.toUpperCase().indexOf("FA") != -1) {
          	continue;
          };
          
          StringTokenizer st = new StringTokenizer(((String)source.get(iterator)).trim(), getDelimiterChars(), true);
          while (st.hasMoreElements())
          {
              int value = 0;
              paramName = st.nextToken().trim().toUpperCase();
              if (Arrays.binarySearch(COMMAND_NAMES, paramName) < 0)
              {
                  return new CompilerError(iterator + 1,
                      "управляющая команда \"" + paramName + "\" не распознана");
              }

              if (paramName.equals("N"))
              {
                  try
                  {
                      st.nextToken();
                  }
                  catch (NoSuchElementException e)
                  {
                      warnings.add(new CompilerError(iterator + 1, "Кадр имеет неправильную структуру"));
                  }
                  continue;
              }
              else if (paramName.equals("L"))
              {
                  String src = (String)source.get(iterator);
                  int lPosition = src.toUpperCase().indexOf((int)'L');
                  lData = src.substring(lPosition + 1, src.length()).trim();
                  if (mData == 33)
                  {
                      _hasMarking = true;
                      break;
                  }
                  
                  if (lData.indexOf(' ') >= 0)
                      return new CompilerError(iterator + 1, "некорректно задано название подпрограммы " + lData);
                  _hasSubprograms = true;
                  break;
              }
              else if (paramName.equals("R"))
              {
                  //RT команда
                  String src = (String)source.get(iterator);
                  if(src.indexOf("RT") != -1) 
                    continue;
                  if (hasG30 == false)
                      return new CompilerError(iterator + 1, "некорректно задана разводка");
                  src = (String)source.get(iterator);
                  StringTokenizer g30St = new StringTokenizer(src, "Rr", false);
                  g30St.nextToken();
                  while (g30St.hasMoreElements())
                  {
                      String rValue = g30St.nextToken().substring(1);
                      rValue = rValue.replace('+', ' ').trim();

                      try
                      {
                          rData.add(new Integer(rValue));
                      }
                      catch (NumberFormatException e)
                      {
                          //warnings.add(rValueWithPrefix + " ");
                          return new CompilerError(iterator + 1,
                                  "некорректно определена разводка \"" + src + "\"");
                      }
                  }
                  break;
              }
//                else if (endOfCp)
//                {
//                    continue;
//                }
              else
              {
                  try
                  {
                      if(paramName.equals("T")) { //команда RT
                        String src = (String)source.get(iterator);
                        if(src.indexOf("RT") != -1) 
                          continue;
                      };
                      valueStr = st.nextToken().trim();
                      //встретилась команда HS
                      if(valueStr.equals("S")) {
                        continue;
                      }

                  }
                  catch (Throwable e)
                  {
                      if (e instanceof NoSuchElementException)
                      {
                          return new CompilerError(iterator + 1,
                              "не определено значение управляющей команды \"" + paramName + "\"");
                      }
                      else
                          e.printStackTrace();
                  }
                  try
                  {
                    if(paramName.equals("M")) {
                      //проверяем на подтипы
                      int indx = valueStr.indexOf('.');
                      if(indx != -1) {
                        subData = Integer.parseInt(valueStr.substring(indx + 1, valueStr.length()));
                        valueStr = valueStr.substring(0,indx);
                      }
                    };
                    
                    value = (int)Double.parseDouble(valueStr);
                  }
                  catch (NumberFormatException e)
                  {
                      return new CompilerError(iterator + 1,
                          "некорректно определено значение управляющей команды \"" + paramName + "\"");
                  }

                  if ( (value > MAX_VALUE) || (value < MIN_VALUE) )
                  {
                      return new CompilerError(iterator + 1,
                          "значение управляющей команды \"" + paramName + "\" находиться за допустимыми пределами");
                  }
              }
              
              if(paramName.equals("X"))
                if(hasG43)//было g43 то ширина реза, эквидастанта = ширина / 2
                  d = new Integer(Math.round(Float.parseFloat(valueStr) * Precision / 2));
                else
                  x = Math.round(Float.parseFloat(valueStr) * Precision);
              else if (paramName.equals("Y"))
                  y = Math.round(Float.parseFloat(valueStr) * Precision);
              else if (paramName.equals("I"))
                  i = Math.round(Float.parseFloat(valueStr) * Precision);
              else if (paramName.equals("J"))
                  j = Math.round(Float.parseFloat(valueStr) * Precision);
              else if (paramName.equals("A")) {
                  a = Math.round(Float.parseFloat(valueStr) * Precision);
                  hasA = true;
              }else if (paramName.equals("Z"))
                  z = Math.round(Float.parseFloat(valueStr) * Precision);
//                }
              else if (paramName.equals("G"))
              {
                //команды ед.измерения игнорируем
                  if((value == 21) || (value == 20)) {
//                    try
//                    {
//                        st.nextToken();
//                    }
//                    catch (NoSuchElementException e)
//                    {
//                        //warnings.add(new CompilerError(iterator + 1, "Кадр имеет неправильную структуру"));
//                    }
                    continue;
                  }else if(value == 43) {
                    //эквилистанта значение в параметре x
                    hasG43 = true;
                    continue;
                  }else if(value == 97) {
                    hasG97 = true;
                    continue;
                  };
                  
                  //if (Compiler.CORRECT_G_COMMANDS.contains(new Integer(value)) == false)
                  if (Arrays.binarySearch(Compiler.CORRECT_G_COMMANDS, value) < 0)
                      return new CompilerError(iterator + 1, "\"G" + value + "\" недопустимая команда");
                  if(value == 98) {
                    //заменяем на M20
                    mData = 20;
                  }else if (value <= 3 && value >= 0)
                  {
                      if (g00_03Count != 0)
                          return new CompilerError(iterator + 1, "слишком много подготовительных команд (G00 - G03)");
                      g00_03Data = value;
                      g00_03Count++;
                  }
                  else if (value == 40 || value == 41 || value == 42)
                  {
                      if (g40_42Count != 0)
                          return new CompilerError(iterator + 1, "слишком много подготовительных команд (G40 - G42)");
                      //if (value == 40)
                      //{
                      //    //g41_42Data = -1;
                      //    d = null;
                      //}
                          g40_42Data = value;
                      g40_42Count++;
                  }
                  else if (value <= 13 && value >= 10)
                  {
                      if (g10_12Count != 0)
                          return new CompilerError(iterator + 1, "слишком много подготовительных команд (G10 - G13)");
                      if (value == 13)
                          g10_12Data = -1;
                      else
                          g10_12Data = value;
                      g10_12Count++;
                  }
                  else if (value == 90 || value == 91 || value == 900)
                  {
                      if (g90_900Count != 0)
                          return new CompilerError(iterator + 1, "слишком много подготовительных команд (G90,G91,G900)");
                      g90_900Data = value;
                      g90_900Count++;
                  }
                  else if (value == 30)
                  {
                      hasG30 = true;
                  }
                  else if (value == 59)
                  {
                      hasG59 = true;
                      String src = (String)source.get(iterator);
                      int lPosition = src.toUpperCase().indexOf((int)'9');
                      String tmp = src.substring(lPosition + 1, src.length());//.trim();
                      tmp = ConvertInfoData(tmp);
                      if(tmp == "-1") 
                        return new CompilerError(iterator + 1, "неправильный формат данных");
                      if(tmp != "") {
                        if(g59Data != null) g59Data += ",";
                        else g59Data = new String();
                        g59Data += tmp;
                      };
                      while(st.hasMoreElements()) {
                        st.nextToken();
                      };
                      continue;
                  }
                  else
                  {
                      _commands.add(new CC(CC.G, value));
                      _subFrames.add(_commands);
                      _frames.add(_subFrames);
                  }
              }
              else if (paramName.equals("F"))
              {
                  f = value;
              }
              else if (paramName.equals("M"))
              {
                  //if ( Compiler.CORRECT_M_COMMANDS.contains(new Integer(value)) == false)
                  //подменяем вкл/выкл реза
                  if((value == 36) || (value == 37)) {
//                    try{
//                      st.nextToken();
//                    }catch (NoSuchElementException e){  }
                    continue; 
                  }
                  
                  if(value == 7) value = 81;
                  else if(value == 8) value = 83;
                  else if(value == 9) value = 77;
                  else if(value == 10) value = 78;
                  else if((value == 30) || (value == 65)) value = 2;
                  else if(value == 51) value = 46;
                  else if(value == 50) value = 45;
                  if (Arrays.binarySearch(Compiler.CORRECT_M_COMMANDS, value) < 0)
                      return new CompilerError(iterator + 1, "\"M" + value + "\" недопустимая команда");
                  if((value <= 85 && value >= 70) || (value == 30) || (value == 700))
                  {
                      if (m71_85Count != 0)
                        return new CompilerError(iterator + 1, "слишком много M команд (M71 - M83)");
                      if (m91_96Count != 0)
                        return new CompilerError(iterator + 1, "слишком много M команд");
                      m71_85Data = value;
                      m71_85Count++;
                  }
                  else if (value == 19 || value == 45 || value == 46)
                  {
                      m19_45Data = value;
                  }
                  else if (value <= 96 && value >= 91) {
                    if (m71_85Count != 0)
                        return new CompilerError(iterator + 1, "слишком много M команд");
                        if (m91_96Count != 0)
                            return new CompilerError(iterator + 1, "слишком много M команд");
                          m91_96Data = value;
                          m91_96Count++;
                    }else
                        mData = value;
              }
              else if (paramName.equals("T"))
              {
                if(hasG97) {
                  if ((value <= 0) && (m71_85Data == -1) && (m19_45Data == -1))
                    return new CompilerError(iterator + 1, "некорректное значение \"H\" команды");
                  hData = value;
                  _hasLoops = true;
                }else {
                  if(mData > 0)
                    tData.add(new Integer(value));
                };
              }
//                else if (paramName.equals("S"))
//                {
//                    s = value;
//                }
              else if (paramName.equals("D") || paramName.equals("K"))
              {
//                    if ( (g41_42Data == 41 || g41_42Data == 42) == false )
//                        return "Ошибка в " + (iterator + 1) + " кадре: " +
//                        "неопределена подготовительная команда (G41, G42)";
                  if (value > 100 || value < -100)
                      return new CompilerError(iterator + 1, "значение \"D\" команды должно быть в пределах от -10 до 10 мм.");
                  d = new Integer(0/*value * Precision*/);
              }
              else if (paramName.equals("H"))
              {
                  if ((value <= 0) && (m71_85Data == -1) && (m19_45Data == -1))
                      return new CompilerError(iterator + 1, "некорректное значение \"H\" команды");
                  if((m71_85Data != -1) || (m19_45Data != -1))//проверяем а не позиция ли это
                    hData = Math.round(Float.parseFloat(valueStr) * Precision);
                  else
                    hData = value;
                  _hasLoops = true;
              }
              else if (paramName.equals("C"))
              {
                  c = new Integer(value);
              }
          }

          if(hasG59) continue;
          // MAIN BLOCK
          _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);
          //order
          if ((hData > 0) && (m71_85Data == -1) &&  (m19_45Data == -1))
          {
              _commands.add(new CC(CC.H, hData, "H"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (m71_85Data > 0 )
          {
              _commands.add(new CC(CC.M, m71_85Data, "M"));
              //TODO сделать отображение резки на форме исходя из масок
              if (tData.size() > 0 ) {
                int data = 0; int tValue = 0;
                for (int it = 0; it < tData.size(); it++){
                  if((tValue = (1 << (((Integer)tData.get(it)).intValue() - 1))) > 0)
                    data |= tValue; 
                };
                  
                _commands.add(new CC(CC.T, data, "T"));
              };
              _commands.add(new CC(CC.SUB, subData, "SUB"));
              
              if(z != 0) {
                 _commands.add(new CC(CC.Z, z, "Z"));
                 z = 0;
              };
              if(hData != 0) {
                _commands.add(new CC(CC.H, hData, "H"));
                _hasLoops = false;
              };
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (m19_45Data > 0)
          {
            _commands.add(new CC(CC.M, m19_45Data, "M"));
            _subFrames.add(_commands);
            if(z != 0) {
                _commands.add(new CC(CC.Z, z, "Z"));
                z = 0;
             };
             if(hData != 0) {
               _commands.add(new CC(CC.H, hData, "H"));
               _hasLoops = false;
             };
            _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (f > 0)
          {
              _commands.add(new CC(CC.F, f, "F"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (m91_96Data > 0)
          {
              _commands.add(new CC(CC.M, m91_96Data, "M"));
              if (tData.size() > 0 ) {
                int data = 0;
                for (int it = 0; it < tData.size(); it++) 
                  data |= (1 << (((Integer)tData.get(it)).intValue() - 1));
                _commands.add(new CC(CC.T, data, "T"));
              }
              _commands.add(new CC(CC.SUB, subData, "SUB"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (g40_42Data > 0)
          {
              _commands.add(new CC(CC.G, g40_42Data, "G"));
              if (d != null && g40_42Data != 40)
                  _commands.add(new CC(CC.D, (d == null ? 0 : d.intValue())));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (g90_900Data == 900)
          {
              _commands.add(new CC(CC.G, 900, "G"));
              _commands.add(new CC(CC.X, x, "X"));
              _commands.add(new CC(CC.Y, y, "Y"));
              _commands.add(new CC(CC.Z, z, "Z"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (g90_900Data == 90 || g90_900Data == 91)
          {
              _commands.add(new CC(CC.G, g90_900Data, "G"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (hasG30)
          {
              int rDataSize = rData.size();
              if (rDataSize == 0)
                  return new CompilerError(iterator + 1, "не заданы параметры разводки");
              _commands.add(new CC(CC.G, 30, "G"));
              for (int it = 0; it < rDataSize; it++)
                  _commands.add(new CC(CC.R, ((Integer)rData.get(it)).intValue() ));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if(g59Data != null) {
            _commands.add(new CC(CC.G, CC.INFO, g59Data));
            _subFrames.add(_commands);
            _frames.add(_subFrames);
            _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);
            _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
            g59Data = null;
          };
          if (g10_12Data > 0)
          {
              _commands.add(new CC(CC.G, g10_12Data, "G"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (g00_03Data >= 0)
          {
              if (g90_900Data != 900)
              {
                  if ( (x == 0 && y == 0 && (hasA == false) &&/*z == 0 &&*/ i == 0 && j == 0) == false)
                  {
                      if (x == 0 && y == 0 && (hasA == false)/* && z == 0*/)
                          _hasFullArcs = true;

                      _commands.add(new CC(CC.G, g00_03Data, "G"));
                      if (x != 0) _commands.add(new CC(CC.X, x, "X"));
                      if (y != 0) _commands.add(new CC(CC.Y, y, "Y"));
                      if (i != 0) _commands.add(new CC(CC.I, i, "I"));
                      if (j != 0) _commands.add(new CC(CC.J, j, "J"));
                      //наклон
                      if (hasA != false)
                      	if(_commands.size() > 1) {
                      		//добавляем xy
                      		_subFrames.add(_commands);
                      		//добавляем а
                      		_commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
                      		_commands.add(new CC(CC.G, 1, "G"));
                      		_commands.add(new CC(CC.A, a, "A"));
                      	}else
                      		//просто переезд по А
                      	 _commands.add(new CC(CC.A, a, "A"));
                      _subFrames.add(_commands);
                      _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
                  }
              }
          }
          if (mData >= 0)
          {
              if (lData != null)
              {
                  _commands.add(new CC(CC.M, mData, lData));
                  lData = null;
              }
              else
                  _commands.add(new CC(CC.M, mData, "M"));
        
//                if (mData == 19 && s >= 0)
//                    _commands.add(new CC(CC.S, s, "S"));

//                if (mData == 2)
//                    endOfCp = true;

              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (c != null)
          {
              _commands.add(new CC(CC.C, c.intValue(), "marking angle"));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }
          if (lData != null)
          {
              _commands.add(new CC(CC.L, 0, lData));
              _subFrames.add(_commands);
              _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);
          }

          if (_subFrames.size() > 0)
          {
              _frames.add(_subFrames);
              _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);
          }
      }

      return null;
  }
  
  protected PhoenixCPFactory()
  {
    super();
  }
}
