package ru.autogenmash.core.utils.compiler;

import java.util.List;

import ru.autogenmash.core.CPList;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 11.05.2007 12:32:09
 * <p>refactor 20.07.2007
 */
public interface ICPFactory
{

    // TODO сделать динамическое формирование DELIMITERS на основании CC.class.getFields() ....
    /** Символы разделители (команды) УП. */
    public static final String DELIMITERS = "NnGgXxYyIiJjFfMmTtDdHhLlRr";
    //public static final String DELIMITERS = "NnGgXxYyIiJjFfMmTtDdZzHhLlRrUuCcWwVvSs";

    /** Коэффициент програмной (внутренней) точности. */
    public static final int ACCURACY_FACTOR = 100;

    /** Максимально допустимое значение управляющей команды. */
    public static final int MAX_VALUE = 1000 * 1000;

    /** Минимально допустимое значение управляющей команды. */
    public static final int MIN_VALUE = -1000 * 1000;

    /**Построить CPList на основании "очищенной" УП.
     * @param source исодная, очищенная от комментариев УП в виде листа строк.
     * @param cpList выходной (построенный) контрл. лист.
     * @param warnings список warning-ов.
     * @return null в случае успеха, иначе ошибку.
     */
    public CompilerError build(final List source, CPList cpList, List warnings);
}
