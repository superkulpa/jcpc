package ru.autogenmash.core.utils.compiler;

import java.util.List;

import ru.autogenmash.core.CPList;
import ru.autogenmash.core.CpParameters;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 11.05.2007 12:32:09
 * <p>refactor 20.07.2007
 * <p>refactor2 23.03.2010 */
public interface ICPListBuilder
{

    /** Коэффициент програмной (внутренней) точности. */
    public static final int ACCURACY_FACTOR = 100;

    /** Максимально допустимое значение управляющей команды. */
    public static final int MAX_VALUE = 1000 * 1000;

    /** Минимально допустимое значение управляющей команды. */
    public static final int MIN_VALUE = -1000 * 1000;

    /**Построить CPList на основании "очищенной" УП.
     * <p><b>CpList строится как есть (as is).</b> 
     * <p><b>Важно:</b> После построения, для дальнейшего использования его нужно подготовить.
     * Во всех гео кадрах, обязательно должна присутствовать подготовительная команда G.
     * В кадрах G41, G42 должна присутсвовать D команда. Кадры должны быть автономными.
     * @param source исодная, очищенная от комментариев УП в виде листа строк.
     * @param cpList выходной (построенный) контрл. лист.
     * @param warnings список warning-ов.
     * @return null в случае успеха, иначе ошибку.
     */
    public CompilerError build(final List source, CPList cpList, List warnings);
    
    /**"NnGgXxYyIiJjFfMmTtDdHhLlRrCcKkUu"
     * @return Символы разделители (команды) УП.
     */
    public String getDelimiterChars();
    
    /**Набор билдеров, которые из обычных строк сделают кадры
     * @return
     */
    public ICpSubFrameBuilder[] getFrameBuilders();
    
    public void SetCpParameters(CpParameters _parameters);
}




