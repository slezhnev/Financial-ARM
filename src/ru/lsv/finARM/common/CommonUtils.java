package ru.lsv.finARM.common;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Набор вспомогательных методов
 */
public class CommonUtils {

    private static final DecimalFormat df = new DecimalFormat("0.00 ¤");

    /**
     * Форматирование денежной суммы
     *
     * @param val Значение для форматирования
     * @return Отформатированное значение
     */
    public static String formatCurrency(Double val) {
        if (val == null) return "";
        else return df.format(val);
    }

    private static String[] monthNames = new DateFormatSymbols(new Locale("RU", "ru")).getMonths();

    /**
     * Возвращает имя месяца по его номеру
     *
     * @param index номер месяца (январь - 0 и т.п.)
     * @return Имя месяца в локали RU/ru
     * @throws IndexOutOfBoundsException Вслучае неверного указания индекса месяца
     */
    public static String getMonthNameByIndex(int index) throws IndexOutOfBoundsException {
        if ((index >= 0) && (index < monthNames.length)) {
            return monthNames[index];
        } else {
            throw new IndexOutOfBoundsException("Неверно указан номер месяца");
        }
    }

    /**
     * Сохраняет сформированный XML-файл
     *
     * @param file Куда сохранять
     * @param doc  Сформированный документ
     * @throws javax.xml.transform.TransformerException
     *                             см. @javax.xml.transform.Transformer
     * @throws java.io.IOException В случае ошибок ввода-вывода при формировании и сохранении XML
     */
    public static void saveXML(File file, Document doc) throws TransformerException, IOException {
        DOMSource domSource = new DOMSource(doc);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        transformer = tf.newTransformer();
        //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(domSource, new StreamResult(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")));
        //transformer.transform(domSource, new StreamResult(new FileWriter(file)));
    }


}
