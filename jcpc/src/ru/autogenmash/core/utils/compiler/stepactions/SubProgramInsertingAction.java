package ru.autogenmash.core.utils.compiler.stepactions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import ru.autogenmash.core.CC;
import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CachedCpFrame;
import ru.autogenmash.core.CpParameters;
import ru.autogenmash.core.CpSubFrame;
import ru.autogenmash.core.utils.MTRUtils;
import ru.autogenmash.core.utils.StringUtils;
import ru.autogenmash.core.utils.compiler.Compiler;
import ru.autogenmash.core.utils.compiler.CompilerError;
import ru.autogenmash.core.utils.compiler.DefaultCPFactory;
import ru.autogenmash.core.utils.compiler.ICPListBuilder;

/**Action вставляет подпрограммы из текущей УП или по заданному пути.
 * @author Dymarchuk Dmitry
 * 26.08.2008 14:46:43
 */
public class SubProgramInsertingAction extends StepActionBase
{
    /** Максимальное количество вложенности подпрограмм (ограничитель рекурсии). */
    public static final int MAX_CALL_COUNT = 5;
    
    public static final String AUX_DATA_SUBPROGRAMS = "Subprograms";

    private ICPListBuilder _factory = DefaultCPFactory.getInstance(null);
    //private ICPListBuilder _builder = DefaultCpListBuilder.getInstance();

    private int _recurseCallCount = 1;

    public SubProgramInsertingAction(Compiler compiler, List warnings)
    {
        super(compiler, warnings);
    }

    public CompilerError execute(CPList cpList, CpParameters cpParameters)
    {
        try
        {
            Boolean hasSubprograms = (Boolean)cpList.getAuxData(AUX_DATA_SUBPROGRAMS);
            if (hasSubprograms != null && hasSubprograms.booleanValue() == false)
                return null;
            _factory.SetCpParameters(cpParameters);
        }
        catch (Throwable t)
        {
            String err = "Ошибка при получении вспомогательной информации";
            _log.error(err, t);
            System.err.println("Warning: " + SubProgramInsertingAction.class.getName() + " has some problems (for detail see logs)");
            return new CompilerError(err);
        }

        try
        {
            CompilerError res = processSubPrograms(cpList);
            if (res != null)
                return res;
            if (_recurseCallCount == MAX_CALL_COUNT)
            {
                final String err = "Превышен лимит вложенности подпрограмм";
                _log.error(err);
                return new CompilerError(err);
            }

        }
        catch (Throwable t)
        {
            String err = "Ошибка при обработке подпрограмм";
            _log.error(err, t);
            System.err.println("Warning: " + SubProgramInsertingAction.class.getName() + " has some problems (for detail see logs)");
            return new CompilerError(err);
        }

        return null;
    }

    private HashMap getSubProgramMap(List source)
    {
        boolean isL = false;
        boolean endOfMainCp = false;
        String subProgramName = null;
        Vector subProgramLines = new Vector(20, 20);
        HashMap map = new HashMap();

        Iterator iterator = source.iterator();
        while (iterator.hasNext())
        {
            isL = false;
            String line = (String)iterator.next();
            StringTokenizer st = new StringTokenizer(line, _factory.getDelimiterChars(), true);
            //StringTokenizer st = new StringTokenizer(line, _builder.getDelimiterChars(), true);
            while (st.hasMoreElements())
            {
                String paramName = st.nextToken().toUpperCase();
                if (paramName.equals("M"))
                {
                    String value = st.nextToken();
                    int intValue = Integer.parseInt(value.trim());
                    if (intValue == 2)
                        endOfMainCp = true;
                    else if (intValue == 17)
                    {
                        CPList tmpCpList = new CPList();
                        _factory.build(subProgramLines, tmpCpList, _warnings);
                        //FIXME new: _builder.build(subProgramLines, tmpCpList, _warnings);
                        map.put(subProgramName, tmpCpList);
                        subProgramName = null;
                        subProgramLines.clear();
                    }
                }
                else if (endOfMainCp && paramName.equals("L"))
                {
                    isL = true;
                    int lPosition = line.indexOf((int)'L');
                    subProgramName = line.substring(lPosition + 1, line.length()).trim();
                    continue;
                }
            }

            if (endOfMainCp && isL)
                continue;

            if (endOfMainCp && subProgramName != null)
                subProgramLines.add(line);
        }

        return map;
    }

