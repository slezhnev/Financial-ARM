package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.FinancialOperation;
import ru.lsv.finARM.mappings.MonthSpending;
import ru.lsv.finARM.ui.MainForm;
import ru.lsv.finARM.ui.ReportViewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Отчет по расходам
 */
public class PlannedMonthSpendingReport {
    /**
     * Формирование отчета
     *
     * @param frameComp    см. @ru.lsv.finARM.ui.ReportViewer
     * @param locationComp см. @ru.lsv.finARM.ui.ReportViewer
     * @param period       За какой период он формируется
     * @param operations   Список финансовых операций за этот период
     * @param timeParams   Параметры временного выделения
     * @throws java.io.IOException В случае ошибок ввода-вывода при формировании и сохранении XML
     * @throws javax.xml.parsers.ParserConfigurationException
     *                             см. @javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
     * @throws javax.xml.transform.TransformerException
     *                             см. @javax.xml.transform.Transformer
     * @throws org.hibernate.HibernateException
     *                             В случае происхождения чего-нито при работе с Hibernate
     */
    public void makeReport(Component frameComp,
                           Component locationComp,
                           String period, java.util.List<FinancialOperation> operations,
                           MainForm.TimeFilterParams timeParams) throws ParserConfigurationException, IOException, TransformerException, HibernateException {
        // Формируем XML с отчетом...
        File file = new File("PlannedMonthSpending.toReport");
        DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document doc = impl.createDocument(null, null, null);

        Element root = doc.createElement("spendings");
        root.setAttribute("period", period);
        doc.appendChild(root);
        Element e1 = doc.createElement("spendingsList");
        root.appendChild(e1);
        //
        // Заготовки для хранения общих сумм плановых и внеплановых
        HashMap<MonthSpending, Double> planned = new HashMap<MonthSpending, Double>();
        HashMap<String, Double> nonPlanned = new HashMap<String, Double>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        // Поехали по всем операциям
        for (FinancialOperation op : operations) {
            if (op.getKind() == 2) {
                Element e2 = doc.createElement("spend");
                // Нашли расход - поехали над ним медитировать
                e2.setAttribute("amount", CommonUtils.formatDouble(op.getOperationSum()));
                e2.setAttribute("date", sdf.format(op.getOperationDate()));
                if (op.getPlannedSpending() == null) {
                    // Внеплановый расход
                    e2.setAttribute("name", op.getNonPlannedSpending());
                    e2.setAttribute("planned", "" + 0.0);
                    e2.setAttribute("remainingAmount", "" + 0.0);
                    if (nonPlanned.containsKey(op.getNonPlannedSpending())) {
                        nonPlanned.put(op.getNonPlannedSpending(), nonPlanned.get(op.getNonPlannedSpending()) + op.getOperationSum());
                    } else {
                        nonPlanned.put(op.getNonPlannedSpending(), op.getOperationSum());
                    }
                } else {
                    // Плановый расход
                    e2.setAttribute("name", op.getPlannedSpending().getName());
                    e2.setAttribute("planned", CommonUtils.formatDouble(op.getPlannedSpending().getAmount()));
                    if (planned.containsKey(op.getPlannedSpending())) {
                        planned.put(op.getPlannedSpending(), planned.get(op.getPlannedSpending()) - op.getOperationSum());
                    } else {
                        planned.put(op.getPlannedSpending(), op.getPlannedSpending().getAmount() - op.getOperationSum());
                    }
                    double tmp = planned.get(op.getPlannedSpending());
                    if (tmp < 0) tmp = 0;
                    e2.setAttribute("remainingAmount", CommonUtils.formatDouble(tmp));
                }
                e1.appendChild(e2);
            }
        }
        // Поехали считать вторую часть - "итого", что называется
        Session sess = null;
        sess = HibernateUtils.openSession();
        // Получаем список планируемых месячных расходов за период
        Query query;
        if (timeParams.getBeginDate() == null) {
            // Значит - за месяц
            query = sess.createQuery("from MonthSpending where month=? AND year=? order by amount").
                    setInteger(0, timeParams.getSelectedMonth()).
                    setInteger(1, timeParams.getSelectedYear());
        } else {
            // А вот тут у нас диапазон дат. И это - ОЧЕНЬ плохо
            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(timeParams.getBeginDate().getTime());
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(timeParams.getEndDate().getTime());
            query = sess.createQuery("from MonthSpending where (month >= ? AND year >=?)AND(month <= ? AND year <=?) order by amount").
                    setInteger(0, cal1.get(Calendar.MONTH)).
                    setInteger(1, cal1.get(Calendar.YEAR)).
                    setInteger(2, cal2.get(Calendar.MONTH)).
                    setInteger(3, cal2.get(Calendar.YEAR));
        }
        e1 = doc.createElement("planned");
        root.appendChild(e1);
        List<MonthSpending> dbPlanned = query.list();
        sess.close();
        for (MonthSpending spend : dbPlanned) {
            Element e2 = doc.createElement("spend");
            e2.setAttribute("name", spend.getName());
            e2.setAttribute("plannedFor", CommonUtils.getMonthNameByIndex(spend.getMonth() - 1) + " " + spend.getYear());
            e2.setAttribute("amount", CommonUtils.formatDouble(spend.getAmount()));
            if (planned.containsKey(spend)) {
                double tmp = planned.get(spend);
                if (tmp < 0) tmp = 0;
                e2.setAttribute("remainingAmount", CommonUtils.formatDouble(tmp));
            } else {
                e2.setAttribute("remainingAmount", CommonUtils.formatDouble(spend.getAmount()));
            }
            e1.appendChild(e2);
        }
        // Поехали считать неплановые
        e1 = doc.createElement("nonPlanned");
        root.appendChild(e1);
        for (String name : new TreeSet<String>(nonPlanned.keySet())) {
            Element e2 = doc.createElement("spend");
            e2.setAttribute("name", name);
            e2.setAttribute("totalAmount", CommonUtils.formatDouble(nonPlanned.get(name)));
            e1.appendChild(e2);
        }
        //
        // Сохраняем
        CommonUtils.saveXML(file, doc);
        //
        // Показываем
        ReportViewer.showPreview(frameComp, locationComp, "PlannedMonthSpending", "PlannedMonthSpending.toReport");
    }
}
