package ru.autogenmash.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import ru.autogenmash.core.exceptions.InvalidFileFormatException;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 21.09.2007 14:45:35
 */
public class XmlUtils
{

    public static final String XML_SECTION_PARAMETERS = "parameters";
    public static final String XML_PARAMETER_NAME = "parameter";
    public static final String XML_SECTION_ERRORS = "errors";
    public static final String XML_ERROR_NAME = "error";
    public static final String XML_ATTRIBUTE_NAME = "name";
    public static final String XML_ATTRIBUTE_VALUE = "value";

    public static final String MSG_WRONG_FILE = "Выбранный файл параметров имеет неверный формат";
    public static final String MSG_FILE_IS_EMPTY = "Выбранный файл параметров пуст";
    public static final String MSG_FILE_EXISTS = "Файл с таким именем уже существует";
    public static final String MSG_WRONG_OR_DAMAGED_FILE = "Выбранный файл параметров имеет неверный формат или поврежден";
    public static final String MSG_FILE_NOT_FOUND = "Выбранный файл параметров отсутствует";
    public static final String MSG_FILE_IO_ERROR = "Ошибка ввода/вывода при чтении файл параметров";
//    public static final String MSG_INVALID_INPUT_PARAMETER = "Некорректно задан входной параметр";

    public static final String PARAMS_FILE_EXTENSION = ".xml";

    public static void readParameters2(String xmlFileName, Map params)
    throws JDOMException, IOException, InvalidFileFormatException
    {
        FileInputStream xml = new FileInputStream(xmlFileName);
        SAXBuilder in = new SAXBuilder();
        Document document = new Document();
        document = in.build(xml);
        xml.close();
        if (document == null || document.getRootElement() == null ||
                document.getRootElement().getChild(XML_SECTION_PARAMETERS) == null)
            throw new InvalidFileFormatException(MSG_WRONG_FILE);

        List list = document.getRootElement().getChild(XML_SECTION_PARAMETERS).getChildren();
        for (int i = 0; i < list.size(); i++)
        {
            String name = ((Element)list.get(i)).getAttributeValue(XML_ATTRIBUTE_NAME);
            String value = ((Element)list.get(i)).getAttributeValue(XML_ATTRIBUTE_VALUE);
            params.put(name, value);
        }
    }

    /**@deprecated use readParameters2 instead
     */
    public static String readParameters(String xmlFileName, Map params)
    {
        //long time = new Date().getTime();

        try
        {
            //Reader xml = new BufferedReader(new InputStreamReader(new FileInputStream(xmlFileName), "UTF8"));
            FileInputStream xml = new FileInputStream(xmlFileName);
            SAXBuilder in = new SAXBuilder();
            Document document = new Document();
            document = in.build(xml);
            xml.close();
            if (document == null || document.getRootElement() == null ||
                    document.getRootElement().getChild(XML_SECTION_PARAMETERS) == null)
                return MSG_WRONG_FILE;

            java.util.List list = document.getRootElement().getChild(XML_SECTION_PARAMETERS).getChildren();
            for (int i = 0; i < list.size(); i++)
            {
                String name = ((Element)list.get(i)).getAttributeValue(XML_ATTRIBUTE_NAME);
                String value = ((Element)list.get(i)).getAttributeValue(XML_ATTRIBUTE_VALUE);
                params.put(name, value);
            }
        }
        catch (FileNotFoundException e)
        {
            return MSG_FILE_NOT_FOUND;
        }
        catch (JDOMException e)
        {
            return MSG_WRONG_OR_DAMAGED_FILE;
        }
        catch (IOException e)
        {
            return MSG_FILE_IO_ERROR;
        }

        //Utils.traceTime(time, "reading parameters from xml");

        return null;
    }