    private CompilerError processSubPrograms(CPList cpList)
    throws IOException, CloneNotSupportedException
    {
        Vector frames = new Vector(100, 100); // 1000, 1000;
        Vector filtratedCp = getCompiler().getFiltratedCp();
        HashMap subProgramsMapInner = getSubProgramMap(filtratedCp); // карта названий подпрограмм и их исходников (в том же файле)
        HashMap subProgramsMapOuter = new HashMap(); // карта названий подпрограмм и их исходников (в файловой системе)

        String currentDir = null;

        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);
            if (frame.hasM())
            {
                if (frame.contains(CC.M, 2))
                    break;
            }
            CC cc = frame.getCCByType(CC.L);
            if (cc != null)
            {		
            		if(frame.getData().length > 1){
            			CpSubFrame[] cpSubFrames = new CpSubFrame[frame.getData().length -1];
            			for(int indx = 0; indx < frame.getData().length - 1; indx++){
            				cpSubFrames[indx] = frame.getSubFrame(indx);
            			}
            			CachedCpFrame tmp_frame = new CachedCpFrame(frame.getType(), cpSubFrames);
            			tmp_frame.setHasG00(frame.hasG00());
            			tmp_frame.setHasG01(frame.hasG01());
            			tmp_frame.setHasG02(frame.hasG02());
            			tmp_frame.setHasG03(frame.hasG03());
            			frames.add(tmp_frame);
            		};
                String subProgramName = cc.getDescription();
                if (subProgramsMapInner.containsKey(subProgramName)) // in same file
                {
                    CPList tmpCpList = (CPList)subProgramsMapInner.get(subProgramName);
                    MTRUtils.addFrames(frames, ((CPList)tmpCpList.clone()).getData());
                }
                else // if file in filesystem
                {
                    if (currentDir == null)
                        currentDir = StringUtils.getDirectory(getCompiler().getCpFileName());

                    CPList tmpCpList = new CPList();
                    if (subProgramsMapOuter.containsKey(subProgramName))
                        tmpCpList = (CPList)subProgramsMapOuter.get(subProgramName);
                    else
                    {
                        String err = "Ошибка при чтении подпрограммы \"" + subProgramName + "\"";
                        Vector cpLines = new Vector(100, 100);;
                        try
                        {
                            CompilerError res = Compiler.filtrateSource(currentDir + subProgramName, "", cpLines, _warnings);
                            if (res != null)
                                return res;
                        }
                        catch (FileNotFoundException e)
                        {
                            err += " (файл не существует)";
                            _log.error(err, e);
                            return new CompilerError(err);
                        }
                        catch (IOException e)
                        {
                            err += " (файл поврежден или нет доступа на чтение)";
                            _log.error(err, e);
                            return new CompilerError(err);
                        }

                        String lastLine = ((String)cpLines.get(cpLines.size() - 1)).trim().toUpperCase();
                        String[] commands = StringUtils.split(lastLine, " ");
                        if (commands.length > 1)
                            lastLine = commands[commands.length - 1];
                        if (lastLine.indexOf('M') >= 0 && lastLine.endsWith("17"))
                            cpLines.remove(cpLines.size() - 1);

                        CompilerError res = null;
                        res = _factory.build(cpLines, tmpCpList, _warnings);
                        //FIXME new: res = _builder.build(cpLines, tmpCpList, _warnings);
                        if (res != null)
                            return res;
                        subProgramsMapOuter.put(subProgramName, tmpCpList);

                        Boolean hasLoops = (Boolean)tmpCpList.getAuxData(LoopingAction.AUX_DATA_LOOPS);
                        Boolean hasFullArcs = (Boolean)tmpCpList.getAuxData(PrepearingAction.AUX_DATA_FULLARCS);
                        if (hasLoops.equals(Boolean.TRUE))
                            cpList.addAuxData(LoopingAction.AUX_DATA_LOOPS, new Boolean(true));
                        if (hasFullArcs.equals(Boolean.TRUE))
                            cpList.addAuxData(PrepearingAction.AUX_DATA_FULLARCS, new Boolean(true));
                    }
                    MTRUtils.addFrames(frames, ((CPList)tmpCpList.clone()).getData());
                }
            }
            else
                frames.add(frame);
        }

        cpList.setData(frames);

        // второй обход (и последующие)
        for (int i = 0; i < cpList.getLength(); i++)
        {
            CachedCpFrame frame = cpList.getFrame(i);
            if (frame.getCCByType(CC.L) != null)
            {
                if (_recurseCallCount == MAX_CALL_COUNT)
                    return null;

                _recurseCallCount++;
                CompilerError res = processSubPrograms(cpList); // рекурсия
                if (res != null)
                    return res;
            }
        }

        return null;
    }

    public String getDescription()
    {
        return "Обработка подпрограмм";
    }


}
