package ru.lsv.finARM.reports;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import ru.lsv.finARM.common.CommonUtils;
import ru.lsv.finARM.common.HibernateUtils;
import ru.lsv.finARM.logic.FinancialMonths;
import ru.lsv.finARM.mappings.*;
import ru.lsv.finARM.ui.MainForm;
import ru.lsv.finARM.ui.ReportViewer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Зарплатная ведомость
 */
public class PayrollReport {

    /**
     * Формирование отчета
     *
     * @param frameComp    см. @ru.lsv.finARM.ui.ReportViewer
     * @param locationComp см. @ru.lsv.finARM.ui.ReportViewer
     * @param period       За какой период он формируется
     * @param operations   Список финансовых операций за этот период
     * @param timeParams   За какой временной промежуток строить отчет
     * @param fullReport   Формировать ведомость для директора (true) или секретаря (false)
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
                           String period, List<FinancialOperation> operations,
                           MainForm.TimeFilterParams timeParams,
                           boolean fullReport) throws ParserConfigurationException, IOException, TransformerException, HibernateException {
        Session sess = null;
        try {
            //
            if ((timeParams.getBeginDate() == null)) {
                FinancialMonth fm = FinancialMonths.getInstance().getActiveMonth();
                // Если месяц совпадает с просматриваемым - то будем сохранять данные по менеджерам...
                if ((fm.getMonth() == timeParams.getSelectedMonth()) && (fm.getYear() == timeParams.getSelectedYear())) {
                    // Удаляем старые данные
                    sess = HibernateUtils.openSession();
                    sess.createQuery("delete from ManagerPerMonth where month=? AND year=?").
                            setInteger(0, fm.getMonth()).
                            setInteger(1, fm.getYear()).
                            executeUpdate();
                    // Получаем и сохраняем текущие...
                    List<Manager> tempMngs = sess.createQuery("from Manager where dismissed=false").list();
                    Transaction trx = null;
                    try {
                        trx = sess.beginTransaction();
                        //
                        for (Manager mng : tempMngs) {
                            sess.save(new ManagerPerMonth(mng, fm));
                        }
                        //
                        sess.flush();
                        trx.commit();
                        trx = null;
                        sess.close();
                        sess = null;
                    } finally {
                        if (trx != null) trx.rollback();
                        if (sess != null) sess.close();
                    }
                }
            }
            //
            // Формируем XML с отчетом...
            File file = new File("Payroll.toReport");
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            DOMImplementation impl = builder.getDOMImplementation();

            Document doc = impl.createDocument(null, null, null);

            Element root = doc.createElement("payroll");
            root.setAttribute("period", period);
            doc.appendChild(root);
            //
            sess = HibernateUtils.openSession();
            // Создаем все для формирования
            HashMap<Manager, PayrollElement> managers = new HashMap<Manager, PayrollElement>();
            // Формируем ОБЩИЙ список менеджеров
            List<Manager> allManagers = sess.createQuery("from Manager order by FIO").list();
            for (Manager mng : allManagers) {
                managers.put(mng, new PayrollElement());
            }
            // Поехали по операциям
            for (FinancialOperation op : operations) {
                switch (op.getKind()) {
                    case 0: { // Договор
                        if (op.getClosed() || op.getClosedForSalary()) {
                            // Что-то считаем только в том случае, если оно у нас тут закрыто
                            // Считаем общую прибыль по договору
                            op = (FinancialOperation) sess.get(FinancialOperation.class, op.getFoId());
                            PayrollElement el = managers.get(op.getManager());
                            if (el == null) el = new PayrollElement();
                            StringBuffer suppliers = new StringBuffer();
                            double suppliersSum = 0;
                            for (Spending sp : op.getSpendings()) {
                                if (suppliers.length() != 0) suppliers.append("\n");
                                suppliers.append(sp.getPayerTo()).append("(").append(sp.getOrderNum()).append(") - ").append(CommonUtils.formatCurrency(sp.getPaymentSalarySum()));
                                suppliersSum = suppliersSum + sp.getPaymentSalarySum();
                            }
                            PayrollElement_Contracts contract = new PayrollElement_Contracts(op.getCustomer(),
                                    op.getOrderNum(), op.getPaymentType(), /*op.getOperationSum(),*/
                                    op.getSalarySum(),
                                    suppliers.toString(), suppliersSum,
                                    op.getCloseMonth(), op.getCloseYear(),
                                    op.getManagerPercent(),
                                    op.getOperationDate());
                            el.contracts.add(contract);
                            managers.put(op.getManager(), el);
                        }
                        break;
                    }
                    case 1: { // Аванс
                        PayrollElement el = managers.get(op.getManager());
                        if (el == null) el = new PayrollElement();
                        managers.put(op.getManager(), el.addPrepayment(op.getOperationSum()));
                        break;
                    }
                }
            }
            // Формируем теперь XML
            HashSet<ManagerPerMonth> managersPerMonth = new HashSet<ManagerPerMonth>();
            int id = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            for (Manager mng : new TreeSet<Manager>(managers.keySet())) {
                // Исключаем директоров
                if ((mng.getCashPercent() != 100) || (mng.getNonCashPercent() != 100)) {
                    Element e = doc.createElement("manager");
                    PayrollElement pe = managers.get(mng);
                    e.setAttribute("id", "" + id);
                    e.setAttribute("FIO", mng.getFIO());
                    e.setAttribute("prepayment", CommonUtils.formatDouble(pe.getPrepayment()));
                    // А вот профит - надо бы еще посчитать как бе...
                    // Причем по договорам, причем с учетом ManagerPerMonth
                    double cashProfit = 0;
                    double nonCashProfit = 0;
//                    double subsidy = 0;
//                    double retention = 0;
//                    double salary = 0;
                    for (PayrollElement_Contracts contract : pe.contracts) {
                        Element e1 = doc.createElement("contract");
                        e1.setAttribute("parentId", "" + id);
                        e1.setAttribute("customer", contract.customer);
                        e1.setAttribute("date", sdf.format(contract.operationDate));
                        e1.setAttribute("order", contract.order);
                        e1.setAttribute("paymentSum", CommonUtils.formatCurrency(contract.paymentSum));
                        e1.setAttribute("managerPercent", "" + contract.managerPercent + "%");
                        e1.setAttribute("suppliersSum", CommonUtils.formatCurrency(contract.suppliersSum));
                        Element e2 = doc.createElement("suppliers");
                        e2.setTextContent(contract.suppliers);
                        e1.appendChild(e2);
                        e1.setAttribute("profit", CommonUtils.formatCurrency(contract.paymentSum - contract.suppliersSum));
//                        ManagerPerMonth manager = ReportsCommonUtils.getManager(sess, mng.getManagerId(), contract.closedMonth, contract.closedYear);
//                        if (!managersPerMonth.contains(manager)) {
//                            // Значит этот месяц еще не учтен. Поедем обработаем
//                            subsidy = subsidy + (manager.getSubsidy() == null ? 0 : manager.getSubsidy());
//                            retention = retention + (manager.getRetention() == null ? 0 : manager.getRetention());
//                            salary = salary + (manager.getSalary() == null ? 0 : manager.getSalary());
//                            managersPerMonth.add(manager);
//                        }
                        if (contract.paymentKind == 0) {
                            e1.setAttribute("paymentType", "нал");
                            //cashProfit = cashProfit + manager.getCashPercent() / 100 * (contract.paymentSum - contract.suppliersSum);
                            cashProfit = cashProfit + contract.managerPercent / 100.0 * (contract.paymentSum - contract.suppliersSum);
                        } else {
                            e1.setAttribute("paymentType", "безнал");
                            //nonCashProfit = nonCashProfit + manager.getNonCashPercent() / 100 * (contract.paymentSum - contract.suppliersSum);
                            nonCashProfit = nonCashProfit + contract.managerPercent / 100.0 * (contract.paymentSum - contract.suppliersSum);
                        }
                        e.appendChild(e1);
                    }
                    // Посчитаем зарплату и прочую фигню за период...
                    int beginYear;
                    int endYear;
                    int beginMonth;
                    int endMonth;
                    if (timeParams.getBeginDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(timeParams.getBeginDate().getTime());
                        beginYear = cal.get(Calendar.YEAR);
                        beginMonth = cal.get(Calendar.MONTH);
                        cal.setTimeInMillis(timeParams.getEndDate().getTime());
                        endYear = cal.get(Calendar.YEAR);
                        endMonth = cal.get(Calendar.MONTH);
                    } else {
                        // А вот тут у нас - тока ОДИН месяц!
                        beginYear = timeParams.getSelectedYear();
                        endYear = beginYear;
                        beginMonth = timeParams.getSelectedMonth();
                        endMonth = beginMonth;
                    }
                    // Получаем из ManagerPerMonth
                    Query query;
                    if (beginYear == endYear) {
                        query = sess.createQuery("from ManagerPerMonth where (managerId=?) and (year=? AND month >=? AND month <=?)").
                                setInteger(0, mng.getManagerId()).
                                setInteger(1, beginYear).
                                setInteger(2, beginMonth).
                                setInteger(3, endMonth);
                    } else {
                        query = sess.createQuery("from ManagerPerMonth where (managerId=?) and ((year > ? AND year < ?)OR(year=? AND month >=?)OR(year=? AND month <=?))").
                                setInteger(0, mng.getManagerId()).
                                setInteger(1, beginYear).
                                setInteger(2, endYear).
                                setInteger(3, beginYear).
                                setInteger(4, beginMonth).
                                setInteger(5, endYear).
                                setInteger(6, endMonth);
                    }
                    List<ManagerPerMonth> mngrs = query.list();
                    // Считаем
                    double subsidy = 0;
                    double retention = 0;
                    double salary = 0;
                    for (ManagerPerMonth manager : mngrs) {
                        if (!managersPerMonth.contains(manager)) {
                            // Значит этот месяц еще не учтен. Поедем обработаем
                            subsidy = subsidy + (manager.getSubsidy() == null ? 0 : manager.getSubsidy());
                            retention = retention + (manager.getRetention() == null ? 0 : manager.getRetention());
                            salary = salary + (manager.getSalary() == null ? 0 : manager.getSalary());
                            managersPerMonth.add(manager);
                        }
                    }
                    //
                    e.setAttribute("profit", CommonUtils.formatDouble(cashProfit + nonCashProfit + salary + subsidy - retention));
                    e.setAttribute("paycheck", CommonUtils.formatDouble(cashProfit + nonCashProfit + salary + subsidy - retention - pe.getPrepayment()));
                    if (fullReport) {
                        e.setAttribute("subsidy", CommonUtils.formatDouble(subsidy));
                        e.setAttribute("retention", CommonUtils.formatDouble(retention));
                        e.setAttribute("salary", CommonUtils.formatDouble(salary));
                        e.setAttribute("cashProfit", CommonUtils.formatDouble(cashProfit));
                        e.setAttribute("nonCashProfit", CommonUtils.formatDouble(nonCashProfit));
                    }
                    root.appendChild(e);
                    id++;
                }
            }
            sess.close();
            sess = null;
            //
            // Сохраняем
            CommonUtils.saveXML(file, doc);
            //
            // Показываем
            if (fullReport) {
                ReportViewer.showPreview(frameComp, locationComp, "FullPayrollReport", "Payroll.toReport");
            } else {
                ReportViewer.showPreview(frameComp, locationComp, "PayrollReport", "Payroll.toReport");
            }
        } finally {
            if (sess != null) sess.close();
        }
    }

    /**
     * Класс - агрегатор суммы аванса и общей прибыли
     */
    private class PayrollElement {

        public PayrollElement() {
            prepayment = 0;
            cashProfit = 0;
            nonCashProfit = 0;
        }

        /**
         * Сумма аванса
         */
        private double prepayment;

        /**
         * Получить сумму аванса
         *
         * @return Сумма аванса
         */
        public double getPrepayment() {
            return prepayment;
        }

        /**
         * Добавить сумму к авансу
         *
         * @param prepayment Сумма к добавлению
         * @return Текущий элемент
         */
        public PayrollElement addPrepayment(double prepayment) {
            this.prepayment = this.prepayment + prepayment;
            return this;
        }

        /**
         * Получить сумму наличной прибыли
         *
         * @return Сумма прибыли
         */
        public double getCashProfit() {
            return cashProfit;
        }

        /**
         * Получить сумму безналичной прибыли
         *
         * @return умма безналичной прибыли
         */
        public double getNonCashProfit() {
            return nonCashProfit;
        }

        /**
         * Получить сумму наличной и безналичной прибыли
         *
         * @return Сумма прибыли
         */
        public double getProfit() {
            return cashProfit + nonCashProfit;
        }

        /**
         * Добавить сумму к наличной прибыли
         *
         * @param profit Сумма к добавлению
         * @return Текущий элемент
         */
        public PayrollElement addCashProfit(double profit) {
            this.cashProfit = this.cashProfit + profit;
            return this;

        }

        /**
         * Добавить сумму к безналичной прибыли
         *
         * @param profit Сумма к добавлению
         * @return Текущий элемент
         */
        public PayrollElement addNonCashProfit(double profit) {
            this.nonCashProfit = this.nonCashProfit + profit;
            return this;

        }

        /**
         * Сумма наличной прибыли
         */
        private double cashProfit;

        /**
         * Сумма безналичной прибыли
         */
        private double nonCashProfit;

        /**
         * Получить "к выдаче"
         *
         * @return Общая прибыль (как прибыль-авансы)
         */
        public double getTotalProfit() {
            return cashProfit + nonCashProfit - prepayment;
        }

        /**
         * Договора и параметры по ним
         */
        public ArrayList<PayrollElement_Contracts> contracts = new ArrayList<PayrollElement_Contracts>();
    }

    /**
     * Класс - инкапсулятор для договора
     * Сделан не совсем, конечно, правильно - но да и ладно. Все равно только storage
     */
    private class PayrollElement_Contracts {

        private PayrollElement_Contracts(String customer, String order, Integer paymentKind, Double paymentSum,
                                         String suppliers, Double suppliersSum, Integer closedMonth, Integer closedYear,
                                         Double managerPercent, Date operationDate) {
            this.customer = customer;
            this.order = order;
            this.paymentKind = paymentKind;
            this.paymentSum = paymentSum;
            this.suppliers = suppliers;
            this.suppliersSum = suppliersSum;
            this.closedMonth = closedMonth;
            this.closedYear = closedYear;
            this.managerPercent = managerPercent;
            this.operationDate = operationDate;
        }

        /**
         * Поставщик
         */
        public String customer;

        /**
         * Номер счета
         */
        public String order;
        /**
         * Сумма
         */
        public Double paymentSum;
        /**
         * Тип платежа - нал/безнал
         */
        public Integer paymentKind;
        /**
         * Поставщики со счетами и суммами
         */
        public String suppliers;
        /**
         * Сколько всего потратили на поставщиков
         */
        public Double suppliersSum;
        /**
         * Месяц закрытия договора
         */
        public Integer closedMonth;
        /**
         * Год закрытия
         */
        public Integer closedYear;
        /**
         * Процент менеджера
         */
        public Double managerPercent;
        /**
         * Дата договора
         */
        public Date operationDate;
    }

}
