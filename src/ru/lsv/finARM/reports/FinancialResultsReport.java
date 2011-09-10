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
import ru.lsv.finARM.ui.FormattedEditDialog;
import ru.lsv.finARM.ui.MainForm;
import ru.lsv.finARM.ui.ReportViewer;

import javax.swing.*;
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
import java.util.List;

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
        // Прибыль по незакрытым (прибыль > 0)
        double nonClosedTotal = 0;
        // Убытки по незакрытым
        double nonClosedLosses = 0;
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
        // Общая сумма совершенных плановых расходов (РЕАЛЬНЫХ!)
        double plannedReallyMakedTotal = 0;
        // Внеплановые расходы
        double nonPlannedTotal = 0;
        // Общая сумма зарплаты "к выдаче"
        double payrollTotal = 0;
        // "Коррекция" по незакрытым договорам
        double correction = 0;
        // Посчитаем начало и конец периода
        int beginYear;
        int endYear;
        int beginMonth;
        int endMonth;
        Date beginDate;
        Date endDate;
        if (timeParams.getBeginDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeParams.getBeginDate().getTime());
            beginYear = cal.get(Calendar.YEAR);
            beginMonth = cal.get(Calendar.MONTH);
            cal.setTimeInMillis(timeParams.getEndDate().getTime());
            endYear = cal.get(Calendar.YEAR);
            endMonth = cal.get(Calendar.MONTH);
            //
            beginDate = timeParams.getBeginDate();
            endDate = timeParams.getEndDate();
        } else {
            // А вот тут у нас - тока ОДИН месяц!
            beginYear = timeParams.getSelectedYear();
            endYear = beginYear;
            beginMonth = timeParams.getSelectedMonth();
            endMonth = beginMonth;
            //
            // Побибикали шаманить!
            Calendar cal = Calendar.getInstance();
            cal.set(beginYear, beginMonth, 1);
            beginDate = new Date(cal.getTimeInMillis());
            cal.set(endYear, endMonth, 1);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            endDate = new Date(cal.getTimeInMillis());
        }
        // Поехали по операциям
        for (FinancialOperation op : operations) {
            switch (op.getKind()) {
                case 0: { // Договор
                    // Подгружаем расходы договора
                    op = (FinancialOperation) sess.get(FinancialOperation.class, op.getFoId());
                    double salary = op.getSalarySum();
                    double tmp = op.getOperationSum();
                    for (Spending spend : op.getSpendings()) {
                        tmp = tmp - spend.getPaymentSum();
                        salary = salary - spend.getPaymentSalarySum();
                    }
                    if (op.getClosed() || op.getClosedForSalary()) {
                        ManagerPerMonth mng = null;
                        // Отдельно обрабатываем закрытие и закрытие по зарплате
                        if (op.getClosed())
                            mng = ReportsCommonUtils.getManager(sess, op.getManager().getManagerId(),
                                    op.getCloseMonth(), op.getCloseYear());
                        else {
                            // А тут будем обрабатывать это отдельно.
                            // Т.е. менеджера будем находить только в том случае, если договор
                            // закрыт по зарплате в ТЕКУЩЕМ периоде!
                            if ((op.getCloseForSalaryDate().compareTo(beginDate) >= 0) &&
                                    (op.getCloseForSalaryDate().compareTo(endDate) <= 0)) {
                                mng = ReportsCommonUtils.getManager(sess, op.getManager().getManagerId(),
                                        op.getCloseForSalaryMonth(), op.getCloseForSalaryYear());
                            }
                        }
                        // А менеджера тут мы найдем (и будем считать на него зарплату)
                        // только если это
                        // а) закрытый договор
                        // б) закрытый по зарплате в ТЕКУЩЕМ периоде
                        if (mng != null) {
                            //double managerCoeff = 0;
                            double managerCoeff = op.getManagerPercent();
                            if (op.getClosed()) {
                                if (op.getPaymentType() == 0) {
                                    cashTotal = cashTotal + tmp;
                                    //managerCoeff = mng.getCashPercent();
                                } else {
                                    nonCashTotal = nonCashTotal + tmp;
                                    //managerCoeff = mng.getNonCashPercent();
                                }
                            } else {
                                if (tmp > 0)
                                    nonClosedTotal = nonClosedTotal + tmp;
                                else
                                    nonClosedLosses = nonClosedLosses + tmp;
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
                                //managers.put(op.getManager(), managers.get(op.getManager()) + tmp * managerCoeff / 100 + anotherPayments);
                                managers.put(op.getManager(), managers.get(op.getManager()) + salary * managerCoeff / 100.0 + anotherPayments);
                            } else {
                                //managers.put(op.getManager(), tmp * managerCoeff / 100 + anotherPayments);
                                managers.put(op.getManager(), salary * managerCoeff / 100.0 + anotherPayments);
                            }
                        }
                    } else {
                        if (tmp > 0)
                            nonClosedTotal = nonClosedTotal + tmp;
                        else
                            nonClosedLosses = nonClosedLosses + tmp;
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
                        double tmp;
                        if (planned.containsKey(op.getPlannedSpending())) {
                            tmp = planned.get(op.getPlannedSpending()) + op.getOperationSum();
                        } else {
                            tmp = op.getOperationSum();
                        }
                        if (tmp > op.getPlannedSpending().getAmount())
                            planned.put(op.getPlannedSpending(), op.getPlannedSpending().getAmount());
                        else
                            planned.put(op.getPlannedSpending(), tmp);
                        plannedReallyMakedTotal = plannedReallyMakedTotal + op.getOperationSum();
                    }
                    break;
                }
            }
        }
        //
        // Поехали обрабатывать остатки менеджеров и плановые платежи
        // Учтем зарплаты тех, у кого нет закрытых договоров
        // Получаем из ManagerPerMonth
        Query query;
        if (beginYear == endYear) {
            query = sess.createQuery("from ManagerPerMonth where (year=? AND month >=? AND month <=?)").
                    setInteger(0, beginYear).
                    setInteger(1, beginMonth).
                    setInteger(2, endMonth);
        } else {
            query = sess.createQuery("from ManagerPerMonth where ((year > ? AND year < ?)OR(year=? AND month >=?)OR(year=? AND month <=?))").
                    setInteger(0, beginYear).
                    setInteger(1, endYear).
                    setInteger(2, beginYear).
                    setInteger(3, beginMonth).
                    setInteger(4, endYear).
                    setInteger(5, endMonth);
        }
        List<ManagerPerMonth> mngrs = query.list();
        for (ManagerPerMonth manager : mngrs) {
            if (!managersPerMonth.contains(manager)) {
                // Если в этом месяце мы еще ничего не учитывали, то учтем
                Manager mng = new Manager();
                mng.setManagerId(manager.getManagerId());
                // Обрабатываем выдачу бабла менеджерам
                if (managers.containsKey(mng)) {
                    managers.put(mng, managers.get(mng) + manager.getSalary() + manager.getSubsidy() - manager.getRetention());
                } else {
                    managers.put(mng, manager.getSalary() + manager.getSubsidy() - manager.getRetention());
                }

            }
        }
        correction = FormattedEditDialog.doEnterValue("Ввод значения", nonClosedLosses, locationComp);
        //
        // Менеджеры
        for (Manager mng : managers.keySet()) {
            if ((mng.getCashPercent() != 100) && (mng.getNonCashPercent() != 100))
                payrollTotal = payrollTotal + managers.get(mng);
        }
        // Плановые платежи
        if (beginYear == endYear) {
            query = sess.createQuery("from MonthSpending where (year=? AND month >=? AND month <=?) order by amount").
                    setInteger(0, beginYear).
                    setInteger(1, beginMonth).
                    setInteger(2, endMonth);
        } else {
            query = sess.createQuery("from MonthSpending where ((year > ? AND year < ?)OR(year=? AND month >=?)OR(year=? AND month <=?)) order by amount").
                    setInteger(0, beginYear).
                    setInteger(1, endYear).
                    setInteger(2, beginYear).
                    setInteger(3, beginMonth).
                    setInteger(4, endYear).
                    setInteger(5, endMonth);
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
        e1.setTextContent(CommonUtils.formatDouble(cashTotal));
        root.appendChild(e1);
        e1 = doc.createElement("nonCashTotal");
        e1.setTextContent(CommonUtils.formatDouble(nonCashTotal));
        root.appendChild(e1);
        e1 = doc.createElement("nonClosedTotal");
        e1.setTextContent(CommonUtils.formatDouble(nonClosedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("nonClosedLosses");
        e1.setTextContent(CommonUtils.formatDouble(nonClosedLosses));
        root.appendChild(e1);
        e1 = doc.createElement("prepaidTotal");
        e1.setTextContent(CommonUtils.formatDouble(prepaidTotal));
        root.appendChild(e1);
        e1 = doc.createElement("plannedTotal");
        e1.setTextContent(CommonUtils.formatDouble(plannedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("plannedMakedTotal");
        e1.setTextContent(CommonUtils.formatDouble(plannedMakedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("plannedReallyMakedTotal");
        e1.setTextContent(CommonUtils.formatDouble(plannedReallyMakedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("plannedTxt");
        e1.setTextContent("всего - " + CommonUtils.formatCurrency(plannedTotal) + "\n потрачено - " + CommonUtils.formatCurrency(plannedMakedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("nonPlannedTotal");
        e1.setTextContent(CommonUtils.formatDouble(nonPlannedTotal));
        root.appendChild(e1);
        e1 = doc.createElement("payrollTotal");
        e1.setTextContent(CommonUtils.formatDouble(payrollTotal));
        root.appendChild(e1);
        e1 = doc.createElement("correction");
        e1.setTextContent(CommonUtils.formatDouble(correction));
        root.appendChild(e1);
        //
        // Сохраняем
        CommonUtils.saveXML(file, doc);
        //
        // Показываем
        ReportViewer.showPreview(frameComp, locationComp, "FinancialResults", "FinancialResults.toReport");
    }

}
