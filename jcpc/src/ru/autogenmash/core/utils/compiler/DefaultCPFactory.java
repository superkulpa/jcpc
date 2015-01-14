package ru.autogenmash.core.utils.compiler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.collections.map.HashedMap;
//import org.apache.commons.net.nntp.NewsgroupInfo;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;

/**Вообще, это не фабрика, а билдер, но название исторически сложилось.
 * Создает CPList из уп-файла.
 * @author Dymarchuk Dmitry
 * @version
 * 11.05.2007 12:58:13
 */
public class DefaultCPFactory implements ICPListBuilder
{
    /** Количество кадров (при инициализации). */
    public static final int СAPACITY_FRAMES = 100;
    /** Количество подкадров в кадре (при инициализации). */
    public static final int СAPACITY_SUBFRAMES = 4;
    /** Количество управляющих команд в подкадре (при инициализации). */
    public static final int СAPACITY_COMMANDS = 5;

    public static final String[] COMMAND_NAMES;

    public static final String KEY_LOOPS = "Loops";
    public static final String KEY_SUBPROGRAMS = "Subprograms";
    public static final String KEY_MARKING = "Marking";
    public static final String KEY_FULLARCS = "Fullarcs";

    public CpParameters params;
    
    private static DefaultCPFactory _instance = new DefaultCPFactory();

    protected Vector _frames;
    protected Vector _subFrames;
    protected Vector _commands;

    protected boolean _hasLoops = false;
    protected boolean _hasSubprograms = false;
    protected boolean _hasMarking = false;
    protected boolean _hasFullArcs = false;

    private Map materials = new HashMap();
    private String cur_material;
    
    private Map work = new HashMap();
    private String cur_work;
    
    private Map gases = new HashMap();
    private String cur_gas;
    
    private Map thicks = new HashMap();
    private String cur_thick;
    
    public void SetCpParameters(CpParameters _parameters) {
        params = _parameters;
    };
    
    static
    {
        Field[] fields = CC.class.getFields();

        Vector tmpCommandNames = new Vector(40);
        for (int i = 0; i < fields.length; i++)
        {
            int value = 0;
            try
            {
                value = fields[i].getInt(null);
            }
            catch (Throwable t)
            {
                continue;
            }
            if (value < 100 || value > 200)
                continue;
            else
                tmpCommandNames.add(fields[i].getName());
        }

        COMMAND_NAMES = (String[])tmpCommandNames.toArray(new String[0]);

        Arrays.sort(COMMAND_NAMES);
    }


    protected DefaultCPFactory()
    {
      CreateInfoMap();
    }

    public static DefaultCPFactory getInstance(CpParameters _params)
    {
      _instance.params = _params;
      return _instance; 
    }

