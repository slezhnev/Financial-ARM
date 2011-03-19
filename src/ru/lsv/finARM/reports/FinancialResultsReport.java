package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.mappings.*;
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
import java.util.*;

/**
 * Отчет по финансовым результатам
 */
public class FinancialResultsReport {

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
        File file = new File("FinancialResults.toReport");
        DocumentBuilderFactory factory
                = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();

        Document doc = impl.createDocument(null, null, null);

        Element root = doc.createElement("result");
        root.setAttribute("period", period);
        doc.appendChild(root);
        //
        Session sess = HibernateUtils.openSession();
        //
        // Прибыль наличная
        double cashTotal = 0;
        // Прибыль безналичная
        double nonCashTotal = 0;
        // Прибыль по незакрытым
        double nonClosedTotal = 0;
        // Общая сумма авансов
        double prepaidTotal = 0;
        // Зарплата по менеджерам
        HashMap<Manager, Double> managers = new HashMap<Manager, Double>();
        // Менеджеры по месяцам
        HashSet<ManagerPerMonth> managersPerMonth = new HashSet<ManagerPerMonth>();
        // Плановые расходы
        HashMap<MonthSpending, Double> planned = new HashMap<MonthSpending, Double>();
        // Общая сумма плановых расходов
        double plannedTotal = 0;
        // Общая сумма совершенных плановых расходов
        double plannedMakedTotal = 0;
        // Внеплановые расходы
        double nonPlannedTotal = 0;
        // Общая сумма зарплаты "к выдаче"
        double payrollTotal = 0;
        // Поехали по операциям
        for (FinancialOperation op : operations) {
            switch (op.getKind()) {
                case 0: { // Договор
                    // Подгружаем расходы договора
                    op = (FinancialOperation) sess.get(FinancialOperation.class, op.getFoId());
                    double tmp = op.getOperationSum();
                    for (Spending spend : op.getSpendings()) {
                        tmp = tmp - spend.getPaymentSum();
                    }
                    if (op.getClosed()) {
                        ManagerPerMonth mng = ReportsCommonUtils.getManager(sess, op.getManager().getManagerId(),
                                op.getCloseMonth(), op.getCloseYear());
                        if (mng != null) {
                            double managerCoeff = 0;
                            if (op.getPaymentType() == 0) {
                                cashTotal = cashTotal + tmp;
                                managerCoeff = mng.getCashPercent();
                            } else {
                                nonCashTotal = nonCashTotal + tmp;
                                managerCoeff = mng.getNonCashPercent();
                            }
                            double anotherPayments = 0;
                            if (!managersPerMonth.contains(mng)) {
                                // Если в этом месяце мы еще ничего не учитывали, то учтем
                                // Приплюсуем зарплату и т.п.
                                anotherPayments = mng.getSalary() + mng.getSubsidy() - mng.getRetention();
                                managersPerMonth.add(mng);
                            }
                            // Обрабатываем выдачу бабла менеджерам
                            if (managers.containsKey(op.getManager())) {
                                managers.put(op.getManager(), managers.get(op.getManager()) + tmp * managerCoeff / 100 + anotherPayments);
                            } else {
                                managers.put(op.getManager(), tmp * managerCoeff / 100 + anotherPayments);
                            }
                        }
                    } else {
                        nonClosedTotal = nonClosedTotal + tmp;
                    }
                    // Поехали считать
                    break;
                }
                case 1: { // Аванс
                    prepaidTotal = prepaidTotal + op.getOperationSum();
                    if (managers.containsKey(op.getManager())) {
                        managers.put(op.getManager(), managers.get(op.getManager()) - op.getOperationSum());
                    } else {
                        managers.put(op.getManager(), -op.getOperationSum());
                    }
                    break;
                }
                case 2: { // Расход
                    if (op.getPlannedSpending() == null) {
                        // Внеплановый
                        nonPlannedTotal = nonPlannedTotal + op.getOperationSum();
                    } else {
                        // Плановый
                        if (planned.containsKey(op.getPlannedSpending())) {
                            planned.put(op.getPlannedSpending(), planned.get(op.getPlannedSpending()) + op.getOperationSum());
                        } else {
                            planned.put(op.getPlannedSpending(), op.getOperationSum());
                        }
                    }
                    break;
                }
            }
        }
        //
        // Поехали обрабатывать остатки менеджеров и плановые платежи
        // Иенеджеры
        for (Manager mng : managers.keySet()) {
            if ((mng.getCashPercent() != 100) && (mng.getNonCashPercent() != 100))
                payrollTotal = payrollTotal + managers.get(mng);
        }
        // Плановые платежи
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
        java.util.List<MonthSpending> dbPlanned = query.list();
        // Итого - запланировано
        for (MonthSpending spend : dbPlanned) {
            plannedTotal = plannedTotal + spend.getAmount();
        }
        // Итого - потрачего
        for (MonthSpending spend : planned.keySet()) {
            plannedMakedTotal = plannedMakedTotal + planned.get(spend);
        }
        //
        sess.close();
        // Засовываем в XML
        Element e1 = doc.createElement("cashTotal");
        e1.setTextContent("" + cashTotal);
        root.appendChild(e1);
        e1 = doc.createElement("nonCashTotal");
        e1.setTextContent("" + nonCashTotal);
        root.appendChild(e1);
        e1 = doc.createElement("nonClosedTotal");
        e1.setTextContent("" + nonClosedTotal);
        root.appendChild(e1);
        e1 = doc.createElement("prepaidTotal");
        e1.setTextContent("" + prepaidTotal);
        root.appendChild(e1);
        e1 = doc.createElement("plannedTotal");
        e1.setTextContent("" + plannedTotal);
        root.appendChild(e1);
        e1 = doc.createElement("plannedMakedTotal");
        e1.setTextContent("" + plannedMakedTotal);
        root.appendChild(e1);
        e1 = doc.createElement("plannedTxt");
        e1.setTextContent("всего - " + CommonUtils.formatCurrency(plannedTotal) + "\n потрачено - " + CommonUtils.formatCurrency(plannedMakedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("nonPlannedTotal");
        e1.setTextContent("" + nonPlannedTotal);
        root.appendChild(e1);
        e1 = doc.createElement("payrollTotal");
        e1.setTextContent("" + payrollTotal);
        root.appendChild(e1);
        //
        // Сохраняем
        CommonUtils.saveXML(file, doc);
        //
        // Показываем
        ReportViewer.showPreview(frameComp, locationComp, "FinancialResults", "FinancialResults.toReport");
    }

}
