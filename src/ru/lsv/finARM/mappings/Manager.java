package ru.lsv.finARM.mappings;

import java.sql.Date;

/**
 * Маппинг для managers
 */
public class Manager implements Comparable{

    /**
     * Идентификатор
     */
    private Integer managerId;
    /**
     * ФИО 
     */
    private String FIO;

    /**
     * Получение процента за наличные сделки
     * @return см.описание
     */
    public Double getCashPercent() {
        return cashPercent;
    }

    /**
     * Установка процента за наличные сделки
     * @param cashPercent см.описание
     */
    public void setCashPercent(Double cashPercent) {
        this.cashPercent = cashPercent;
    }

    /**
     * Получение даты увольнения
     * @return см.описание
     */
    public Date getDismissDate() {
        return dismissDate;
    }

    /**
     * Установка даты увольнения
     * @param dismissDate см.описание
     */
    public void setDismissDate(Date dismissDate) {
        this.dismissDate = dismissDate;
    }

    /**
     * Получение признака увольнения
     * @return см.описание
     */
    public Boolean isDismissed() {
        return dismissed;
    }

    /**
     * Установка признака удаления
     * @param dismissed см.описание
     */
    public void setDismissed(Boolean dismissed) {
        this.dismissed = dismissed;
    }

    /**
     * Получение ФИО
     * @return см.описание
     */
    public String getFIO() {
        return FIO;
    }

    /**
     * Установка ФИО
     * @param FIO см.описание
     */
    public void setFIO(String FIO) {
        this.FIO = FIO;
    }

    /**
     * Получение идентификатора
     * @return см.описание
     */
    public Integer getManagerId() {
        return managerId;
    }

    /**
     * Установка идентификатора
     * @param managerId см.описание
     */
    public void setManagerId(Integer managerId) {
        this.managerId = managerId;
    }

    /**
     * Получение даты приема на работу
     * @return см.описание
     */
    public Date getIncomeDate() {
        return incomeDate;
    }

    /**
     * Установка даты приема на работу
     * @param incomeDate см.описание
     */
    public void setIncomeDate(Date incomeDate) {
        this.incomeDate = incomeDate;
    }

    /**
     * Получение процента с безналичной сделки
     * @return см.описание
     */
    public Double getNonCashPercent() {
        return nonCashPercent;
    }

    /**
     * Установка процента с безналичной сделки
     * @param nonCashPercent см.описание
     */
    public void setNonCashPercent(Double nonCashPercent) {
        this.nonCashPercent = nonCashPercent;
    }

    /**
     * Получение суммы удержания
     * @return см.описание
     */
    public Double getRetention() {
        return retention;
    }

    /**
     * Установка суммы удержания
     * @param retention см.описание
     */
    public void setRetention(Double retention) {
        this.retention = retention;
    }

    /**
     * Получение суммы субсидии
     * @return см.описание
     */
    public Double getSubsidy() {
        return subsidy;
    }

    /**
     * Установка суммы субсидии
     * @param subsidy см.описание
     */
    public void setSubsidy(Double subsidy) {
        this.subsidy = subsidy;
    }

    /**
     * Получение суммы оклада
     * @return Сумма оклада
     */
    public Double getSalary() {
        return salary == null ? 0.0 : salary;
    }

    /**
     * Установка суммы оклада
     * @param salary Сумма оклада
     */
    public void setSalary(Double salary) {
        this.salary = salary;
    }

    /**
     * Дата приема на работу
     */
    private Date incomeDate;
    /**
     * Сумма субсидии
     */
    private Double subsidy;
    /**
     * Сумма удержаний
     */
    private Double retention;
    /**
     * Процент с наличных сделок
      */
    private Double cashPercent;
    /**
     * Процент с безналичных сделок
     */
    private Double nonCashPercent;
    /**
     * Признак увольнения
     */
    private Boolean dismissed;
    /**
     * Дата увольнения
     */
    private Date dismissDate;
    /**
     * Оклад
     */
    private Double salary;

