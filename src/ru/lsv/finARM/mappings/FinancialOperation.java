package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Финансовая операция
 */
public class FinancialOperation implements Cloneable {

    public FinancialOperation() {
        closed = false;
        operationSum = 0.0;
        salarySum = 0.0;
        operationDate = new Date(Calendar.getInstance().getTimeInMillis());
        paymentType = 0;
        kind = 0;
        spendings = new TreeSet<Spending>();
        incomings = new TreeSet<Incoming>();
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a clone of this instance.
     * @see Cloneable
     */
    @Override
    public Object clone() {
        FinancialOperation fo = new FinancialOperation();
        fo.foId = this.foId;
        fo.kind = this.kind;
        fo.operationDate = this.operationDate;
        fo.operationSum = this.operationSum;
        fo.manager = this.manager;
        fo.customer = this.customer;
        fo.orderNum = this.orderNum;
        fo.paymentType = this.paymentType;
        fo.closed = this.closed;
        fo.closeDate = this.closeDate;
        fo.closeYear = this.closeYear;
        fo.closeMonth = this.closeMonth;
        fo.spendings = new TreeSet<Spending>();
        for (Spending sp : this.spendings) {
            fo.spendings.add((Spending) sp.clone());
        }
        fo.currentProfit = this.currentProfit;
        fo.salarySum = this.salarySum;
        fo.currentSalaryProfit = this.currentSalaryProfit;
        fo.managerPercent = this.managerPercent;
        fo.incomings = new TreeSet<Incoming>();
        for (Incoming inc : this.incomings) {
            fo.incomings.add((Incoming) inc.clone());
        }
        fo.plannedSpending = this.plannedSpending;
        fo.nonPlannedSpending = this.nonPlannedSpending;
        fo.closedForSalary = this.closedForSalary;
        fo.closeForSalaryDate = this.closeForSalaryDate;
        fo.closeForSalaryYear = this.closeForSalaryYear;
        fo.closeForSalaryMonth = this.closeForSalaryMonth;
        return fo;
    }

    /**
     * Выдает изменения между oldFinOp и this
     *
     * @param oldFinOp - финансовая операция для сравнения
     * @return текстовое описание изменений
     */
    public String whatChanged(FinancialOperation oldFinOp) {
        // Если сравнивать не с чем - возвращаем пустое место
        if (oldFinOp == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        StringBuffer res = new StringBuffer("изменено:");
        if (!oldFinOp.operationDate.equals(this.operationDate)) {
            res.append("\nдата договора с ").append(sdf.format(oldFinOp.operationDate)).append(" на ").append(sdf.format(this.operationDate));
        }
        if (!oldFinOp.operationSum.equals(this.operationSum)) {
            res.append("\nсумма договора с ").append(CommonUtils.formatCurrency(oldFinOp.operationSum)).append(" на ").append(CommonUtils.formatCurrency(this.operationSum));
        }
        if ((oldFinOp.manager != null) && (!oldFinOp.manager.equals(this.manager))) {
            res.append("\nменеджер с ").append(oldFinOp.manager.toString()).append(" на ").append(this.manager.toString());
        }
        if ((oldFinOp.customer != null) && (!oldFinOp.customer.equals(this.customer))) {
            res.append("\nзаказчик с ").append(oldFinOp.customer).append(" на ").append(this.customer);
        }
        if ((oldFinOp.orderNum != null) && (!oldFinOp.orderNum.equals(this.orderNum))) {
            res.append("\nномер счета с ").append(oldFinOp.orderNum).append(" на ").append(this.orderNum);
        }
        if (!oldFinOp.paymentType.equals(this.paymentType)) {
            res.append("\nвид платежа с ");
            if (oldFinOp.paymentType == 0) res.append("безнал.");
            else res.append("нал.");
            res.append(" на ");
            if (this.paymentType == 0) res.append("безнал.");
            else res.append("нал.");
        }
        if (oldFinOp.closed != this.closed) {
            if (oldFinOp.closed) {
                res.append("\nдоговор открыт");
            } else {
                res.append("\nдоговор закрыт ").append(sdf.format(this.closeDate));
            }
        }
        if (oldFinOp.closedForSalary != this.closedForSalary) {
            if (oldFinOp.closedForSalary) {
                res.append("\nдоговор открыт по зарплате");
            } else {
                res.append("\nдоговор закрыт по зарплате ").append(sdf.format(this.closeForSalaryDate));
            }
        }
        if (this.kind == 2) {
            if ((oldFinOp.plannedSpending == null) && (this.plannedSpending != null)) {
                res.append("\nрасход с ").append(oldFinOp.nonPlannedSpending).append(" на ").append(this.plannedSpending.toString());
            } else if ((oldFinOp.plannedSpending != null) && (this.plannedSpending == null)) {
                res.append("\nрасход с ").append(oldFinOp.plannedSpending.toString()).append(" на ").append(this.nonPlannedSpending);
            } else if ((oldFinOp.plannedSpending != null) && (!oldFinOp.plannedSpending.equals(this.plannedSpending))) {
                res.append("\nрасход с ").append(oldFinOp.plannedSpending.toString()).append(" на ").append(this.plannedSpending.toString());
            } else if ((oldFinOp.plannedSpending == null) && (!oldFinOp.nonPlannedSpending.equals(this.nonPlannedSpending))) {
                res.append("\nрасход с ").append(oldFinOp.nonPlannedSpending).append(" на ").append(this.nonPlannedSpending);
            }
        }
        if (this.kind == 0) {
            if (!oldFinOp.currentProfit.equals(this.currentProfit)) {
                res.append("\nтекущая прибыль по договору с ").append(CommonUtils.formatCurrency(oldFinOp.currentProfit)).append(" на ").append(CommonUtils.formatCurrency(this.currentProfit));
            }
            if (!oldFinOp.salarySum.equals(this.salarySum)) {
                res.append("\nзарплатная сумма с ").append(CommonUtils.formatCurrency(oldFinOp.salarySum)).append(" на ").append(CommonUtils.formatCurrency(this.salarySum));
            }
            if (!oldFinOp.currentSalaryProfit.equals(this.currentSalaryProfit)) {
                res.append("\nтекущая зарплатная прибыль по договору с ").append(CommonUtils.formatCurrency(oldFinOp.currentSalaryProfit)).append(" на ").append(CommonUtils.formatCurrency(this.currentSalaryProfit));
            }
            if (!oldFinOp.managerPercent.equals(this.managerPercent)) {
                res.append("\nпроцент менеджера с ").append(oldFinOp.managerPercent).append(" на ").append(this.managerPercent);
            }
            // А тут щас еще будем сравнивать приход / расход
            // Тут все просто. По очереди пытаемся делать сравнение
            // Поступления
            if (this.incomings.size() < oldFinOp.incomings.size()) {
                res.append("\nудалены поступления");
            } else if (this.incomings.size() > oldFinOp.incomings.size()) {
                res.append("\nдобавлены поступления");                
            } else {
                // Вот тут поедем сравнивать по очереди по а)коду, б)хэшу
                Iterator<Incoming> oldIncs = oldFinOp.incomings.iterator();
                for (Incoming inc : this.incomings) {
                    Incoming oldInc = oldIncs.next();
                    if (!inc.getIncomingId().equals(oldInc.getIncomingId())) {
                        // Тут - что-то новое
                        res.append("\nизменены поступления");
                        break;
                    } else if (inc.hashCode() != oldInc.hashCode()) {
                        res.append("\nизменены поступления");
                        break;
                    }
                }
            }
            // Расходы
            if (this.spendings.size() < oldFinOp.spendings.size()) {
                res.append("\nудалены расходы");
            } else if (this.spendings.size() > oldFinOp.spendings.size()) {
                res.append("\nдобавлены расходы");
            } else {
                // Вот тут поедем сравнивать по очереди по а)коду, б)хэшу
                Iterator<Spending> oldIncs = oldFinOp.spendings.iterator();
                for (Spending inc : this.spendings) {
                    Spending oldInc = oldIncs.next();
                    if (!inc.getFinSpId().equals(oldInc.getFinSpId())) {
                        // Тут - что-то новое
                        res.append("\nизменены расходы");
                        break;
                    } else if (inc.hashCode() != oldInc.hashCode()) {
                        res.append("\nизменены расходы");
                        break;
                    }
                }
            }
        }
        return res.toString();
    }

    public Boolean getClosed() {
        return closed;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Integer getFoId() {
        return foId;
    }

    public void setFoId(Integer foId) {
        this.foId = foId;
    }

    public Integer getKind() {
        return kind;
    }

    public void setKind(Integer kind) {
        this.kind = kind;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public String getNonPlannedSpending() {
        return nonPlannedSpending;
    }

    public void setNonPlannedSpending(String nonPlannedSpending) {
        this.nonPlannedSpending = nonPlannedSpending;
    }

    public Date getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(Date operationDate) {
        this.operationDate = operationDate;
    }

    public Double getOperationSum() {
        return operationSum;
    }

    public void setOperationSum(Double operationSum) {
        this.operationSum = operationSum;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(Integer paymentType) {
        this.paymentType = paymentType;
    }

    public MonthSpending getPlannedSpending() {
        return plannedSpending;
    }

    public void setPlannedSpending(MonthSpending plannedSpending) {
        this.plannedSpending = plannedSpending;
    }

    public SortedSet<Spending> getSpendings() {
        return spendings;
    }

    public void setSpendings(SortedSet<Spending> spendings) {
        this.spendings = spendings;
    }

    public Integer getCloseMonth() {
        return closeMonth;
    }

    public void setCloseMonth(Integer closeMonth) {
        this.closeMonth = closeMonth;
    }

    public Integer getCloseYear() {
        return closeYear;
    }

    public void setCloseYear(Integer closeYear) {
        this.closeYear = closeYear;
    }

    public Double getCurrentProfit() {
        return currentProfit;
    }

    public void setCurrentProfit(Double currentProfit) {
        this.currentProfit = currentProfit;
    }

    /**
     * Код операции
     */
    private Integer foId;
    /**
     * Тип операции. 0 - договор, 1 - аванс, 2 - трата
     */
    private Integer kind;

    // Общие параметры вообще для всех трех типов
    /**
     * Дата операции
     */
    private Date operationDate;
    /**
     * Сумма
     */
    private Double operationSum;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Общие параметры для договора и аванса
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Менеджер
     */
    private Manager manager;


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Блок для договора
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Заказчик
     */
    private String customer;
    /**
     * Номер счета
     */
    private String orderNum;
    /**
     * Вид оплаты. 0 - наличными, 1 - безналичный
     */
    private Integer paymentType;
    /**
     * Отметка о закрытии операции
     */
    private Boolean closed;
    /**
     * Дата закрытия операции
     */
    private Date closeDate;
    /**
     * Года закрытия операции - для удобства фильтрования
     */
    private Integer closeYear;
    /**
     * Месяц закрытия операции - для удобства фильтрования
     */
    private Integer closeMonth;
    /**
     * Перечень расходов по операции
     */
    private SortedSet<Spending> spendings;
    /**
     * Текущая прибыль
     */
    private Double currentProfit;
    /**
     * Сумма договора для расчета зарплаты
     */
    private Double salarySum;
    /**
     * Текущая прибыль для расчета зарплаты
     */
    private Double currentSalaryProfit;
    /**
     * Текущий процент менеджера
     */
    private Double managerPercent;
    /**
     * Поступления
     */
    private SortedSet<Incoming> incomings;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Блок для расхода
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Ссылка на планируемую трату
     */
    private MonthSpending plannedSpending;
    /**
     * Наименование для внеплановой траты
     */
    private String nonPlannedSpending;
    /**
     * Отметка о закрытии операции по зарплате
     */
    private Boolean closedForSalary;
    /**
     * Дата закрытия операции  по зарплате
     */
    private Date closeForSalaryDate;
    /**
     * Год закрытия операции  по зарплате - для удобства фильтрования
     */
    private Integer closeForSalaryYear;
    /**
     * Месяц закрытия операции  по зарплате - для удобства фильтрования
     */
    private Integer closeForSalaryMonth;

    /**
     * Returns a string representation of the object. I
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        String str = null;
        switch (kind) {
            case 0: {
                str = "договор с " + customer + " (счет №" + orderNum + ", сумма: " + CommonUtils.formatCurrency(operationSum) + ")";
                break;
            }
            case 1: {
                str = "аванс " + manager + " (сумма: " + CommonUtils.formatCurrency(operationSum) + ")";
                break;
            }
            case 2: {
                str = "расход \"";
                if (plannedSpending != null) str = str + plannedSpending;
                else str = str + nonPlannedSpending;
                str = str + "\" (сумма: " + CommonUtils.formatCurrency(operationSum) + ")";
                break;
            }
        }
        return str;
    }

    public Double getSalarySum() {
        return salarySum;
    }

    public void setSalarySum(Double salarySum) {
        this.salarySum = salarySum;
    }

    public Double getCurrentSalaryProfit() {
        return currentSalaryProfit;
    }

    public void setCurrentSalaryProfit(Double currentSalaryProfit) {
        this.currentSalaryProfit = currentSalaryProfit;
    }

    public Double getManagerPercent() {
        return managerPercent;
    }

    public void setManagerPercent(Double managerPercent) {
        this.managerPercent = managerPercent;
    }

    public Boolean getClosedForSalary() {
        return closedForSalary == null ? false : closedForSalary;
    }

    public void setClosedForSalary(Boolean closedForSalary) {
        this.closedForSalary = closedForSalary;
    }

    public Date getCloseForSalaryDate() {
        return closeForSalaryDate;
    }

    public void setCloseForSalaryDate(Date closeForSalaryDate) {
        this.closeForSalaryDate = closeForSalaryDate;
    }

    public Integer getCloseForSalaryYear() {
        return closeForSalaryYear;
    }

    public void setCloseForSalaryYear(Integer closeForSalaryYear) {
        this.closeForSalaryYear = closeForSalaryYear;
    }

    public Integer getCloseForSalaryMonth() {
        return closeForSalaryMonth;
    }

    public void setCloseForSalaryMonth(Integer closeForSalaryMonth) {
        this.closeForSalaryMonth = closeForSalaryMonth;
    }

    public SortedSet<Incoming> getIncomings() {
        return incomings;
    }

    public void setIncomings(SortedSet<Incoming> incomings) {
        this.incomings = incomings;
    }
}