    public static void writeParameters2(String xmlFileName, Map params, String rootElementName, boolean _isError)
    throws IOException
    {
        if (params.size() <= 0)
            throw new IllegalArgumentException("Карта входных параметров пуста");
        String sectName = XML_SECTION_PARAMETERS;
        String paramName = XML_PARAMETER_NAME;
        if(_isError) {
          sectName = XML_SECTION_ERRORS;
          paramName = XML_ERROR_NAME;
        }
        Element macros = new Element(rootElementName);
        Document document = new Document(macros);
        Element parameters = new Element(sectName);
        macros.addContent(parameters);

        Object[] keys = params.keySet().toArray();
        for (int i = 0; i < keys.length; i++)
        {
            Element element = new Element(paramName).
                setAttribute(XML_ATTRIBUTE_NAME, (String)keys[i]).
                setAttribute(XML_ATTRIBUTE_VALUE, (String)params.get(keys[i]));
            parameters.addContent(element);
        }

        String directory = StringUtils.getDirectory(xmlFileName);
        if (directory != null)
        {
            File file = new File(directory);
            if (file.isDirectory() == false)
            {
                if (file.mkdir() == true)
                    System.out.println("mkdir " + directory);
                else
                    System.err.println("failed to mkdir " + directory);
            }
        }
        FileOutputStream xml = new FileOutputStream(xmlFileName);
        new XMLOutputter(Format.getPrettyFormat()).output(document, xml);
        xml.close();
    }

    /**@deprecated use writeParameters2 instead
     */
    public static String writeParameters(String xmlFileName, Map params, String rootElementName)
    {
        try
        {
            if (params.size() <= 0)
                return "Карта входных параметров пуста";

            Element macros = new Element(rootElementName);
            Document document = new Document(macros);
            Element parameters = new Element(XML_SECTION_PARAMETERS);
            macros.addContent(parameters);

            Object[] keys = params.keySet().toArray();
            for (int i = 0; i < keys.length; i++)
            {
                Element element = new Element(XML_PARAMETER_NAME).
                    setAttribute(XML_ATTRIBUTE_NAME, (String)keys[i]).
                    setAttribute(XML_ATTRIBUTE_VALUE, (String)params.get(keys[i]));
                parameters.addContent(element);
            }

            String directory = StringUtils.getDirectory(xmlFileName);
            if (directory != null)
            {
                File file = new File(directory);
                if (file.isDirectory() == false)
                {
                    if (file.mkdir() == true)
                        System.out.println("mkdir " + directory);
                    else
                        System.err.println("failed to mkdir " + directory);
                }
            }
            FileOutputStream xml = new FileOutputStream(xmlFileName);
            new XMLOutputter(Format.getPrettyFormat()).output(document, xml);
            xml.close();
        }
        catch (FileNotFoundException e)
        {
            return MSG_FILE_NOT_FOUND;
        }
        catch (IOException e)
        {
            return MSG_FILE_IO_ERROR;
        }
//        catch (ClassCastException e)
//        {
//            return MSG_INVALID_INPUT_PARAMETER;
//        }

        return null;
    }

//    public static String appendParameters(String xmlFileName, Map params, String rootElementName)
//    {
//        try
//        {
//            FileInputStream inXml = new FileInputStream(xmlFileName);
//            SAXBuilder in = new SAXBuilder();
//            Document document = new Document();
//            document = in.build(inXml);
//            inXml.close();
//            if (document == null || document.getRootElement() == null)
//                return MSG_WRONG_FILE;
//
//            Element rootElement = document.getRootElement();
//            if (rootElement.getName().equalsIgnoreCase(rootElementName) == false)
//            {
//                rootElement = new Element(rootElementName);
//                document.addContent(rootElement);
//            }
//
//            Object[] keys = params.keySet().toArray();
//            for (int i = 0; i < keys.length; i++)
//            {
//                Element element = new Element(XML_PARAMETER_NAME).
//                    setAttribute(XML_ATTRIBUTE_NAME, (String)keys[i]).
//                    setAttribute(XML_ATTRIBUTE_VALUE, (String)params.get(keys[i]));
//                rootElement.addContent(element);
//            }
//            FileOutputStream outXml = new FileOutputStream(xmlFileName);
//            new XMLOutputter(Format.getPrettyFormat()).output(document, outXml);
//            outXml.close();
//        }
//        catch (FileNotFoundException e)
//        {
//            return MSG_FILE_NOT_FOUND;
//        }
//        catch (JDOMException e)
//        {
//            return MSG_WRONG_OR_DAMAGED_FILE;
//        }
//        catch (IOException e)
//        {
//            return MSG_FILE_IO_ERROR;
//        }
//
//        return null;
//    }
}
