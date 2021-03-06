package ru.lsv.finARM.common;

import org.w3c.dom.Document;

import javax.swing.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Набор вспомогательных методов
 */
public class CommonUtils {

    //private static final DecimalFormat df = new DecimalFormat("0.00 ¤");
    private static final NumberFormat df = DecimalFormat.getCurrencyInstance();
    private static final NumberFormat nf = DecimalFormat.getNumberInstance();

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

    /**
     * Форматирование числа
     *
     * @param val Число
     * @return Отформатированное значение
     */
    public static String formatDouble(Double val) {
        if (val == null) return "";
        else return nf.format(val);
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

    /**
     * Дизейблит все контролы у указанного контейнера
     * Дизейблит все до уровня JScrollPane!
     *
     * @param c Контейнер для выключения всех контролов
     */
    public static void disableComponents(java.awt.Container c) {
        Component[] components = c.getComponents();
        for (Component comp : components) {
            if (comp instanceof JScrollPane)
                continue;
            if (comp instanceof java.awt.Container)
                disableComponents((java.awt.Container) comp);
            comp.setEnabled(false);
        }
    }


}