    public Manager() {
        managerId = null;
        dismissed = false;
        FIO = "";
        incomeDate = null;
        subsidy = 0.0;
        retention = 0.0;
        cashPercent = 50.0;
        nonCashPercent = 40.0;
        salary = 0.0;
        dismissDate = null;
    }

    /**
     * Создает копию
     * @param manager С чего создавать копию
     */
    public Manager(Manager manager) {
        managerId = manager.managerId;
        dismissed = manager.dismissed;
        FIO = manager.FIO;
        incomeDate = manager.incomeDate;
        subsidy = manager.subsidy;
        retention = manager.retention;
        cashPercent = manager.cashPercent;
        nonCashPercent = manager.nonCashPercent;
        salary = manager.salary;
        dismissDate = manager.dismissDate;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return <code>true</code> if this object is the same as the obj
     *         argument; <code>false</code> otherwise.
     * @see #hashCode()
     * @see java.util.Hashtable
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj.getClass().equals(this.getClass())) {
            Manager mng = (Manager)obj;
            return (managerId == null ? (mng.getManagerId() == null) : managerId.equals(mng.getManagerId()));
        } else {
            return false;
        }        
    }

    /**
     * Hash code calculation
     * @return hash
     */
    @Override
    public int hashCode() {
        return (managerId == null ? 0 : managerId.hashCode());
    }

    /**
     * см. @java.land.String
     * @return Тектовое представление
     */
    @Override
    public String toString() {
        return FIO;
    }

    /**
     * Возвращает нужное значение по индексу для table model
     * @param columnIndex см.описание
     * @return Значение одного из полей в зависимости от columnIndex
     */
    public Object getValueByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0 : return FIO;
                case 1 : return incomeDate;
                case 2 : return salary;
                case 3 : return subsidy;
                case 4 : return retention;
                case 5 : return cashPercent;
                case 6 : return nonCashPercent;
                case 7 : return dismissed;
                case 8 : return dismissDate;
                default: return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Возвращает класс поля в зависимости от индекса
     * @param columnIndex см.описание
     * @return Класс поля в зависимости от индекса
     */
    public static Class getValueClassByIndex(int columnIndex) {
        if (columnIndex < getValuesCount()) {
            Manager tempManager = new Manager();
            switch (columnIndex) {
                case 0 : return tempManager.FIO.getClass();
                case 1 : return Date.class;
                case 2 : return tempManager.salary.getClass();
                case 3 : return tempManager.subsidy.getClass();
                case 4 : return tempManager.retention.getClass();
                case 5 : return tempManager.cashPercent.getClass();
                case 6 : return tempManager.nonCashPercent.getClass();
                case 7 : return Boolean.class;
                case 8 : return Date.class;
                default: return null;
            }
        } else {
            return null;        }

    }

    /**
     * Возвращает название поля в зависимости от индекса
     * @param columnIndex см.описание
     * @return Название одного из полей в зависимости от индекса
     */
    public static String getValueNameByIndex (int columnIndex) {
        if (columnIndex < getValuesCount()) {
            switch (columnIndex) {
                case 0 : return "ФИО";
                case 1 : return "Дата приема";
                case 2 : return "Оклад (руб.)";
                case 3 : return "Доплаты (руб.)";
                case 4 : return "Удержания (руб.)";
                case 5 : return "% за наличные";
                case 6 : return "% за безналичные";
                case 7 : return "Уволен";
                case 8 : return "Дата увольнения";
                default: return null;
            }
        } else {
            return null;
        }        
    }

    /**
     * Возвращает количество полей, которые будут отображаться в table model
     * @return см.описание
     */
    public static int getValuesCount() {
        return 9;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     *         is less than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it
     *                            from being compared to this object.
     */
    @Override
    public int compareTo(Object o) {
        if (o == null) return 1;
        return toString().compareTo(o.toString());
    }

}