    public CompilerError build(List source, CPList cpList, List warnings)
    {
        _hasLoops = false;
        _hasSubprograms = false;
        _hasMarking = false;
        _hasFullArcs = false;
    	
        _frames = new Vector(СAPACITY_FRAMES, СAPACITY_FRAMES);
        _subFrames = new Vector(СAPACITY_SUBFRAMES, СAPACITY_SUBFRAMES);
        _commands = new Vector(СAPACITY_COMMANDS, СAPACITY_COMMANDS);

        CompilerError result = buildPrefatoryCpList(source, warnings);
        if (result != null)
            return result;

        result = buildCpList(cpList);
        if (result != null)
            return result;

        return null;
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

            cpListData[i] = new CachedCpFrame(type, cpSubFrames);

            cpListData[i].setHasX(hasX);
            cpListData[i].setHasY(hasY);
            cpListData[i].setHasI(hasI);
            cpListData[i].setHasJ(hasJ);
            cpListData[i].setHasZ(hasZ);
            
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
        int d = Integer.parseInt(params.getValue(Compiler.PARAM_KERFING_D).toString());
        int f = -1;
        Integer c = null;
        int hData = 0;
        String lData = null;
        String g59Data = null;
        boolean hasG30 = false;
        boolean hasG59 = false;
        int g00_03Count = 0;
        int g10_12Count = 0;
        int g40_42Count = 0;
        int g90_900Count = 0;
        int m71_85Count = 0;
        int m91_96Count = 0;
        int x = 0, y = 0, u = 0,z = 0, i = 0, j = 0;

        for (int iterator = 0; iterator < source.size(); iterator++)
        {
            hasG59 = false;
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
            x = y = u = z = i = j = 0;

            //int Precision = Integer.parseInt(params.getValue(Compiler.PARAM_PRECISION_GEO_VALUES).toString());
            
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
                    if (hasG30 == false)
                        return new CompilerError(iterator + 1, "некорректно задана разводка");
                    String src = (String)source.get(iterator);
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
                        valueStr = st.nextToken().trim();
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
                      
                      value = (int)Double.parseDouble(valueStr);// Integer.parseInt(valueStr);
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
                    x = (int)(Double.parseDouble(valueStr) /** Precision*/);
                else if (paramName.equals("Y"))
                    y = (int)(Double.parseDouble(valueStr) /** Precision*/);
                else if (paramName.equals("I"))
                    i = (int)(Double.parseDouble(valueStr) /** Precision*/);
                else if (paramName.equals("J"))
                    j = (int)(Double.parseDouble(valueStr) /** Precision*/);
//                else if (paramName.equals("U")){
//                    u = (int)(Double.parseDouble(valueStr) /** Precision*/);
                else if (paramName.equals("Z"))
                    z = (int)(Double.parseDouble(valueStr));
//                }
                else if (paramName.equals("G"))
                {
                    //if (Compiler.CORRECT_G_COMMANDS.contains(new Integer(value)) == false)
                    if (Arrays.binarySearch(Compiler.CORRECT_G_COMMANDS, value) < 0)
                        return new CompilerError(iterator + 1, "\"G" + value + "\" недопустимая команда");

                    if (value <= 3 && value >= 0)
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
                        if(g59Data != null) g59Data += ",";
                        else g59Data = new String();
                        g59Data += tmp;
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
                    tData.add(new Integer(value));
                }
//                else if (paramName.equals("S"))
//                {
//                    s = value;
//                }
                else if (paramName.equals("D"))
                {
//                    if ( (g41_42Data == 41 || g41_42Data == 42) == false )
//                        return "Ошибка в " + (iterator + 1) + " кадре: " +
//                        "неопределена подготовительная команда (G41, G42)";
                    if (value > 100 || value < -100)
                        return new CompilerError(iterator + 1, "значение \"D\" команды должно быть в пределах от -10 до 10 мм.");
                    d = value;
                }
                else if (paramName.equals("H"))
                {
                    if ((value <= 0) && (m71_85Data == -1) && (m19_45Data == -1))
                        return new CompilerError(iterator + 1, "некорректное значение \"H\" команды");
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
                if (d != 0 && g40_42Data != 40)
                    _commands.add(new CC(CC.D, d));
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
                    if ( (x == 0 && y == 0 && u == 0 &&/*z == 0 &&*/ i == 0 && j == 0) == false)
                    {
                        if (x == 0 && y == 0 && u == 0/* && z == 0*/)
                            _hasFullArcs = true;

                        _commands.add(new CC(CC.G, g00_03Data, "G"));
                        if (x != 0) _commands.add(new CC(CC.X, x, "X"));
                        if (y != 0) _commands.add(new CC(CC.Y, y, "Y"));
                        if (i != 0) _commands.add(new CC(CC.I, i, "I"));
                        if (j != 0) _commands.add(new CC(CC.J, j, "J"));
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
    
    protected String ConvertInfoData(String _tmp) {
       _tmp = _tmp.toUpperCase();
      int posV = _tmp.indexOf((int)'V');
      int posF = _tmp.indexOf((int)'F');
      //String data = new String("-1");
      //int typeInfo = (int)(Double.parseDouble(_tmp.substring(posV + 1,posF)));
      //data = _tmp.substring(posF + 1);
//      switch(typeInfo) {
//        case 503:{
//          int pos = data.indexOf((int)'.');
//          String subtype = new String();
//          if(pos != -1) {
//            subtype = data.substring(pos + 1);
//            data = data.substring(0, pos);
//          };
//          String steel = (String)materials.get(data);
//          String type_torch = (String)work.get(subtype);
//          if(steel != null)
//            cur_material = steel;
//          if(type_torch != null)
//            cur_work = type_torch;
//          else
//            cur_work = "";
//          data = "Keys/MetallType=" + cur_material + ",Keys/Descr=" + cur_work;
//        }
//        break;
//        case 504:
//          data = "Keys/Power=" + data;
//        break;
//        case 505:{
//          data = (String)gases.get(data);
//          if(data != null)
//            cur_gas = data;
//          else
//            data = cur_gas;
//          data = "Keys/GasTypes=" + data;
//        };
//        break;
//        case 507:
//          data = (String)thicks.get(data);
//          if(data != null)
//            cur_thick = data;
//          else  
//            data = cur_thick;
//          data = "Keys/Thickness=" + data;
//        break;
//        //не используемые
//        case 502:
//        case 506:
//        case 600:
//        	data = "Common/SVRVoltage=" + data;
//        case 601:
//        	data = "Common/BurningTime=" + data;
//        case 602:
//        	data = "Common/BurningTime=" + data;
//        case 603:
//        case 604:
//           data = "";
//        break;
//        default:
//        return "-1";
//      };
//     return data;
      return _tmp.substring(posV + 1,posF - 1) + "=" + _tmp.substring(posF + 1); 
    }

    private void CreateInfoMap() {
      //type material
      materials.put("1", "Mild_Steel");
      materials.put("2", "Stainless_Steel");
      materials.put("3", "Al");
      //type work
      work.put("98", "TH");
      //type gases
      gases.put("1","Air/Air");
      gases.put("2","O2/Air");
      gases.put("3","O2/O2");
      gases.put("5","N2/Air");
      gases.put("6","None/N2");
      gases.put("7","O2/N2");
      gases.put("8","CH4/N2");
      gases.put("9","H35/N2");
      gases.put("10","H5/N2");
      gases.put("11","Air/N2");
      gases.put("13","CO2/N2");
      gases.put("14","None/Air");
      gases.put("15","CH4/Air");
      gases.put("16","O2-N2/Air");
      gases.put("17","O2-N2/O2");
      gases.put("18","O2");
      gases.put("19","N2");
      gases.put("21","Air");
      gases.put("22","F5/N2");
      gases.put("23","H35&N2/N2");
      //thick
      thicks.put("2","0.35");
      thicks.put("3","0.35");
      thicks.put("4","0.4");
      thicks.put("5","0.4");
      thicks.put("6","0.5");
      thicks.put("7","0.5");
      thicks.put("8","0.6");
      thicks.put("9","0.6");
      thicks.put("10","0.8");
      thicks.put("11","0.8");
      thicks.put("12","0.9");
      thicks.put("13","0.9");
      thicks.put("14","1");
      thicks.put("15","1.2");
      thicks.put("16","1.2");
      thicks.put("17","1.5");
      thicks.put("18","1.5");
      thicks.put("19","1.6");
      thicks.put("20","2");
      thicks.put("21","2");
      thicks.put("22","2.4");
      thicks.put("23","2.5");
      thicks.put("24","2.5");
      thicks.put("25","3.2");
      thicks.put("26","3.5");
      thicks.put("27","3.5");
      thicks.put("28","4.8");
      thicks.put("29","6");
      thicks.put("30","8");
      thicks.put("31","10");
      thicks.put("32","11");
      thicks.put("33","12");
      thicks.put("34","14");
      thicks.put("35","15");
      thicks.put("36","20");
      thicks.put("37","22");
      thicks.put("38","25");
      thicks.put("39","30");
      thicks.put("40","32");
      thicks.put("41","35");
      thicks.put("42","38");
      thicks.put("43","45");
      thicks.put("44","50");
      thicks.put("45","60");
      thicks.put("46","65");
      thicks.put("47","2.2");
      thicks.put("48","3");
      thicks.put("49","3.8");
      thicks.put("50","4.5");
      thicks.put("51","5.5");
      thicks.put("52","4");
      thicks.put("53","5");
      thicks.put("54","40");
      thicks.put("55","48");
      thicks.put("56","55");
      thicks.put("57","70");
      thicks.put("58","75");
      thicks.put("59","80");
      thicks.put("60","85");
      thicks.put("61","90");
      thicks.put("62","95");
      thicks.put("63","100");
    };
    
    public ICpSubFrameBuilder[] getFrameBuilders()
    {
        return null;
    }

    public String getDelimiterChars()
    {
        return "NnGgXxYyIiJjFfMmTtDdHhLlRrCcKkUuVvZzAa";
    }
}









