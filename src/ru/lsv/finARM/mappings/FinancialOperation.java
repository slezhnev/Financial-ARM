package ru.lsv.finARM.mappings;

import ru.lsv.finARM.common.CommonUtils;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Финансовая операция
 */
public class FinancialOperation {

    public FinancialOperation() {
        closed = false;
        operationSum = 0.0;
        salarySum = 0.0;
        operationDate = new Date(Calendar.getInstance().getTimeInMillis());
        paymentType = 0;
        kind = 0;
        spendings = new HashSet<Spending>();
        incomings = new HashSet<Incoming>();
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

    public Set<Spending> getSpendings() {
        return spendings;
    }

    public void setSpendings(Set<Spending> spendings) {
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
    private Set<Spending> spendings;
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
    private Set<Incoming> incomings;

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

    public Set<Incoming> getIncomings() {
        return incomings;
    }

    public void setIncomings(Set<Incoming> incomings) {
        this.incomings = incomings;
    }
}
