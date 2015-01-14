package ru.autogenmash.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 18.04.2007 13:26:31
 */
public class StringUtils
{

    public static String checkForNull(String str)
    {
        if (str == null)
            return "";
        else if (str.trim().equalsIgnoreCase("null"))
            return "";
        else
            return str;
    }

    /**Important: Only for files
     * @param fileName
     * @return
     */
    public static String getDirectory(String fileName)
    {
        for (int i = fileName.length(); i > 0; i--)
        {
            char c = fileName.charAt(i - 1);
            if (c == '/' || c == '\\')
                return fileName.substring(0, i);
        }
        return null;
    }

    /**Добавить конечный слеш, если его нет.
     * @param dirName
     * @return
     */
    public static String extractDirectoryName(String dirName)
    {
        if (dirName.charAt(dirName.length()) == '/')
            return dirName;
        else
            return dirName + '/';
    }

    /**Получить имя файла. Например fileName="somefile.txt" return "somefile".
     * @param fileName
     * @return Имя файла. Если имя файла задано не корретно (например "somefile"), то вернуть null.
     */
    public static String getNameOfFile(String fileName)
    {
        int position = fileName.lastIndexOf('.');
        if (position == -1)
            return null;
        else
            return fileName.substring(0, position - 1);
    }

    /**Получить расширенние файла. Например fileName="somefile.txt" return "txt".
     * @param fileName
     * @return Расширение файла. Если имя файла задано не корретно (например "somefile"),
     * то вернуть null.
     */
    public static String getExtensionOfFile(String fileName)
    {
        int position = fileName.lastIndexOf('.');
        if (position == -1)
            return null;
        else
            return fileName.substring(position);
    }

    /**Дополняет спереди номер number fillChar символами так, чтобы длина получившейся строки была
     * равна length.
     * @param number
     * @param length
     * @param fillChar
     * @return
     */
    public static String extractNumber(int number, int length, char fillChar)
    {
        String numberStr = String.valueOf(number);
        String res = "";
        for (int i = 0; i < length - numberStr.length(); i++)
        {
            res += fillChar;
        }
        return res + number;
    }

    /**
     * @param value
     * @return
     */
    public static boolean isNumber(final String value)
    {
        int commaCount = 0; // количество запятых
        int pointCount = 0; // количество точек

        for (int i = 0; i < value.length(); i++)
        {
            if ( (value.charAt(i) == '-') && (i > 0) )
                return false;

            if (value.charAt(i) == ',')
                commaCount++;

            if (value.charAt(i) == '.')
                pointCount++;

            if ( ((int)value.charAt(i) < 44) || ((int)value.charAt(i) > 57) || ((int)value.charAt(i) == 47) )
                return false;
        }

        if ( (commaCount > 1) || (pointCount > 1) || ( (commaCount == 1)&&(pointCount == 1) ) )
            return false;

        return true;
    }

    public static String[] split(final String source, final String delimiter)
    {
        StringTokenizer st = new StringTokenizer(source, delimiter);
        ArrayList res = new ArrayList();
        //String[] res = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens())
        {
            //res[i] = st.nextToken();
            res.add(st.nextToken());
            i++;
        }
        return (String[])res.toArray(new String[0]);
    }

    public static String getClassName(Class clazz)
    {
        String className = clazz.getName();
        int pos = className.lastIndexOf('.');
        return className.substring(pos + 1);
    }

    private static HashMap _translitChars;

    public static String translit(char ch)
    {
        if (_translitChars == null)
        {
            _translitChars = new HashMap();
            _translitChars.put(new Character('А'), "A");
            _translitChars.put(new Character('Б'), "B");
            _translitChars.put(new Character('В'), "V");
            _translitChars.put(new Character('Г'), "G");
            _translitChars.put(new Character('Д'), "D");
            _translitChars.put(new Character('Е'), "E");
            _translitChars.put(new Character('Ё'), "E");
            _translitChars.put(new Character('Ж'), "Zh");
            _translitChars.put(new Character('З'), "Z");
            _translitChars.put(new Character('И'), "I");
            _translitChars.put(new Character('Й'), "I");
            _translitChars.put(new Character('К'), "K");
            _translitChars.put(new Character('Л'), "L");
            _translitChars.put(new Character('М'), "M");
            _translitChars.put(new Character('Н'), "N");
            _translitChars.put(new Character('О'), "O");
            _translitChars.put(new Character('П'), "P");
            _translitChars.put(new Character('Р'), "R");
            _translitChars.put(new Character('С'), "S");
            _translitChars.put(new Character('Т'), "T");
            _translitChars.put(new Character('У'), "U");
            _translitChars.put(new Character('Ф'), "F");
            _translitChars.put(new Character('Х'), "H");
            _translitChars.put(new Character('Ц'), "Ts");
            _translitChars.put(new Character('Ч'), "Ch");
            _translitChars.put(new Character('Ш'), "Sh");
            _translitChars.put(new Character('Щ'), "Sch");
            _translitChars.put(new Character('Ъ'), "'");
            _translitChars.put(new Character('Ы'), "Y");
            _translitChars.put(new Character('Ь'), "'");
            _translitChars.put(new Character('Э'), "E");
            _translitChars.put(new Character('Ю'), "Yu");
            _translitChars.put(new Character('Я'), "Ya");


            HashMap translitLowerChars = new HashMap();
            Iterator iterator = _translitChars.keySet().iterator();
            while (iterator.hasNext())
            {
                Character key = (Character)iterator.next();
                String value = (String)_translitChars.get(key);
                translitLowerChars.put(
                        new Character(Character.toLowerCase(key.charValue())),
                        value.toLowerCase());
            }
            _translitChars.putAll(translitLowerChars);

        }

        Character character = new Character(ch);

        if (_translitChars.containsKey(character))
            return (String)_translitChars.get(character);
        else
            return "" + ch;
    }

    public static String translit(String string)
    {
        String result = "";
        for (int i = 0; i < string.length(); i++)
        {
            char ch = string.charAt(i);
            result += translit(ch);
        }

        return result;
    }

    public static String capitalizeFirstLetter(String word)
    {
        if (word == null || word.trim().equals(""))
            return word;

        char firstChar = Character.toUpperCase(word.charAt(0));

        return firstChar + word.substring(1);
    }

    /**Не учитывается моноширность шрифтов!
     * @param word
     * @param newLength
     * @return
     */
    public static String format(String word, int newLength)
    {
        if (word.length() > newLength)
            return word.substring(0, newLength - 3) + "...";
        else if (word.length() == newLength)
            return word;
        else
        {
            int spaceLength = newLength - word.length();
            String spaces = "";
            for (int i = 0; i < spaceLength; i++)
                spaces += " ";
            return word + spaces;
        }
    }

    public static String toString(Object[] a)
    {
        if (a == null)
            return "null";

        int iMax = a.length - 1;
        if (iMax == -1)
            return "[]";

        String b = "";
        b += '[';
        for (int i = 0;; i++)
        {
            b += String.valueOf(a[i]);
            if (i == iMax)
                return b += ']';
            
            b += ", ";
        }
    }
}


















